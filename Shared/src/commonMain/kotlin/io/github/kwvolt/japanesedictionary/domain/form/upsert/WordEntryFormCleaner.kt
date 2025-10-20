package io.github.kwvolt.japanesedictionary.domain.form.upsert

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

object WordEntryFormCleaner {
    fun cleanWordEntryFormData(wordEntryFormData: WordEntryFormData): WordEntryFormData{
        return wordEntryFormData.copy(
            noteInputMap = wordEntryFormData.noteInputMap.filterNonBlankInputs(),
            wordSectionMap = wordEntryFormData.wordSectionMap
                .mapValues { (_, section) -> section.cleaned() }
                .filterValues { it.isValid() }
                .toPersistentMap()
        )
    }

    // Extension to filter out blank InputTextItems
    private fun Map<String, TextItem>.filterNonBlankInputs(): PersistentMap<String, TextItem> =
        this.filterValues { it.inputTextValue.trim().isNotEmpty() || it.itemProperties.getTableId() != WordEntryTable.UI.asString() }.toPersistentMap()

    // Extension to clean a single WordSectionFormData
    private fun WordSectionFormData.cleaned(): WordSectionFormData =
        this.copy(
            kanaInputMap = this.kanaInputMap.filterNonBlankInputs(),
            noteInputMap = this.noteInputMap.filterNonBlankInputs()
        )

    // Extension to determine if a section is still valid after cleaning
    private fun WordSectionFormData.isValid(): Boolean =
        this.meaningInput.inputTextValue.trim().isNotEmpty() ||
                this.kanaInputMap.isNotEmpty() ||
                this.noteInputMap.isNotEmpty()
}