package io.github.kwvolt.japanesedictionary.presentation.addupdate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormCommandManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.UiFormHandlerInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordFormHandler
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.UndoRedoStateListener
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation.FormItemValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation.InputTextFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation.StaticLabelFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation.WordClassFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import kotlinx.coroutines.launch

class AddUpdateViewModel(
    private val wordFormService: WordFormService,
): ViewModel() {

    // handles managing data
    private lateinit var wordFormHandler: WordFormHandler

    // word class variables
    private var mainClassData: List<MainClassContainer> = listOf(MainClassContainer(0, "TEMP", "temp"))
    private var subClassMapData: Map<Long, List<SubClassContainer>> = mapOf(0L to listOf(SubClassContainer(0, "TEMP", "temp")))

    // validation Error
    private val errorMessage: MutableMap<ValidationKey, ErrorMessage> = mutableMapOf()

    // Main item list
    private val _uiState: MutableLiveData<UiState<List<BaseItem>>> = MutableLiveData()
    val uiState: LiveData<UiState<List<BaseItem>>> get() = _uiState

    // Undo Redo
    private val _canUndo = MutableLiveData(false)
    val canUndo: LiveData<Boolean> = _canUndo

    private val _canRedo = MutableLiveData(false)
    val canRedo: LiveData<Boolean> = _canRedo

    fun loadWordClassSpinner(){
        viewModelScope.launch {
            //_uiState.value = UiState.Loading
            val result = wordFormService.getMainClassList().flatMap { main ->
                mainClassData = main
                wordFormService.getSubClassMap(mainClassData).flatMap { subMap ->
                    subClassMapData = subMap
                    DatabaseResult.Success(Unit)
                }
            }
            when(result){
                is DatabaseResult.InvalidInputMap -> {
                    TODO()
                }
                is DatabaseResult.Success<Unit> -> TODO()
                is DatabaseResult.UnknownError -> TODO()
                else -> TODO()
            }
        }
    }

    fun loadItems(dictionaryEntryId: Long, formSectionManager: FormSectionManager, wordUiFormHandler: UiFormHandlerInterface) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = wordFormService.getWordFormData(dictionaryEntryId, formSectionManager).flatMap { data ->
                val handler = FormCommandManager(data)
                wordFormHandler = WordFormHandler(handler, formSectionManager, wordUiFormHandler)
                val formList = wordFormHandler.generateFormList()

                setUndoRedoListener()

                _uiState.postValue(UiState.Success(formList))
                DatabaseResult.Success(Unit)
            }

            if (result is DatabaseResult.UnknownError) {
                _uiState.postValue(UiState.UnknownError(result.exception, result.message ?: ""))
            }
        }
    }

    fun loadItems(formSectionManager: FormSectionManager, wordUiFormHandler: UiFormHandlerInterface) {
        _uiState.value = UiState.Loading
        val subClassList: List<SubClassContainer> = subClassMapData[mainClassData[0].id] ?: emptyList()
        val wordClassItem: WordClassItem = WordClassItem(
            0, 0,
            subClassList,
            ItemProperties(WordEntryTable.WORD_CLASS))
        val wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(wordClassItem, formSectionManager)
        val dataHandler = FormCommandManager(wordEntryFormData)
        wordFormHandler = WordFormHandler(dataHandler, formSectionManager, wordUiFormHandler)
        val list = wordFormHandler.generateFormList()
        _uiState.postValue(UiState.Success(list))
        setUndoRedoListener()
    }

    fun upsertValuesIntoDB(){
        val wordEntryFormData: WordEntryFormData = wordFormHandler.getWordEntryFormData()
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = wordFormService.upsertWordEntryFormDataIntoDatabase(
                wordEntryFormData,
                deleteList = wordFormHandler.getDeletedItemIds().toList()
            )
            when(result){
                is DatabaseResult.InvalidInputMap -> {
                    val errorMap: Map<ValidationKey, ErrorMessage> = result.errors.entries.associate { (key, value) ->
                        key to ErrorMessage(errorMessage =formatValidationTypeToMessage(value), isDirty = true)
                    }
                    errorMessage.putAll(errorMap)
                    wordFormHandler.generateFormList(errorMap)
                    _uiState.postValue(UiState.ValidationError())
                    TODO()
                }
                is DatabaseResult.Success<Unit> -> TODO()
                is DatabaseResult.UnknownError -> TODO()
                else -> TODO()
            }
        }
    }

    fun redo(){
        val list = wordFormHandler.redo()
        if(list.isNotEmpty()) {
            _uiState.postValue(UiState.Success(list))
        }
    }

    fun undo(){
        val list = wordFormHandler.undo()
        if(list.isNotEmpty()) {
            _uiState.postValue(UiState.Success(list))
        }
    }

    private fun setUndoRedoListener(){
        wordFormHandler.setUndoRedoListener(object : UndoRedoStateListener {
            override fun onStateChanged(canUndo: Boolean, canRedo: Boolean) {
                _canUndo.postValue(canUndo)
                _canRedo.postValue(canRedo)
            }
        })
    }

    private fun withList(action: (MutableList<BaseItem>) -> Unit) {
        val currentList = (uiState.value as? UiState.Success)?.data?.toMutableList() ?: return
        action(currentList)
        _uiState.postValue(UiState.Success(currentList))
    }

    private fun addItemAtPosition(item: BaseItem, position: Int) {
        withList { it.add(position, item) }
    }

    private fun removeItemAtPosition(position: Int) {
        withList { it.removeAt(position) }
    }

    private fun updateItemAtPosition(item: BaseItem, position: Int) {
        withList { it[position] = item }
    }

    // section
    fun addSectionClicked(position: Int) {
        val newItems = wordFormHandler.createNewSection()
        withList { it.addAll(position, newItems) }
    }

    fun removeSectionClicked(entryLabelItem: EntryLabelItem, position: Int) {
        val updated = wordFormHandler.removeSection(
            (uiState.value as? UiState.Success)?.data.orEmpty(),
            entryLabelItem.itemProperties.getSectionIndex(),
            entryLabelItem.sectionCount,
            position
        )
        _uiState.postValue(UiState.Success(updated))
    }

    // Word Class
    fun updateMainClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int): Boolean{
        val selectedMainClass: MainClassContainer = mainClassData[selectionPosition]
        if(wordClassItem.chosenMainClassId != selectedMainClass.id) {
            val subClassList: List<SubClassContainer> = subClassMapData[selectedMainClass.id] ?: emptyList()
            val updateWordClassItem: WordClassItem = wordClassItem.copy(chosenMainClassId = selectedMainClass.id, currentSubClassData = subClassList)
            wordFormHandler.updateWordClassId(updateWordClassItem)
            updateItemAtPosition(updateWordClassItem, position)
            return true
        }
        return false
    }

    fun updateSubClassId(wordClassItem: WordClassItem, selectedPosition: Int, position: Int){
        val subClassId: Long = wordClassItem.currentSubClassData[selectedPosition].id
        val updateWordClassItem: WordClassItem = wordClassItem.copy(chosenSubClassId = subClassId)
        wordFormHandler.updateWordClassId(updateWordClassItem)
        updateItemAtPosition(updateWordClassItem, position)
    }

    fun getMainClassListIndex(wordClassItem: WordClassItem): Int{
        val mainClassIndex: Int = mainClassData.indexOfFirst { it.id.toInt() == wordClassItem.chosenMainClassId.toInt() }.takeIf { it >= 0 } ?: 0
        return mainClassIndex
    }

    fun getSubClassListIndex(wordClassItem: WordClassItem): Int{
        val subClassList: List<SubClassContainer> = wordClassItem.currentSubClassData
        val subClassIndex = subClassList.indexOfFirst { it.id.toInt() == wordClassItem.chosenSubClassId.toInt() }.takeIf { it >= 0 } ?: 0
        return subClassIndex
    }

    fun getMainClassList(): List<MainClassContainer>{
        return mainClassData
    }

    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>{
        return wordClassItem.currentSubClassData
    }

    // TextInput
    fun addTextItemClicked(action: ButtonAction.AddItem, position: Int){
        val inputTextType = action.inputType
        val inputTextItem = InputTextItem(inputTextType, itemProperties = ItemProperties())
        wordFormHandler.addItemCommand(inputTextItem)
        addItemAtPosition(inputTextItem, position)
    }

    fun addChildTextItemClicked(action: ButtonAction.AddChild, position: Int){
        val inputTextType = action.inputTextType
        val parent: EntryLabelItem = action.entryLabelItem
        val inputTextItem = InputTextItem(inputTextType, itemProperties = ItemSectionProperties(sectionId = parent.itemProperties.getSectionIndex()))
        wordFormHandler.addItemCommand(inputTextItem)
        addItemAtPosition(inputTextItem, position)
    }

    //Primary Text, Kana, Meaning, Dictionary Entry Note. Section Entry Note
    fun updateInputTextValue(inputTextItem: InputTextItem, textValue: String, position: Int) {
        val updatedItem = inputTextItem.copy(inputTextValue = textValue)
        wordFormHandler.updateItemCommand(inputTextItem,  textValue)
        updateItemAtPosition(updatedItem, position)
    }

    fun removeTextItemClicked(inputTextItem: InputTextItem, position: Int){
        wordFormHandler.removeItemCommand(inputTextItem)
        removeItemAtPosition(position)
    }

    private suspend fun revalidateDirtyFieldsAfterUndoRedo(currentItems: List<BaseItem>) {
        val updatedErrors = mutableMapOf<ValidationKey, ErrorMessage>()
        val formData = wordFormHandler.getWordEntryFormData()

        val uiItems = currentItems.asSequence()
            .filterIsInstance<FormUIItem>()
            .filter { uiItem ->
                when (uiItem) {
                    is InputTextFormUIItem ->
                        errorMessage.containsKey(ValidationKey.DataItem(uiItem.inputTextItem.itemProperties.getIdentifier()))
                    is StaticLabelFormUIItem -> {
                        val props = uiItem.itemProperties as ItemSectionProperties
                        errorMessage.containsKey(ValidationKey.FormItem(FormKeys.kanaLabel(props.getSectionIndex())))
                    }
                    is WordClassFormUIItem ->
                        errorMessage.containsKey(ValidationKey.DataItem(uiItem.wordClassItem.itemProperties.getIdentifier()))
                }
            }.toList()

        for (uiItem in uiItems) {
            val validator = when (uiItem) {
                is InputTextFormUIItem -> {
                    val validator = InputTextFormValidator(wordFormService)
                    validator.validate(uiItem, formData)
                }
                is StaticLabelFormUIItem -> {
                    val validator = StaticLabelFormValidator(wordFormService)
                    validator.validate(uiItem, formData)
                }
                is WordClassFormUIItem -> {
                    val validator = WordClassFormValidator(wordFormService)
                    validator.validate(uiItem, formData)
                }
            }

            val (key, result) = validator
            when (result){
                is ValidationResult.InvalidInput -> {
                    val error = result.error
                    updatedErrors[key] = ErrorMessage(errorMessage = formatValidationTypeToMessage(error), isDirty = true)
                }
                is ValidationResult.Success ->{
                    errorMessage.remove(key)
                }
                is ValidationResult.UnknownError -> {
                    val exception = result.exception
                    val message = result.message
                    if (exception != null && message != null) {
                        _uiState.postValue(UiState.UnknownError(exception, message))
                    }
                }
                else -> continue
            }
        }
        errorMessage.putAll(updatedErrors)
    }
}

sealed class UiState<out T>{
    data class Success<T>(val data: T): UiState<T>()
    data object Loading: UiState<Nothing>()
    data class ValidationError(val error: Map<String, String>): UiState<Nothing>()
    data class UnknownError(val exception: Throwable, val message: String) : UiState<Nothing>()
}