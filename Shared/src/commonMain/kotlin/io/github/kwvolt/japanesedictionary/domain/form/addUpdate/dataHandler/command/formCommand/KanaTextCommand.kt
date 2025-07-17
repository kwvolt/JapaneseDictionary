package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData


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