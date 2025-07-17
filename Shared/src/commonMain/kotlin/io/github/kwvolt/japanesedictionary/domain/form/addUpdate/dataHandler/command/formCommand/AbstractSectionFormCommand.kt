package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

abstract class UpdateComponentSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int
) : FormCommand<Unit> {

    private val originalSection: WordSectionFormData =
        wordEntryFormData.wordSectionMap[sectionIndex]
            ?: error("No section found at index $sectionIndex")

    private val updatedSection: WordSectionFormData by lazy {
        transform(originalSection)
    }

    abstract fun transform(section: WordSectionFormData): WordSectionFormData

    override fun execute(): CommandReturn<Unit> {
        return CommandReturn(wordEntryFormData.copy(
            wordSectionMap = wordEntryFormData.wordSectionMap.put(sectionIndex, updatedSection)
        ), Unit)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordSectionMap = wordEntryFormData.wordSectionMap.put(sectionIndex, originalSection)
        )
    }
}