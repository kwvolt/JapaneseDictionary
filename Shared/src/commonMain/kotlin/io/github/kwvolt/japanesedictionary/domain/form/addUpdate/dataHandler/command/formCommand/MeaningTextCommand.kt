package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData


class UpdateMeaningItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newMeaning: InputTextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(meaningInput = newMeaning)
    }
}