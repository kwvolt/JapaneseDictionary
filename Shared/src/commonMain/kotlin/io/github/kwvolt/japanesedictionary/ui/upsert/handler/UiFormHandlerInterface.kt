package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ErrorMessage

interface UiFormHandlerInterface {
    fun createUIList(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        formSectionManager: FormSectionManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<DisplayItem>

    fun createSectionItems(
        sectionId: Int,
        section: WordSectionFormData,
        formItemManager: FormItemManager,
        formSectionManager: FormSectionManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<DisplayItem>
}