package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem

class StaticLabelFormValidator(
    private val wordFormService: WordFormService
) : FormItemValidator<StaticLabelFormUIItem> {

    override fun getValidationKey(item: StaticLabelFormUIItem): ItemKey {
        val props = item.itemProperties as ItemSectionProperties
        return ItemKey.FormItem(FormKeys.kanaLabel(props.getSectionIndex()))
    }

    override suspend fun validate(item: StaticLabelFormUIItem, formData: WordEntryFormData): Pair<ItemKey, ValidationResult<Unit>> {
        val props = item.itemProperties as ItemSectionProperties
        val sectionIndex = props.getSectionIndex()
        val kanaInputMap = formData.wordSectionMap[sectionIndex]?.kanaInputMap
        return Pair(getValidationKey(item), kanaInputMap?.let { wordFormService.validateKanaListNotEmpty(sectionIndex, it) } ?: ValidationResult.NotFound)
    }
}