package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties

class InputTextFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<InputTextFormUIItem> {

    override fun getValidationKey(item: InputTextFormUIItem): ItemKey {
        return ItemKey.DataItem(item.textItem.itemProperties.getIdentifier())
    }

    override suspend fun validate(item: InputTextFormUIItem, formData: WordEntryFormData): Pair<ItemKey, ValidationResult<Unit>> {
        val inputItem = item.textItem
        val result =  when (inputItem.inputTextType) {
            InputTextType.PRIMARY_TEXT -> wordFormService.validatePrimaryText(inputItem)
            InputTextType.MEANING -> wordFormService.validateMeaning(inputItem)
            InputTextType.KANA -> {
                val props = inputItem.itemProperties as ItemSectionProperties
                val kanaList = formData.wordSectionMap[props.getSectionIndex()]?.getKanaInputMapAsList()
                if (kanaList != null) {
                    wordFormService.validateKana(inputItem, kanaList)
                }
                else{
                    ValidationResult.NotFound
                }
            }
            InputTextType.ENTRY_NOTE_DESCRIPTION ->
                wordFormService.validateNotes(inputItem, formData.getEntryNoteMapAsList())

            InputTextType.SECTION_NOTE_DESCRIPTION -> {
                val props = inputItem.itemProperties as ItemSectionProperties
                val noteList = formData.wordSectionMap[props.getSectionIndex()]?.getComponentNoteInputMapAsList()
                if (noteList != null) {
                    wordFormService.validateNotes(inputItem, noteList)
                }
                else {
                    ValidationResult.NotFound
                }
            }
        }
        return Pair(getValidationKey(item), result)
    }
}