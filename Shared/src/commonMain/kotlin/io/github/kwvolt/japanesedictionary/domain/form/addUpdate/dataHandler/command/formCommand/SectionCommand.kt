package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf


class AddSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int
) : FormCommand {

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap

    override fun execute(): WordEntryFormData {
        // Create the new section
        val newMEANING = InputTextItem(InputTextType.MEANING, "", ItemSectionProperties(sectionId = sectionIndex))
        val newKana = InputTextItem(InputTextType.KANA, "", ItemSectionProperties(sectionId = sectionIndex))
        val newComponentNote = InputTextItem(InputTextType.SECTION_NOTE_DESCRIPTION, "", ItemSectionProperties(sectionId = sectionIndex))

        val newSection = WordSectionFormData(
            newMEANING,
            persistentMapOf(newKana.itemProperties.getIdentifier() to newKana),
            persistentMapOf(newComponentNote.itemProperties.getIdentifier() to newComponentNote)
        )

        // Start with the original map
        var updatedMap = originalSectionMap

        // Shift sections at and after sectionIndex one position up
        val maxIndex = originalSectionMap.keys.maxOrNull() ?: -1
        for (i in maxIndex downTo sectionIndex) {
            val section = updatedMap[i]
            if (section != null) {
                updatedMap = updatedMap.put(i + 1, section).remove(i)
            }
        }

        // Insert the new section
        updatedMap = updatedMap.put(sectionIndex, newSection)

        return wordEntryFormData.copy(wordSectionMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}

class RemoveSectionCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val sectionIndex: Int
) : FormCommand {

    private val originalSectionMap: PersistentMap<Int, WordSectionFormData> = wordEntryFormData.wordSectionMap

    override fun execute(): WordEntryFormData {
        var updatedMap = originalSectionMap.remove(sectionIndex)

        val maxIndex = originalSectionMap.keys.maxOrNull() ?: -1
        for (i in sectionIndex + 1..maxIndex) {
            val section = updatedMap[i]
            if (section != null) {
                updatedMap = updatedMap.put(i - 1, section).remove(i)
            }
        }

        return wordEntryFormData.copy(wordSectionMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(wordSectionMap = originalSectionMap)
    }
}