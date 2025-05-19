package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.RemoveEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.RemoveKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.RemoveSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.RemoveSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateMeaningItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdatePrimaryTextCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateWordClassCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem

class WordFormHandler (
    private val dataHandler: FormCommandManager,
    private val formSectionManager: FormSectionManager,
    private val wordUiFormHandler: UiFormHandlerInterface
){
    fun getWordEntryFormData(): WordEntryFormData{
        return dataHandler.wordEntryFormData
    }

    fun createInitialForm(): List<BaseItem> {
        return wordUiFormHandler.createUIList(dataHandler.wordEntryFormData, formSectionManager)
    }

    fun createNewSection(): List<BaseItem> {
        val sectionIndex = formSectionManager.getEntrySectionId()
        val command = AddSectionCommand(dataHandler.wordEntryFormData, sectionIndex)
        dataHandler.executeCommand(command)
        val sectionData = dataHandler.wordEntryFormData.wordSectionMap[sectionIndex]
        return sectionData?.let {
            wordUiFormHandler.createSectionItems(sectionIndex, it, formSectionManager)
        } ?: emptyList()
    }

    fun updateItemCommand(item: InputTextItem, value: String) {
        val updated = item.copy(inputTextValue = value)
        dataHandler.executeCommand(createUpdateItemFormCommand(item, updated))
    }

    fun addItemCommand(textItem: InputTextItem){
        dataHandler.executeCommand(createAddItemFormCommand(textItem))
    }

    fun removeItemCommand(textItem: InputTextItem){
        val removeCommand: FormCommand? =  createRemoveItemFormCommand(textItem)
        if(removeCommand != null){
            dataHandler.executeCommand(removeCommand)
        }
    }

    fun removeSection(
        currentList: List<BaseItem>,
        sectionId: Int,
        sectionCount: Int,
        position: Int
    ): List<BaseItem> {
        val childrenCount = formSectionManager.getChildrenCount(sectionId)
        formSectionManager.removeSection(sectionId)
        val newList = currentList.toMutableList().apply {
            subList(position, position + 1 + childrenCount).clear()
        }
        formSectionManager.setCurrentEntryCount(sectionCount)
        val command: FormCommand = RemoveSectionCommand(dataHandler.wordEntryFormData, sectionId)
        dataHandler.executeCommand(command)
        return newList
    }

    fun updateWordClassId(wordClassItem: WordClassItem){
        val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, wordClassItem)
        dataHandler.executeCommand(command)
    }

    fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem): EntryLabelItem? {
        val currentSectionCount = formSectionManager.getCurrentSectionCount()
        if(currentSectionCount < entryLabelItem.sectionCount){
            val updateEntryLabelItem: EntryLabelItem = entryLabelItem.copy(sectionCount = currentSectionCount)
            formSectionManager.incrementCurrentSectionCount()
            return updateEntryLabelItem
        }
        return null
    }

    private fun createUpdateItemFormCommand(original: InputTextItem, updated: InputTextItem): FormCommand {
        val section = (original.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        return when (original.inputTextType) {
            InputTextType.PRIMARY_TEXT -> UpdatePrimaryTextCommand(data, updated)
            InputTextType.MEANING -> UpdateMeaningItemCommand(data, section, updated)
            InputTextType.KANA -> UpdateKanaItemCommand(data, section, updated)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> UpdateEntryNoteItemCommand(data, updated)
            InputTextType.SECTION_NOTE_DESCRIPTION -> UpdateSectionNoteItemCommand(data, section, updated)
        }
    }

    private fun createAddItemFormCommand(textItem: InputTextItem): FormCommand {
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        if (section != -1) {formSectionManager.incrementChildrenCount(section)}
        return when (textItem.inputTextType) {
            InputTextType.KANA -> AddKanaItemCommand(data, section, textItem)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> AddEntryNoteItemCommand(data, textItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> AddSectionNoteItemCommand(data, section, textItem)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed")
        }
    }

    private fun createRemoveItemFormCommand(textItem: InputTextItem): FormCommand?{
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        val itemId = textItem.itemProperties.getIdentifier()
        return when (textItem.inputTextType) {
            InputTextType.KANA -> RemoveKanaItemCommand(data, section, itemId)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> RemoveEntryNoteItemCommand(data, itemId)
            InputTextType.SECTION_NOTE_DESCRIPTION -> RemoveSectionNoteItemCommand(data, section, itemId)
            else -> null
        }
    }
}
