package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem

class WordClassFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<WordClassFormUIItem> {
    override fun getValidationKey(item: WordClassFormUIItem): ValidationKey {
        return ValidationKey.DataItem(item.wordClassItem.itemProperties.getIdentifier())
    }

    override suspend fun validate(
        item: WordClassFormUIItem,
        formData: WordEntryFormData
    ): Pair<ValidationKey, ValidationResult<Unit>> {
        return Pair(getValidationKey(item), wordFormService.validateWordClass(item.wordClassItem))
    }
}