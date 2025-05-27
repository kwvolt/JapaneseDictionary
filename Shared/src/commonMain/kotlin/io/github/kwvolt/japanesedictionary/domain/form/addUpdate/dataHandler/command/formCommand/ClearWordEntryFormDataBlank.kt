package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

class ClearWordEntryFormDataBlankCommand(val wordEntryFormData: WordEntryFormData): FormCommand {

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(
            entryNoteInputMap = wordEntryFormData.entryNoteInputMap.filterNonBlankInputs(),
            wordSectionMap = wordEntryFormData.wordSectionMap
                .mapValues { (_, section) -> section.cleaned() }
                .filterValues { it.isValid() }
                .toPersistentMap()
        )
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData
    }

    // Extension to filter out blank InputTextItems
    private fun Map<String, InputTextItem>.filterNonBlankInputs(): PersistentMap<String, InputTextItem> =
        this.filterValues { it.inputTextValue.trim().isNotEmpty() || it.itemProperties.getTableId() != WordEntryTable.UI.asString() }.toPersistentMap()

    // Extension to clean a single WordSectionFormData
    private fun WordSectionFormData.cleaned(): WordSectionFormData =
        this.copy(
            kanaInputMap = this.kanaInputMap.filterNonBlankInputs(),
            sectionNoteInputMap = this.sectionNoteInputMap.filterNonBlankInputs()
        )

    // Extension to determine if a section is still valid after cleaning
    private fun WordSectionFormData.isValid(): Boolean =
        this.meaningInput.inputTextValue.trim().isNotEmpty() &&
                this.kanaInputMap.isNotEmpty()
}