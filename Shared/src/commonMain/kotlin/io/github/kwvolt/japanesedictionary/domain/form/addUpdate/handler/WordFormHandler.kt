package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidationKey
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
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable

class WordFormHandler (
    private val dataHandler: FormCommandManager,
    private val formSectionManager: FormSectionManager,
    private val wordUiFormHandler: UiFormHandlerInterface
){
    private val originalItemIds: Set<GenericItemProperties> = dataHandler.wordEntryFormData.getDeletableItemIds()

    fun getWordEntryFormData(): WordEntryFormData{
        return dataHandler.wordEntryFormData
    }

    fun generateFormList(errors: Map<ValidationKey, ErrorMessage> = emptyMap()): List<BaseItem> {
        return wordUiFormHandler.createUIList(dataHandler.wordEntryFormData, formSectionManager, errors)
    }

    // get those that were present in the original but was then removed
    fun getDeletedItemIds(): Set<GenericItemProperties> {
        val currentIds = dataHandler.wordEntryFormData.getDeletableItemIds()
        return originalItemIds - currentIds
    }

    fun redo() = applyHistoryAction { dataHandler.redo() }
    fun undo() = applyHistoryAction { dataHandler.undo() }

    private fun applyHistoryAction(action: () -> Unit): List<BaseItem> {
        action()
        formSectionManager.setCurrentEntryCount(1)
        return wordUiFormHandler.createUIList(dataHandler.wordEntryFormData, formSectionManager)
    }

    fun setUndoRedoListener(undoRedoStateListener: UndoRedoStateListener){
        dataHandler.setUndoRedoListener(undoRedoStateListener)
    }

    fun updateItemCommand(item: InputTextItem, value: String) {
        val updated = item.copy(inputTextValue = value)
        dataHandler.executeCommand(createUpdateItemFormCommand(item, updated))
    }

    fun addItemCommand(textItem: InputTextItem){
        dataHandler.executeCommand(createAddItemFormCommand(textItem))
    }

    fun removeItemCommand(textItem: InputTextItem){
        dataHandler.executeCommand(createRemoveItemFormCommand(textItem))
    }

    fun createNewSection(): List<BaseItem> {

        val sectionIndex = formSectionManager.getThenIncrementEntrySectionId()

        val command = AddSectionCommand(dataHandler.wordEntryFormData, sectionIndex)
        dataHandler.executeCommand(command)

        val sectionDataList = dataHandler.wordEntryFormData.wordSectionMap[sectionIndex]
        return sectionDataList?.let {
            wordUiFormHandler.createSectionItems(sectionIndex, it, formSectionManager)
        } ?: emptyList()
    }

    fun removeSection(
        currentList: List<BaseItem>,
        sectionId: Int,
        sectionCount: Int,
        position: Int
    ): List<BaseItem> {
        formSectionManager.setCurrentEntryCount(sectionCount)
        val childrenCount = formSectionManager.getChildrenCount(sectionId)
        formSectionManager.removeSection(sectionId)
        val resultList = currentList.toMutableList().apply {
            subList(position, position + childrenCount + 1).clear() // Remove section and its children
        }
        val updated = updateEntryLabelsInList(resultList, position)


        val command: FormCommand = RemoveSectionCommand(dataHandler.wordEntryFormData, sectionId)
        dataHandler.executeCommand(command)
        return updated
    }

    private fun updateEntryLabelsInList(list: MutableList<BaseItem>, position: Int): List<BaseItem> {
        for (i in position until list.size) {
            val item = list[i]
            if (item is EntryLabelItem) {
                // Modify the item directly in the list
                val updatedItem = item.copy(sectionCount = formSectionManager.getCurrentSectionCount())
                formSectionManager.incrementCurrentSectionCount()
                list[i] = updatedItem
            }
        }
        return list
    }

    fun updateWordClassId(wordClassItem: WordClassItem){
        val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, wordClassItem)
        dataHandler.executeCommand(command)
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

    private fun createRemoveItemFormCommand(textItem: InputTextItem): FormCommand{
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        val itemId = textItem.itemProperties.getIdentifier()
        return when (textItem.inputTextType) {
            InputTextType.KANA -> RemoveKanaItemCommand(data, section, itemId)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> RemoveEntryNoteItemCommand(data, itemId)
            InputTextType.SECTION_NOTE_DESCRIPTION -> RemoveSectionNoteItemCommand(data, section, itemId)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed")
        }
    }

    private fun WordEntryFormData.getDeletableItemIds(): Set<GenericItemProperties> {
        val ids = mutableSetOf<GenericItemProperties>()

        // Primary Text
        if (primaryTextInput.itemProperties.getTableId() != WordEntryTable.UI.asString()) {
            ids.add(primaryTextInput.itemProperties)
        }

        // Entry Notes
        entryNoteInputMap.values
            .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
            .mapTo(ids) { it.itemProperties }

        // Sections
        wordSectionMap.values.forEach { section ->
            section.kanaInputMap.values
                .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
                .mapTo(ids) { it.itemProperties }

            section.sectionNoteInputMap.values
                .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
                .mapTo(ids) { it.itemProperties }

            if (section.meaningInput.itemProperties.getTableId() != WordEntryTable.UI.asString()) {
                ids.add(section.meaningInput.itemProperties)
            }
        }
        return ids
    }
}


