package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class AddSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newComponentNoteItem: TextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(
            sectionNoteInputMap = section.sectionNoteInputMap.put(
                newComponentNoteItem.itemProperties.getIdentifier(),
                newComponentNoteItem
            )
        )
    }
}

class UpdateSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newComponentNoteItem: TextItem,
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(
            sectionNoteInputMap = section.sectionNoteInputMap.put(
                newComponentNoteItem.itemProperties.getIdentifier(),
                newComponentNoteItem
            )
        )
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