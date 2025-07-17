package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

interface FormCommand <T>  {
    fun execute(): CommandReturn<T>
    fun undo(): WordEntryFormData
}

data class CommandReturn <T> (val wordEntryFormData: WordEntryFormData, val value: T)