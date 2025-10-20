package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

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