package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class UpdatePrimaryTextCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newPrimaryTextItem: TextItem,
) : FormCommand<Unit> {

    private val oldPrimaryTextItem: TextItem = wordEntryFormData.primaryTextInput.copy()

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