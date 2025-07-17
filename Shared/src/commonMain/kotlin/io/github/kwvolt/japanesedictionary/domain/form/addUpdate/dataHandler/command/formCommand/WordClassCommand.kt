package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class UpdateWordClassCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newWordClassItem: WordClassItem
) : FormCommand<Unit> {

    private val oldWordClassItem: WordClassItem = wordEntryFormData.wordClassInput

    override fun execute(): CommandReturn<Unit> {
        return CommandReturn(wordEntryFormData.copy(
            wordClassInput = newWordClassItem
        ), Unit)
    }
    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            wordClassInput = oldWordClassItem
        )
    }
}