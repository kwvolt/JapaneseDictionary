package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import kotlinx.collections.immutable.PersistentMap


class AddSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val formItemManager: FormItemManager
) : FormCommand<Int> {

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap
    override fun execute(): CommandReturn<Int> {

        val (sectionIndex: Int, wordSectionFormData: WordSectionFormData) = WordSectionFormData.buildDefault(formItemManager)

        val updatedMap = originalSectionMap.put(sectionIndex, wordSectionFormData)

        return CommandReturn(wordEntryFormData.copy(wordSectionMap = updatedMap), sectionIndex)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}

class RemoveSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int,
) : FormCommand<Unit> {

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap

    override fun execute(): CommandReturn<Unit> {
        val updatedMap = originalSectionMap.remove(sectionIndex)
        return CommandReturn(wordEntryFormData.copy(wordSectionMap = updatedMap), Unit)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}