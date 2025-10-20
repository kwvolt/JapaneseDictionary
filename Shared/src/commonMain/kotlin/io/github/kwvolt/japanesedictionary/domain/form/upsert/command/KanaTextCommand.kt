package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData


class AddKanaItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newKanaItem: TextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(kanaInputMap = section.kanaInputMap.put(newKanaItem.itemProperties.getIdentifier(), newKanaItem))
    }
}

class UpdateKanaItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newKanaItem: TextItem,
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(kanaInputMap = section.kanaInputMap.put(newKanaItem.itemProperties.getIdentifier(), newKanaItem))
    }
}

class RemoveKanaItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val kanaId: String
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {
    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(kanaInputMap = section.kanaInputMap.remove(kanaId))
    }
}