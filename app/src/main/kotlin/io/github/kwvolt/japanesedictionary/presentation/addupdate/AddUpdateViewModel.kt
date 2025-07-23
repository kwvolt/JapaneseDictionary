package io.github.kwvolt.japanesedictionary.presentation.addupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.formatDatabaseErrorTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.UpsertResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormCommandManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordFormHandler
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormListValidatorManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.UndoRedoStateListener
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordFormItemListManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.FormItemValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.InputTextFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.StaticLabelFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.WordClassFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.ui.model.FormScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddUpdateViewModel(
    private val _wordFormService: WordFormService,
    private val _listManager: WordFormItemListManager,
    private val _formItemManager: FormItemManager,
    private val _wordClassDataManager: WordClassDataManager,
    private val _formListValidatorManager: FormListValidatorManager
): ViewModel() {

    // handles managing data
    private var _wordFormHandler: WordFormHandler? = null

    private val _uiState = MutableStateFlow(FormScreenState())
    val uiState: StateFlow<FormScreenState> get() = _uiState

    // retrieves Word Class values from database
    fun loadWordClassSpinner(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = _wordClassDataManager.loadWordClassData(_wordFormService)
            handleDatabaseResult(result, "Value was not found within the database for LoadWordClassSpinner"){
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadItems(dictionaryEntryId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = _wordFormService.getWordFormData(dictionaryEntryId, _formItemManager).blankMap { data ->
                val commandManager = FormCommandManager(data)
                _wordFormHandler = WordFormHandler(commandManager)
            }

            handleDatabaseResult(result, "Value was not found within the database for LoadItems"){
                withWordFormHandler { handler ->
                    val formList: List<BaseItem> = _listManager.generateFormList(handler.getWordEntryFormData(), _formItemManager)
                    setUndoRedoListener()
                    _uiState.update { it.copy(isLoading = false, items = formList) }
                }
            }
        }
    }

    fun loadItems() {
        _uiState.update { it.copy(isLoading = true)}
        val wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
        val dataHandler = FormCommandManager(wordEntryFormData)
        _wordFormHandler = WordFormHandler(dataHandler)

        withWordFormHandler { handler ->
            val list: List<BaseItem> = _listManager.generateFormList(handler.getWordEntryFormData(), _formItemManager)
            _uiState.update { it.copy(isLoading = false, items = list) }
            setUndoRedoListener()
        }
    }

    fun upsertValuesIntoDB() {
        withWordFormHandler { handler ->
            val wordEntryFormData: WordEntryFormData = handler.getWordEntryFormData()
            _uiState.value = _uiState.value.copy(isLoading = true)
            viewModelScope.launch {

                val result = _wordFormService.upsertWordEntryFormDataIntoDatabase(
                    wordEntryFormData,
                    deleteList = handler.getDeletedItemIds().toList()
                )

                when (result) {
                    is UpsertResult.ItemsOperationFailed -> {
                        val errorMap: Map<ItemKey, ErrorMessage> =

                            result { (key, value) ->
                                key to ErrorMessage(value , isDirty = true)
                            }
                        val updatedErrorMessage: MutableMap<ItemKey, ErrorMessage> =
                            uiState.value.errors.toMutableMap()
                        updatedErrorMessage.putAll(errorMap)
                        val itemList: List<BaseItem> = _listManager.reBuild(handler.getWordEntryFormData(), _formItemManager, updatedErrorMessage)
                        _uiState.update {
                            it.copy(
                                items = itemList,
                                errors = updatedErrorMessage,
                                isLoading = false
                            )
                        }
                    }

                    is UpsertResult.SingleItemOperationFailed -> {
                        val updatedErrorMessage: MutableMap<ItemKey, ErrorMessage> =
                            uiState.value.errors.toMutableMap()
                        updatedErrorMessage[result.itemKey] = ErrorMessage(
                            errorMessage = formatDatabaseErrorTypeToMessage(result.error),
                            isDirty = true
                        )

                        val itemList: List<BaseItem> = _listManager.generateFormList(handler.getWordEntryFormData(), _formItemManager, updatedErrorMessage)
                        _uiState.update {
                            it.copy(
                                items = itemList,
                                errors = updatedErrorMessage,
                                isLoading = false
                            )
                        }

                    }

                    is UpsertResult.Success -> _uiState.update { it.copy(isLoading = false) }
                    is UpsertResult.UnknownError -> _uiState.update {
                        it.copy(
                            screenStateUnknownError = ScreenStateUnknownError(
                                result.exception,
                                result.message
                            )
                        )
                    }

                    UpsertResult.NotFound -> _uiState.update {
                        it.copy(
                            screenStateUnknownError = ScreenStateUnknownError(
                                NotFoundException("Value was not found within the database for upsertValuesIntoDB"),
                                "Value was not found within the database for upsertValuesIntoDB"
                            )
                        )
                    }
                }
            }
        }
    }

    fun redo(){
        redoUndo { it.redo() }
    }

    fun undo(){
        redoUndo { it.undo() }
    }

    private fun redoUndo(block: (WordFormHandler) -> Unit){
        _uiState.update { it.copy(isLoading = true) }
        withWordFormHandler { handler ->
            block(handler)
            viewModelScope.launch {
                val errorMap = uiState.value.errors
                val list = _listManager.reBuild(handler.getWordEntryFormData(), _formItemManager, errorMap)
                val updatedUiState = _formListValidatorManager.revalidateEntireList(
                    handler, list, errorMap.toMutableMap(), _wordFormService, uiState.value, _listManager
                )
                _uiState.update { updatedUiState.copy(isLoading = false) }
            }
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
        withWordFormHandler { handler ->
            val newSectionId = handler.createNewSection(_formItemManager)
            val newItems: List<BaseItem> = _listManager.generateSectionList(newSectionId, handler.getWordEntryFormData(), _formItemManager, uiState.value.errors)
            val updated = _listManager.addItemsAt(uiState.value.items, newItems, position)
            _uiState.update { it.copy(items = updated) }
        }
    }

    fun removeSectionClicked(entryLabelItem: EntryLabelItem, position: Int) {
        withWordFormHandler { handler ->
            val sectionId = entryLabelItem.itemProperties.getSectionIndex()
            handler.removeSection(sectionId)
            val updated = _listManager.removeSection(uiState.value.items, sectionId, entryLabelItem.sectionCount, position, _formItemManager)
            _uiState.update { it.copy(items = updated) }
        }
    }

    // Word Class
    fun updateMainClassId(wordClassFormUIItem: WordClassFormUIItem, selectionPosition: Int, position: Int): Boolean {
        var updated = false
        withWordFormHandler { handler ->
            val updatedItem: WordClassItem?  = _wordClassDataManager.updateMainClassId(
                wordClassFormUIItem.wordClassItem,
                selectionPosition,
                handler
            )
            if (updatedItem != null) {
                val updatedUIItem = wordClassFormUIItem.copy(updatedItem)
                val validator = WordClassFormValidator(_wordFormService)
                updateAndValidateFormUIItem(updatedUIItem, position, validator)
                updated = true
            }
        }
        return updated
    }

    fun updateSubClassId(wordClassFormUIItem: WordClassFormUIItem, selectedPosition: Int, position: Int){
        withWordFormHandler { handler ->
            val wordClassItem = wordClassFormUIItem.wordClassItem
            val updatedItem: WordClassItem? = _wordClassDataManager.updateSubClassId(wordClassItem, selectedPosition, handler)
            if (updatedItem != null) {
                val updatedWordClassFormUIItem = wordClassFormUIItem.copy(updatedItem)
                val validator = WordClassFormValidator(_wordFormService)
                updateAndValidateFormUIItem(updatedWordClassFormUIItem, position, validator)
            }
        }
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
            _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(IllegalStateException("Invalid value accessed within the subClassMap"), "Invalid value accessed within the subClassMap")) }
        }
        return subList
    }

    fun addTextItemClicked(action: ButtonAction.AddItem, position: Int){
        withWordFormHandler { handler ->
            val inputTextType = action.inputType
            val textItem: TextItem = handler.addTextItemCommand(inputTextType, formItemManager = _formItemManager)
            val inputTextFormUIItem = InputTextFormUIItem(textItem, ErrorMessage())
            _uiState.update { it.copy(items = _listManager.addItemAt(uiState.value.items, inputTextFormUIItem, position))}
        }
    }

    fun addChildTextItemClicked(action: ButtonAction.AddChild, position: Int){
        withWordFormHandler { handler ->
            val inputTextType = action.inputTextType
            val parent: EntryLabelItem = action.entryLabelItem
            val textItem: TextItem = handler.addTextItemCommand(inputTextType, parent.itemProperties.getSectionIndex(), formItemManager = _formItemManager)
            val inputTextFormUIItem = InputTextFormUIItem(textItem, ErrorMessage())
            _uiState.update { it.copy(items = _listManager.addItemAt(uiState.value.items, inputTextFormUIItem, position))}
        }
    }

    //Primary Text, Kana, Meaning, Dictionary Entry Note. Section Entry Note
    fun updateInputTextValue(inputTextFormUIItem: InputTextFormUIItem, textValue: String, position: Int) {
        withWordFormHandler { handler ->
            val newInputTextItem = handler.updateTextItemCommand(inputTextFormUIItem.textItem, textValue)
            val updatedItem = inputTextFormUIItem.copy(newInputTextItem)

            val validator = InputTextFormValidator(_wordFormService)
            updateAndValidateFormUIItem(updatedItem, position, validator)
        }
    }

    fun removeTextItemClicked(inputTextFormUIItem: InputTextFormUIItem, position: Int){
        withWordFormHandler { handler ->
            val textItem: TextItem = inputTextFormUIItem.textItem
            handler.removeItemCommand(textItem)
            _uiState.update { it.copy(items = _listManager.removeItemAt(uiState.value.items, position)) }
        }
    }

    fun validateStaticLabelFormUIItem(staticLabelFormUIItem: StaticLabelFormUIItem){
        val currentList = uiState.value.items
        val labelIndex = currentList.indexOfFirst {
            it is StaticLabelFormUIItem && it.itemProperties.getIdentifier() == staticLabelFormUIItem.itemProperties.getIdentifier()
        }
        if(labelIndex >= 0){
            val staticLabelFormValidator = StaticLabelFormValidator(_wordFormService)
            updateAndValidateFormUIItem(staticLabelFormUIItem, labelIndex, staticLabelFormValidator)
        }
    }

    private fun <T : FormUIItem> updateAndValidateFormUIItem(
        updatedItem: T,
        position: Int,
        validator: FormItemValidator<T>
    ) {
        if (updatedItem.errorMessage.isDirty) {
            viewModelScope.launch {
                withWordFormHandler { handler ->
                    val updatedState: FormScreenState = _formListValidatorManager.validateAndUpdateItem(
                        updatedItem,
                        position,
                        handler.getWordEntryFormData(),
                        validator,
                        uiState.value,
                        _listManager
                    )
                    _uiState.update { updatedState }
                }
            }
        } else {
            // No validation error, just update UI list
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
            _uiState.update {
                it.copy(
                    screenStateUnknownError = ScreenStateUnknownError(
                        IllegalStateException("WordFormHandler was not initialized"),
                        "Internal error: WordFormHandler was not initialized"
                    )
                )
            }
        }
    }

    private inline fun handleDatabaseResult(
        result: DatabaseResult<Unit>,
        notFoundMessage: String,
        onSuccess: () -> Unit
    ) {
        when (result) {
            is DatabaseResult.Success -> onSuccess()
            is DatabaseResult.UnknownError -> _uiState.update {
                it.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message))
            }
            DatabaseResult.NotFound -> _uiState.update {
                it.copy(screenStateUnknownError = ScreenStateUnknownError(
                    NotFoundException(notFoundMessage),
                    notFoundMessage
                ))
            }
            else -> _uiState.update {
                it.copy(screenStateUnknownError = ScreenStateUnknownError(
                    IllegalStateException("InvalidInput should not be accessible"),
                    "InvalidInput should not be accessible"
                ))
            }
        }
    }
}

class NotFoundException(message: String): Exception(message)