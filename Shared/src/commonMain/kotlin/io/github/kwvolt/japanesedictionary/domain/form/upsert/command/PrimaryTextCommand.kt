package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

class UpdatePrimaryTextCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newPrimaryTextItem: TextItem,
) : FormCommand {

    private val oldPrimaryTextItem: TextItem = wordEntryFormData.primaryTextInput.copy()

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(
            primaryTextInput = newPrimaryTextItem
        )
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            primaryTextInput = oldPrimaryTextItem
        )
    }
}