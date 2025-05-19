package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData


class AddKanaItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newKanaItem: InputTextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(kanaInputMap = section.kanaInputMap.put(newKanaItem.itemProperties.getIdentifier(), newKanaItem))
    }
}

class UpdateKanaItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newKanaItem: InputTextItem,
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