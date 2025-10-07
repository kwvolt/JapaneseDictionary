package io.github.kwvolt.japanesedictionary.presentation.upsert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidUpsertResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormItemFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import io.github.kwvolt.japanesedictionary.domain.form.upsert.WordEntryFormCleaner
import io.github.kwvolt.japanesedictionary.domain.model.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.FormCommandManager
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordFormHandler
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.FormListValidatorManager
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.UndoRedoStateListener
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.WordFormItemListManager
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.ui.model.FormScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import io.github.kwvolt.japanesedictionary.util.handleResultWithErrorCopy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpsertViewModel(
    private val _listManager: WordFormItemListManager,
    private val _formItemManager: FormItemManager,
    private val _wordClassDataManager: WordClassDataManager,
    private val _formListValidatorManager: FormListValidatorManager,
    private val _wordEntryFormUpsertValidation: WordEntryFormUpsertValidation,
    private val _wordFormEntryFormFetcher: WordEntryFormItemFetcher,
    private val _wordEntryFormBuilder: WordEntryFormBuilder
): ViewModel() {

    // handles managing data
    private var _wordFormHandler: WordFormHandler? = null

    private val _uiState = MutableStateFlow(FormScreenState())
    val uiState: StateFlow<FormScreenState> get() = _uiState

    // retrieves Word Class values from database
    suspend fun loadWordClassSpinner(){
        _uiState.update { it.copy(isLoading = true) }
        val result = _wordClassDataManager.loadWordClassData()
        _uiState.handleResultWithErrorCopy( "loadWordClassSpinner", result){
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun loadExistingItemForm(dictionaryEntryId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        val result = _wordEntryFormBuilder.buildDetailedFormData(dictionaryEntryId, _formItemManager).toUnit { data ->
            val commandManager = FormCommandManager(data)
            _wordFormHandler = WordFormHandler(commandManager)
        }
        _uiState.handleResultWithErrorCopy("loadExistingItemForm", result){
            formBuilder()
        }
    }

    fun initializeNewItemForm() {
        _uiState.update { it.copy(isLoading = true)}
        val wordEntryFormData: WordEntryFormData =
            _wordClassDataManager.initializeWordEntryFormDataWordClass(
                WordEntryFormData.buildDefault(_formItemManager)
            )
        val commandManager = FormCommandManager(wordEntryFormData)
        _wordFormHandler = WordFormHandler(commandManager)
        formBuilder()
    }

    private fun formBuilder(){
        withWordFormHandler { handler: WordFormHandler ->
            val formList: List<DisplayItem> = _listManager.generateFormList(handler.getWordEntryFormData(), _formItemManager)
            setUndoRedoListener()
            _uiState.update { it.copy(isLoading = false, items = formList) }
        }
    }

    fun upsertValuesIntoDB(){
        withWordFormHandler { handler ->
            _uiState.update { it.copy(isLoading = true) }
            val wordEntryFormData: WordEntryFormData = WordEntryFormCleaner.cleanWordEntryFormData(handler.getWordEntryFormData())
            viewModelScope.launch {
                val result = _wordEntryFormUpsertValidation.wordEntryForm(
                    wordEntryFormData,
                    deleteList = handler.getDeletedItemIds().toList()
                )
                when (result) {
                    is ValidUpsertResult.ItemsOperationFailed -> {
                        val errorMap: Map<ItemKey, ErrorMessage> = result.errors.entries.associate{ (key, value) ->
                            key to ErrorMessage(value , isDirty = true)
                        }
                        applyItemErrors(handler, errorMap)
                    }
                    is ValidUpsertResult.SingleItemOperationFailed -> {
                        applyItemErrors(handler, mapOf(result.itemKey to ErrorMessage(errorMessage = result.error, isDirty = true)))
                    }
                    is ValidUpsertResult.Success -> _uiState.update { it.copy(isLoading = false, confirmed = result.value) }
                    is ValidUpsertResult.UnknownError -> reportUnknownError(result.exception)
                    ValidUpsertResult.NotFound -> reportUnknownError(NotFoundException("Value was not found within the database for upsertValuesIntoDB").fillInStackTrace())
                }
            }
        }
    }

    private fun applyItemErrors(
        handler: WordFormHandler,
        errorMap: Map<ItemKey, ErrorMessage>
    ) {
        val updatedErrorMessage = uiState.value.errors.toMutableMap().apply {
            putAll(errorMap)
        }
        val updatedItems = _listManager.reBuild(handler.getWordEntryFormData(), _formItemManager, updatedErrorMessage)
        _uiState.update { it.copy(items = updatedItems, errors = updatedErrorMessage, isLoading = false) }
    }

    fun redo() = redoUndo { it.redo() }


    fun undo() = redoUndo { it.undo() }


    private fun redoUndo(block: (WordFormHandler) -> Unit){
        _uiState.update { it.copy(isLoading = true) }
        withWordFormHandler { handler ->
            block(handler)
            val errorMap = uiState.value.errors
            val list = _listManager.reBuild(handler.getWordEntryFormData(), _formItemManager, errorMap)
            val updatedUiState = _formListValidatorManager.revalidateEntireList(
                handler, list, errorMap.toMutableMap(), uiState.value, _listManager
            )
            _uiState.update { updatedUiState.copy(isLoading = false) }
        }
    }

    private fun setUndoRedoListener(){
        withWordFormHandler { handler ->
            handler.setUndoRedoListener(object : UndoRedoStateListener {
                override fun onStateChanged(canUndo: Boolean, canRedo: Boolean) {
                    _uiState.update{ it.copy(canUndo = canUndo, canRedo = canRedo) }
                }
            })
        }
    }

    // section
    fun addSectionClicked(position: Int) {
        withWordFormHandler { handler: WordFormHandler ->
            val newSectionId = handler.createNewSection(_formItemManager)
            val newItems: List<DisplayItem> = _listManager.generateSectionList(newSectionId, handler.getWordEntryFormData(), _formItemManager, uiState.value.errors)
            val updated = _listManager.addItemsAt(uiState.value.items, newItems, position)
            _uiState.update { it.copy(items = updated) }
        }
    }

    fun removeSectionClicked(sectionLabelItem: SectionLabelItem, position: Int) {
        withWordFormHandler { handler: WordFormHandler ->
            val sectionId = sectionLabelItem.itemProperties.getSectionIndex()
            handler.removeSection(sectionId)
            val updated = _listManager.removeSection(uiState.value.items, sectionId, sectionLabelItem.sectionCount, position)
            _uiState.update { it.copy(items = updated) }
        }
    }

    // Word Class
    fun updateMainOrSubClassIdForWordClass(
        displayItem: DisplayItem.DisplayWordClassItem,
        selectionPosition: Int,
        position: Int,
        isMainClass: Boolean
    ): Boolean {
        withWordFormHandler { handler: WordFormHandler ->
            val wordClassItem: WordClassItem = displayItem.item
            val updatedItem: WordClassItem? = if (isMainClass) {
                _wordClassDataManager.updateMainClassId(wordClassItem, selectionPosition, handler)
            } else {
                _wordClassDataManager.updateSubClassId(wordClassItem, selectionPosition, handler)
            }

            if (updatedItem != null) {
                val updatedDisplayItem = displayItem.copy(item = updatedItem)
                updateAndValidateFormUIItem(
                    updatedDisplayItem,
                    updatedDisplayItem.errorMessage,
                    position
                )
                return true
            }
        }
        return false
    }

    fun getMainClassListIndex(wordClassItem: WordClassItem): Int{
        return _wordClassDataManager.getMainClassListIndex(wordClassItem)
    }

    fun getSubClassListIndex(wordClassItem: WordClassItem): Int{
        return _wordClassDataManager.getSubClassListIndex(wordClassItem)
    }

    fun getMainClassList(): List<MainClassContainer>{
        return _wordClassDataManager.getMainClassList()
    }

    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>{
        val subList: List<SubClassContainer> = _wordClassDataManager.getSubClassList(wordClassItem)
        if(subList.isEmpty()){
            _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(IllegalStateException("Invalid value accessed within the subClassMap").fillInStackTrace())) }
        }
        return subList
    }

    fun addTextItemClicked(inputTextType: InputTextType, sectionId: Int? = null, position: Int){
        withWordFormHandler { handler ->
            val textItem: TextItem = handler.addTextItemCommand(inputTextType, sectionId, formItemManager = _formItemManager)
            val newTextItem: DisplayItem.DisplayTextItem =
                DisplayItem.DisplayTextItem(
                    ErrorMessage(),
                    textItem,
                    ItemKey.DataItem(textItem.itemProperties.getIdentifier())
                )
            _uiState.update { it.copy(items = _listManager.addItemAt(uiState.value.items, newTextItem, position, sectionId))}
        }
    }

    //Primary Text, Kana, Meaning, Dictionary Entry Note. Section Entry Note
    fun updateInputTextValue(displayItem: DisplayItem.DisplayTextItem, textValue: String, position: Int) {
        withWordFormHandler { handler ->
            val newTextItem: TextItem = handler.updateTextItemCommand(displayItem.item, textValue)
            val updatedItem: DisplayItem.DisplayTextItem = displayItem.copy(item = newTextItem)
            updateAndValidateFormUIItem(updatedItem, updatedItem.errorMessage,position)
        }
    }

    fun removeTextItemClicked(textItem: TextItem, position: Int){
        withWordFormHandler { handler ->
            handler.removeItemCommand(textItem)
            val sectionId: Int? = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex()
            _uiState.update { it.copy(items = _listManager.removeItemAt(uiState.value.items, position, sectionId)) }
        }
    }

    fun generatePreview(block: (WordEntryFormData) -> Unit) {
        withWordFormHandler { handler: WordFormHandler ->
            val wordEntryFormData: WordEntryFormData =
                WordEntryFormCleaner.cleanWordEntryFormData(handler.getWordEntryFormData())
            block(wordEntryFormData)
        }
    }

    fun reportUnknownError(throwable: Throwable){
        _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(throwable)) }
    }

    private fun  updateAndValidateFormUIItem(
        updatedItem: DisplayItem,
        errorMessage: ErrorMessage,
        position: Int,
    ) {
        // only check if confirm button was clicked and had an error
        if (errorMessage.isDirty) {
            withWordFormHandler { handler ->
                val updatedState: FormScreenState = _formListValidatorManager.validateAndUpdateItem(
                    updatedItem,
                    position,
                    handler.getWordEntryFormData(),
                    uiState.value,
                    _listManager
                )
                _uiState.update { updatedState }
            }
        } else {
            _uiState.update {
                it.copy(items = _listManager.updateItemAt(it.items, updatedItem, position))
            }
        }
    }


    private inline fun withWordFormHandler(block: (WordFormHandler) -> Unit) {
        val handler = _wordFormHandler
        if (handler != null) {
            block(handler)
        } else {
            reportUnknownError(
                IllegalStateException("Internal error: WordFormHandler was not initialized").fillInStackTrace()
            )
        }
    }
}

class NotFoundException(message: String? = "Not Found") : Exception(message)