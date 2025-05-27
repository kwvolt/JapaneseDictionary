package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage

interface UiFormHandlerInterface {
    fun createUIList(wordEntryFormData: WordEntryFormData, formSectionManager: FormSectionManager, errors: Map<ValidationKey, ErrorMessage> = emptyMap()): List<BaseItem>

    fun createSectionItems(sectionKey: Int, section: WordSectionFormData, formSectionManager: FormSectionManager, errors: Map<ValidationKey, ErrorMessage> = emptyMap()): List<BaseItem>
}