package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

interface FormCommand {
    fun execute(): WordEntryFormData
    fun undo(): WordEntryFormData
}