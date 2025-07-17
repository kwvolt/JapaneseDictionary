package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData


class UpdateMeaningItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newMeaning: TextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(meaningInput = newMeaning)
    }
}