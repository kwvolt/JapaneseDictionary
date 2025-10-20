package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ConjugationTemplateItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem

class UpdateConjugationTemplateCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newConjugationTemplateItem: ConjugationTemplateItem
) : FormCommand {

    private val oldConjugationTemplateItem: ConjugationTemplateItem = wordEntryFormData.conjugationTemplateInput

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(
            conjugationTemplateInput = newConjugationTemplateItem
        )
    }
    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(
            conjugationTemplateInput = oldConjugationTemplateItem
        )
    }
}