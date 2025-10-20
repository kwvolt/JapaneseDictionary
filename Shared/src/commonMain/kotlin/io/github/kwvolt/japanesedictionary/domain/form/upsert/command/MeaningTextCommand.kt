package io.github.kwvolt.japanesedictionary.domain.form.upsert.command
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData


class UpdateMeaningItemCommand(
    wordEntryFormData: WordEntryFormData,
    sectionIndex: Int,
    private val newMeaning: TextItem
) : UpdateComponentSectionCommand(wordEntryFormData, sectionIndex) {

    override fun transform(section: WordSectionFormData): WordSectionFormData {
        return section.copy(meaningInput = newMeaning)
    }
}