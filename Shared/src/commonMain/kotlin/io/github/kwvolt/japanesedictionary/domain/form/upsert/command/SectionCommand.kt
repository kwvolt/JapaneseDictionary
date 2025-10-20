package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import kotlinx.collections.immutable.PersistentMap


class AddSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionId: Int,
    private val formItemManager: FormItemManager
) : FormCommand {
    constructor(wordEntryFormData: WordEntryFormData, formItemManager: FormItemManager):
            this(wordEntryFormData, formItemManager.getThenIncrementEntrySectionId(), formItemManager)

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap
    override fun execute(): WordEntryFormData {

        val wordSectionFormData: WordSectionFormData = WordSectionFormData.buildDefault(sectionId, formItemManager)

        val updatedMap = originalSectionMap.put(sectionId, wordSectionFormData)

        return wordEntryFormData.copy(wordSectionMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}

class RemoveSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int,
) : FormCommand {

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap

    override fun execute(): WordEntryFormData {
        val updatedMap = originalSectionMap.remove(sectionIndex)
        return wordEntryFormData.copy(wordSectionMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}