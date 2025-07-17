package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
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
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable

class WordFormHandler (
    private val dataHandler: FormCommandManager,
){
    private val originalItemIds: Set<GenericItemProperties> = dataHandler.wordEntryFormData.getDeletableItemIds()

    fun getWordEntryFormData(): WordEntryFormData {
        return dataHandler.wordEntryFormData
    }
    // get those that were present in the original but was then removed
    fun getDeletedItemIds(): Set<GenericItemProperties> {
        val currentIds = dataHandler.wordEntryFormData.getDeletableItemIds()
        return originalItemIds - currentIds
    }

    fun redo(): Boolean = dataHandler.redo()
    fun undo(): Boolean = dataHandler.undo()

    fun setUndoRedoListener(undoRedoStateListener: UndoRedoStateListener){
        dataHandler.setUndoRedoListener(undoRedoStateListener)
    }

    fun addTextItemCommand(inputTextType: InputTextType, sectionId: Int? = null, formItemManager: FormItemManager): TextItem {
        // create itemProperties based on whether it belongs to a section
        val itemProperties = sectionId?.let { formItemManager.createItemSectionProperties(sectionId = it) }
            ?: formItemManager.createItemProperties()

        val newItem = formItemManager.createNewTextItem(inputTextType, itemProperties)

        // increment children count if applicable
        sectionId?.let { formItemManager.incrementChildrenCount(it) }

        dataHandler.executeCommand(createAddItemFormCommand(newItem, formItemManager))

        return newItem
    }

    fun updateTextItemCommand(item: TextItem, value: String): TextItem {
        val updated = item.copy(inputTextValue = value)
        dataHandler.executeCommand(createUpdateItemFormCommand(item, updated))
        return updated
    }

    fun removeItemCommand(textItem: TextItem){
        dataHandler.executeCommand(createRemoveItemFormCommand(textItem))
    }

    fun createNewSection(formItemManager: FormItemManager): Int {
        val command = AddSectionCommand(dataHandler.wordEntryFormData, formItemManager)
        val sectionId = dataHandler.executeCommand(command)
        return sectionId
    }

    fun removeSection(
        sectionId: Int,
    ) {
        val command: FormCommand<Unit> = RemoveSectionCommand(dataHandler.wordEntryFormData, sectionId)
        dataHandler.executeCommand(command)
    }

    fun updateSubClassWordClassItemCommand(wordClassItem: WordClassItem, subClassContainer: SubClassContainer): WordClassItem{
        val updated: WordClassItem = wordClassItem.copy(chosenSubClass = subClassContainer)
        updateWordClassId(updated)
        return updated
    }


    fun updateMainClassWordClassItemCommand(wordClassItem: WordClassItem, mainClassContainer: MainClassContainer): WordClassItem{
        val updated: WordClassItem = wordClassItem.copy(chosenMainClass = mainClassContainer)
        updateWordClassId(updated)
        return updated
    }

    private fun updateWordClassId(wordClassItem: WordClassItem){
        val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, wordClassItem)
        dataHandler.executeCommand(command)
    }


    private fun createUpdateItemFormCommand(original: TextItem, updated: TextItem): FormCommand<Unit> {
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

    private fun createAddItemFormCommand(textItem: TextItem, formItemManager: FormItemManager): FormCommand<Unit> {
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        if (section != -1) {formItemManager.incrementChildrenCount(section)}
        return when (textItem.inputTextType) {
            InputTextType.KANA -> AddKanaItemCommand(data, section, textItem)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> AddEntryNoteItemCommand(data, textItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> AddSectionNoteItemCommand(data, section, textItem)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed")
        }
    }

    private fun createRemoveItemFormCommand(textItem: TextItem): FormCommand<Unit>{
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


