package io.github.kwvolt.japanesedictionary.domain.form.upsert.handler
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.AddSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.AddEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.AddKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.AddSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.RemoveEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.RemoveKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.RemoveSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.RemoveSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateConjugationTemplateCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateMeaningItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdatePrimaryTextCommand
import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.UpdateWordClassCommand
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ConjugationTemplateItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemSectionProperties

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

        val newItem = formItemManager.createNewTextItem(inputTextType, genericItemProperties = itemProperties)

        dataHandler.executeCommand(createAddItemFormCommand(newItem))

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
        val sectionId: Int = formItemManager.getThenIncrementEntrySectionId()
        val command = AddSectionCommand(dataHandler.wordEntryFormData, sectionId, formItemManager)
        dataHandler.executeCommand(command)
        return sectionId
    }

    fun removeSection(
        sectionId: Int,
    ) {
        val command: FormCommand = RemoveSectionCommand(dataHandler.wordEntryFormData, sectionId)
        dataHandler.executeCommand(command)
    }

    fun updateConjugationTemplateIdItemCommand(conjugationTemplateItem: ConjugationTemplateItem, conjugationTemplateId: Long):ConjugationTemplateItem {
        val updated: ConjugationTemplateItem = conjugationTemplateItem.copy(chosenConjugationTemplateId = conjugationTemplateId)
        UpdateConjugationTemplateCommand(dataHandler.wordEntryFormData, updated)
        return updated
    }

    fun updateConjugationTemplateKanaItemCommand(conjugationTemplateItem: ConjugationTemplateItem, kanaId: Long):ConjugationTemplateItem {
        val updated: ConjugationTemplateItem = conjugationTemplateItem.copy(kanaId = kanaId)
        UpdateConjugationTemplateCommand(dataHandler.wordEntryFormData, updated)
        return updated
    }


    fun updateSubClassWordClassItemCommand(wordClassItem: WordClassItem, subClassContainer: SubClassContainer): WordClassItem {
        val updated: WordClassItem = wordClassItem.copy(chosenSubClass = subClassContainer)
        updateWordClassId(updated)
        return updated
    }


    fun updateMainClassWordClassItemCommand(
        wordClassItem: WordClassItem,
        mainClassContainer: MainClassContainer,
        subClassContainer: SubClassContainer? = null
    ): WordClassItem {
        val updated = wordClassItem.copy(chosenMainClass = mainClassContainer)
            .let {
                if (subClassContainer != null) {
                    it.copy(chosenSubClass = subClassContainer)
                } else it
            }
        updateWordClassId(updated)
        return updated
    }

    private fun updateWordClassId(wordClassItem: WordClassItem){
        val command = UpdateWordClassCommand(dataHandler.wordEntryFormData, wordClassItem)
        dataHandler.executeCommand(command)
    }


    private fun createUpdateItemFormCommand(original: TextItem, updated: TextItem): FormCommand {
        val section = (original.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        return when (original.inputTextType) {
            InputTextType.PRIMARY_TEXT -> UpdatePrimaryTextCommand(data, updated)
            InputTextType.MEANING -> UpdateMeaningItemCommand(data, section, updated)
            InputTextType.KANA -> UpdateKanaItemCommand(data, section, updated)
            InputTextType.DICTIONARY_NOTE_DESCRIPTION -> UpdateEntryNoteItemCommand(data, updated)
            InputTextType.SECTION_NOTE_DESCRIPTION -> UpdateSectionNoteItemCommand(data, section, updated)
        }
    }

    private fun createAddItemFormCommand(textItem: TextItem): FormCommand {
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        return when (textItem.inputTextType) {
            InputTextType.KANA -> AddKanaItemCommand(data, section, textItem)
            InputTextType.DICTIONARY_NOTE_DESCRIPTION -> AddEntryNoteItemCommand(data, textItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> AddSectionNoteItemCommand(data, section, textItem)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed").fillInStackTrace()
        }
    }

    private fun createRemoveItemFormCommand(textItem: TextItem): FormCommand{
        val section = (textItem.itemProperties as? ItemSectionProperties)?.getSectionIndex() ?: -1
        val data = dataHandler.wordEntryFormData
        val itemId = textItem.itemProperties.getIdentifier()
        return when (textItem.inputTextType) {
            InputTextType.KANA -> RemoveKanaItemCommand(data, section, itemId)
            InputTextType.DICTIONARY_NOTE_DESCRIPTION -> RemoveEntryNoteItemCommand(data, itemId)
            InputTextType.SECTION_NOTE_DESCRIPTION -> RemoveSectionNoteItemCommand(data, section, itemId)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed").fillInStackTrace()
        }
    }

    private fun WordEntryFormData.getDeletableItemIds(): Set<GenericItemProperties> {
        val ids = mutableSetOf<GenericItemProperties>()

        // Primary Text
        if (primaryTextInput.itemProperties.getTableId() != WordEntryTable.UI.asString()) {
            ids.add(primaryTextInput.itemProperties)
        }

        // Entry Notes
        noteInputMap.values
            .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
            .mapTo(ids) { it.itemProperties }

        // Sections
        wordSectionMap.values.forEach { section ->
            section.kanaInputMap.values
                .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
                .mapTo(ids) { it.itemProperties }

            section.noteInputMap.values
                .filter { it.itemProperties.getTableId() != WordEntryTable.UI.asString() }
                .mapTo(ids) { it.itemProperties }

            if (section.meaningInput.itemProperties.getTableId() != WordEntryTable.UI.asString()) {
                ids.add(section.meaningInput.itemProperties)
            }
        }
        return ids
    }
}


