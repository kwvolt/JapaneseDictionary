package io.github.kwvolt.japanesedictionary.presentation.addupdate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormCommandManager
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.handler.UiFormHandlerInterface
import io.github.kwvolt.japanesedictionary.domain.form.handler.WordFormHandler
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.service.WordFormService
import kotlinx.coroutines.launch

class AddUpdateViewModel(
    private val wordFormService: WordFormService,
): ViewModel() {

    // handles managing data
    private lateinit var wordFormHandler: WordFormHandler

    // word class variables
    private lateinit var mainClassData: List<MainClassContainer>
    private lateinit var subClassMapData: Map<Long, List<SubClassContainer>>

    // used to delete values during update that was removed from Form
    private val deleteFromDatabaseList: List<GenericItemProperties> = listOf()

    // Main   item list
    private val _uiState: MutableLiveData<UiState<List<BaseItem>>> = MutableLiveData()
    val uiState: LiveData<UiState<List<BaseItem>>> get() = _uiState

    fun loadWordClassSpinner(){
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = wordFormService.getMainClassList().flatMap { main ->
                mainClassData = main
                wordFormService.getSubClassMap(mainClassData).flatMap { subMap ->
                    subClassMapData = subMap
                    DatabaseResult.Success(Unit)
                }
            }
            when (result) {
                is DatabaseResult.Success -> Unit
                is DatabaseResult.InvalidInput -> _uiState.postValue(UiState.ValidationError("Invalid input"))
                is DatabaseResult.UnknownError -> _uiState.postValue(UiState.UnknownError(result.exception, result.message ?: "Unknown error"))
                DatabaseResult.NotFound -> _uiState.postValue(UiState.UnknownError(Exception(), "Data not found"))
            }
        }
    }

    fun loadItems(dictionaryEntryId: Long, formSectionManager: FormSectionManager, wordUiFormHandler: UiFormHandlerInterface) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = wordFormService.getWordFormData(dictionaryEntryId, formSectionManager).flatMap { data ->
                val handler = FormCommandManager(data)
                wordFormHandler = WordFormHandler(handler, formSectionManager, wordUiFormHandler)
                val formList = wordFormHandler.createInitialForm()
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
        val wordClassItem: WordClassItem = WordClassItem(0, 0, subClassList, ItemProperties(
            WordEntryTable.WORD_CLASS))
        val wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(wordClassItem, formSectionManager)
        val dataHandler = FormCommandManager(wordEntryFormData)
        wordFormHandler = WordFormHandler(dataHandler, formSectionManager, wordUiFormHandler)
        val list = wordFormHandler.createInitialForm()
        _uiState.postValue(UiState.Success(list))
    }

    fun upsertValuesIntoDB(){
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            wordFormService.upsertWordEntryFormDataIntoDatabase(wordFormHandler.getWordEntryFormData())
        }
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
        val inputTextItem = InputTextItem(inputTextType, itemProperties = ItemSectionProperties(getSectionIndex = parent.itemProperties.getSectionIndex()))
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

    // util
    fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int){
        val updatedItem = wordFormHandler.updateEntryIndexIfNeeded(entryLabelItem)
        if (updatedItem != null) updateItemAtPosition(updatedItem, position)
    }
}

sealed class UiState<out T>{
    data class Success<T>(val data: T): UiState<T>()
    data object Loading: UiState<Nothing>()
    data class ValidationError(val error: String) : UiState<Nothing>()
    data class UnknownError(val exception: Throwable, val message: String) : UiState<Nothing>()
}
