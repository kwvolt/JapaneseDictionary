package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

class AddSectionNoteItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newComponentNoteItem: TextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(
            noteInputMap = section.noteInputMap.put(
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
            noteInputMap = section.noteInputMap.put(
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
        return section.copy(noteInputMap = section.noteInputMap.remove(sectionNoteIdentifier))
    }
}