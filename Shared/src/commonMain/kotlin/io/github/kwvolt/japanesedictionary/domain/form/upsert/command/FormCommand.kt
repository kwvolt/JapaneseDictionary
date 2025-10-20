package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

interface FormCommand  {
    fun execute(): WordEntryFormData
    fun undo(): WordEntryFormData
}