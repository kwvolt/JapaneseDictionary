package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

class UpdateWordClassCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newWordClassItem: WordClassItem
) : FormCommand {

    private val oldWordClassItem: WordClassItem = wordEntryFormData.wordClassInput

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordClassInput = newWordClassItem
        )
    }
    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordClassInput = oldWordClassItem
        )
    }
}