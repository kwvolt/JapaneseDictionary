package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem

interface FormItemValidator<T : FormUIItem> {
    fun getValidationKey(item: T): ValidationKey
    suspend fun validate(item: T, formData: WordEntryFormData): Pair<ValidationKey, ValidationResult<Unit>>
}