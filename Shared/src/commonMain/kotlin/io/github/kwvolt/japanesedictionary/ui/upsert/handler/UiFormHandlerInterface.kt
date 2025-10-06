package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage

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