package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage

interface UiFormHandlerInterface {
    fun createUIList(wordEntryFormData: WordEntryFormData, formItemManager: FormItemManager, errors: Map<ItemKey, ErrorMessage> = emptyMap()): List<BaseItem>

    fun createSectionItems(sectionKey: Int, section: WordSectionFormData, formItemManager: FormItemManager, errors: Map<ItemKey, ErrorMessage> = emptyMap()): List<BaseItem>
}