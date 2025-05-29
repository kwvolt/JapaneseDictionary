package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemValidation

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem

class StaticLabelFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<StaticLabelFormUIItem> {

    override fun getValidationKey(item: StaticLabelFormUIItem): ValidationKey {
        val props = item.itemProperties as ItemSectionProperties
        return ValidationKey.FormItem(FormKeys.kanaLabel(props.getSectionIndex()))
    }

    override suspend fun validate(item: StaticLabelFormUIItem, formData: WordEntryFormData): Pair<ValidationKey, ValidationResult<Unit>> {
        val props = item.itemProperties as ItemSectionProperties
        val sectionIndex = props.getSectionIndex()
        val kanaInputMap = formData.wordSectionMap[sectionIndex]?.kanaInputMap
        return Pair(getValidationKey(item), kanaInputMap?.let { wordFormService.validateKanaListNotEmpty(sectionIndex, it) } ?: ValidationResult.NotFound)
    }
}