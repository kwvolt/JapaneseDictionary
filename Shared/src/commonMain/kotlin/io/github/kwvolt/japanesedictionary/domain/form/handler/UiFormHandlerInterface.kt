package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem

interface UiFormHandlerInterface {
    fun createUIList(wordEntryFormData: WordEntryFormData, formSectionManager: FormSectionManager): List<BaseItem>

    fun createSectionItems(sectionKey: Int, section: WordSectionFormData, formSectionManager: FormSectionManager): List<BaseItem>
}