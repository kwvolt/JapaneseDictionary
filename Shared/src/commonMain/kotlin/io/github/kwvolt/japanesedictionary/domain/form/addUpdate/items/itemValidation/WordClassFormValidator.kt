package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem

class WordClassFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<WordClassFormUIItem> {
    override fun getValidationKey(item: WordClassFormUIItem): ItemKey {
        return ItemKey.DataItem(item.wordClassItem.itemProperties.getIdentifier())
    }

    override suspend fun validate(
        item: WordClassFormUIItem,
        formData: WordEntryFormData
    ): Pair<ItemKey, ValidationResult<Unit>> {
        return Pair(getValidationKey(item), wordFormService.validateWordClass(item.wordClassItem))
    }
}