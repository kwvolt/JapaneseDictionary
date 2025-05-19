package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData

class UpdatePrimaryTextCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newPrimaryTextItem: InputTextItem,
) : FormCommand {

    private val oldPrimaryTextItem: InputTextItem = wordEntryFormData.primaryTextInput

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