package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

class AddSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newComponentNoteItem: InputTextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(sectionNoteInputMap = section.sectionNoteInputMap.put(newComponentNoteItem.itemProperties.getIdentifier(), newComponentNoteItem))
    }
}

class UpdateSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newComponentNoteItem: InputTextItem,
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(sectionNoteInputMap = section.sectionNoteInputMap.put(newComponentNoteItem.itemProperties.getIdentifier(), newComponentNoteItem))
    }
}

class RemoveSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val sectionNoteIdentifier: String
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {
    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(sectionNoteInputMap = section.sectionNoteInputMap.remove(sectionNoteIdentifier))
    }
}