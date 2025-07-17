package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem

interface FormItemValidator<T : FormUIItem> {
    fun getValidationKey(item: T): ItemKey
    suspend fun validate(item: T, formData: WordEntryFormData): Pair<ItemKey, ValidationResult<Unit>>
}