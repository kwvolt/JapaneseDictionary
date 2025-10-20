package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

abstract class UpdateComponentSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int
) : FormCommand {

    private val originalSection: WordSectionFormData =
        wordEntryFormData.wordSectionMap[sectionIndex]
            ?: error("No section found at index $sectionIndex")

    private val updatedSection: WordSectionFormData by lazy {
        transform(originalSection)
    }

    abstract fun transform(section: WordSectionFormData): WordSectionFormData

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordSectionMap = wordEntryFormData.wordSectionMap.put(sectionIndex, updatedSection)
        )
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordSectionMap = wordEntryFormData.wordSectionMap.put(sectionIndex, originalSection)
        )
    }
}