package io.github.kwvolt.japanesedictionary.presentation.addupdate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionary_entry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.entry_component.ComponentRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.FormStateManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.UiFormHandlerInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.WordUiFormHandler
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.FormCommandManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddComponentNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.RemoveSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateComponentNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateMeaningItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdatePrimaryTextCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateWordClassCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import kotlinx.coroutines.launch

class AddUpdateViewModel(
    private val wordClassRepository: WordClassRepositoryInterface,
    private val entryRepository: EntryRepositoryInterface? = null,
    private val componentRepository: ComponentRepositoryInterface? = null,
    private val wordFormService: WordFormService
): ViewModel() {

    // handles managing the number of items in a sections and how many sections exists
    private val formStateManager: FormStateManager = FormStateManager()
    private lateinit var dataHandler: FormCommandManager
    private val wordUiFormHandler: UiFormHandlerInterface = WordUiFormHandler()

    // word class variables
    private var mainClassData: List<MainClassContainer> = listOf()
    private var subClassMapData: MutableMap<MainClassContainer, List<SubClassContainer>> = mutableMapOf()

    // Main item list
    private val _formItems: MutableLiveData<List<BaseItem>> = MutableLiveData(listOf())
    val formItems: LiveData<List<BaseItem>> get() = _formItems

    //private val _uiState: MutableLiveData<UiState<WordFormData>> = MutableLiveData()
    //val uiState: LiveData<UiState<WordFormData>> get() = _uiState

    fun loadWordClassSpinner(){
        viewModelScope.launch {
            mainClassData = wordClassRepository.selectAllMainClass()
            mainClassData.forEach { main ->
                val subList: List<SubClassContainer> = wordClassRepository.selectAllSubClassByMainClassId(main.id)
                subClassMapData[main] = subList
            }
        }

    }

    fun loadItems() {
        val list = wordUiFormHandler.createUIList(dataHandler.wordEntryFormData, formStateManager)
        formStateManager.incrementEntryCount()
        _formItems.postValue(list) // Update the LiveData
    }

    private fun createSectionList(): List<BaseItem>{
        val currentEntryCount: Int = formStateManager.getCurrentEntryCount()
        val command: FormCommand = AddSectionCommand(dataHandler.wordEntryFormData, currentEntryCount)
        dataHandler.executeCommand(command)
        val wordSectionFormData: WordSectionFormData? = dataHandler.wordEntryFormData.wordSectionMap.toMap()[currentEntryCount]
        var list: List<BaseItem> = listOf()
        if (wordSectionFormData != null) {
            list = wordUiFormHandler.createSectionItems(currentEntryCount, wordSectionFormData, formStateManager)
        }
        return list
    }

    private fun modifyItemList(action: (MutableList<BaseItem>) -> Unit) {
        val currentList = (_formItems.value ?: mutableListOf()).toMutableList()
        action(currentList)
        _formItems.postValue(currentList)
    }

    fun addItemAtPosition(item: BaseItem, position: Int) {
        modifyItemList { it.add(position, item) }
    }

    fun removeItemAtPosition(position: Int) {
        modifyItemList { it.removeAt(position) }
    }

    fun updateItemAtPosition(item: BaseItem, position: Int) {
        modifyItemList { it[position] = item }
    }

    fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int){
        val currentList: MutableList<BaseItem> = (_formItems.value ?: mutableListOf<BaseItem>()).toMutableList()
        val countMap: Map<String, Int> = formStateManager.getEntryChildrenCountMap()
        val childrenCount = countMap[entryLabelItem.itemProperties.getIdentifier()]?: 0
        formStateManager.removeIdFromChildrenCountMap(entryLabelItem.itemProperties.getIdentifier())
        formStateManager.setCurrentEntryCount(entryLabelItem.itemProperties.section)
        currentList.subList(position, position + 1 + childrenCount).clear()
        val command: FormCommand = RemoveSectionCommand(dataHandler.wordEntryFormData, entryLabelItem.itemProperties.section)
        dataHandler.executeCommand(command)
        _formItems.postValue(currentList)

    }

    fun addItemEntryClicked(position: Int){
        val currentList: MutableList<BaseItem> = (_formItems.value ?: mutableListOf()).toMutableList()
        currentList.addAll(position, createSectionList())
        _formItems.postValue(currentList) // Update the LiveData
    }

    // Base Text, Primary Text, Kana, english, dictionaryEntry Note. component Entry Note
    fun updateInputTextValue(inputTextItem: InputTextItem, textValue: String, position: Int) {
        val updatedItem = inputTextItem.copy(inputTextValue = textValue)
        val formCommand: FormCommand = createUpdateFormCommand(inputTextItem, updatedItem)
        dataHandler.executeCommand(formCommand)
        updateItemAtPosition(updatedItem, position)
    }

    fun getInputTextValue(inputTextItem: InputTextItem): String {
        return inputTextItem.inputTextValue
    }

    // Word Class
    fun updateMainClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int): Boolean{
        val selectedMainClass: MainClassContainer = mainClassData[selectionPosition]
        if( wordClassItem.chosenMainClassId != selectedMainClass.id) {
            val subClassList: List<SubClassContainer> = subClassMapData[selectedMainClass] ?: emptyList()
            val updateWordClassItem: WordClassItem = wordClassItem.copy(chosenMainClassId = selectedMainClass.id, currentSubClassData = subClassList)
            val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, updateWordClassItem)
            dataHandler.executeCommand(command)
            updateItemAtPosition(updateWordClassItem, position)
            return true
        }
        return false
    }

    fun updateSubClassId(wordClassItem: WordClassItem, selectedPosition: Int, position: Int){
        val subClassId: Long = wordClassItem.currentSubClassData[selectedPosition].id
        val updateWordClassItem: WordClassItem = wordClassItem.copy(chosenSubClassId = subClassId)
        val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, updateWordClassItem)
        dataHandler.executeCommand(command)
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

    fun addTextItemClicked(action: ButtonAction.AddItem, position: Int){
        val inputTextType = action.inputType
        val inputTextItem = InputTextItem(inputTextType, itemProperties = ItemProperties())
        val command: FormCommand = createAddFormCommand(inputTextItem)
        dataHandler.executeCommand(command)
        addItemAtPosition(inputTextItem, position)
    }

    fun addChildTextItemClicked(action: ButtonAction.AddChild, position: Int){
        val inputTextType = action.inputTextType
        val parent: EntryLabelItem = action.entryLabelItem
        formStateManager.incrementChildrenCount(parent.itemProperties.getIdentifier())
        val inputTextItem = InputTextItem(inputTextType, itemProperties = ItemSectionProperties(section = parent.itemProperties.section))
        val command: FormCommand = createAddFormCommand(inputTextItem)
        dataHandler.executeCommand(command)
        addItemAtPosition(inputTextItem, position)
    }

    fun getInputTextType(inputTextItem: InputTextItem): InputTextType {
        return inputTextItem.inputTextType
    }

    fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int){
        val currentSectionCount = formStateManager.getCurrentEntryCount()
        if(currentSectionCount < entryLabelItem.itemProperties.section){
            val itemProp: ItemSectionProperties = entryLabelItem.itemProperties
            val updateEntryLabelItem: EntryLabelItem = entryLabelItem.copy(itemProperties = ItemSectionProperties(itemProp.getTableId(), itemProp.getId(), currentSectionCount))
            formStateManager.incrementEntryCount()
            updateItemAtPosition(updateEntryLabelItem, position)
        }
    }

    fun getWidgetName(namedItem: NamedItem): String {
        return namedItem.getDisplayText()
    }

    fun getLabelType (labelItem: LabelItem): LabelType {
        return labelItem.labelType
    }

    private fun createUpdateFormCommand(originalItem: InputTextItem, updatedItem: InputTextItem): FormCommand {
        val section = (originalItem.itemProperties as? ItemSectionProperties)?.section() ?: -1
        val data = dataHandler.wordEntryFormData

        return when (originalItem.inputTextType) {
            InputTextType.PRIMARY_TEXT -> UpdatePrimaryTextCommand(data, updatedItem)
            InputTextType.MEANING -> UpdateMeaningItemCommand(data, section, updatedItem)
            InputTextType.KANA -> UpdateKanaItemCommand(data, section, updatedItem)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> UpdateEntryNoteItemCommand(data, updatedItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> UpdateComponentNoteItemCommand(data, section, updatedItem)
        }
    }

    private fun createAddFormCommand(textItem: InputTextItem): FormCommand {
        val section = (textItem.itemProperties as? ItemSectionProperties)?.section() ?: -1
        val data = dataHandler.wordEntryFormData

        return when (textItem.inputTextType) {
            InputTextType.KANA -> AddKanaItemCommand(data, section, textItem)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> AddEntryNoteItemCommand(data, textItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> AddComponentNoteItemCommand(data, section, textItem)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed")
        }
    }
}
