package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class UpdatePrimaryTextCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newPrimaryTextItem: TextItem,
) : FormCommand<Unit> {

    private val oldPrimaryTextItem: TextItem = wordEntryFormData.primaryTextInput

    override fun execute(): CommandReturn<Unit> {
        return CommandReturn(wordEntryFormData.copy(
            primaryTextInput = newPrimaryTextItem), Unit)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            primaryTextInput = oldPrimaryTextItem
        )
    }
}