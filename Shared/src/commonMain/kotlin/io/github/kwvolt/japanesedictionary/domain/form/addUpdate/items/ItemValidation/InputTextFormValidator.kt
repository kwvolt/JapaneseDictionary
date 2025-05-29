package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties

class InputTextFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<InputTextFormUIItem> {

    override fun getValidationKey(item: InputTextFormUIItem): ValidationKey {
        return ValidationKey.DataItem(item.inputTextItem.itemProperties.getIdentifier())
    }

    override suspend fun validate(item: InputTextFormUIItem, formData: WordEntryFormData): Pair<ValidationKey, ValidationResult<Unit>> {
        val inputItem = item.inputTextItem
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