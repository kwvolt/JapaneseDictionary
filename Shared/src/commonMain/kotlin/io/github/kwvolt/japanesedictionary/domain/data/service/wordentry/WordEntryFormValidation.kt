package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationError
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.findDuplicateIdentifiers
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.data.validation.validateNotEmptyString
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem

class WordEntryFormValidation() {

    fun validatePrimaryText(primaryTextInput: TextItem): ValidationResult<ItemKey> = validatePrimaryTextBuilder(primaryTextInput).returnValidationResult()

    private fun validatePrimaryTextBuilder(primaryTextInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(primaryTextInput)
            .validateNotEmptyString()
            .validJapanese()
    }

    fun validateNotes(entryNoteInput: TextItem, entryNoteList: List<TextItem>): ValidationResult<ItemKey> {
        return  validateNotesBuilder(entryNoteInput).validateNoDuplicate(findDuplicateIdentifiers(entryNoteList)).returnValidationResult()
    }

    private fun validateNotesBuilder(entryNoteInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(entryNoteInput).validateNotEmptyString()
    }

    fun validateMeaningText(meaningTextInput: TextItem): ValidationResult<ItemKey> {
        return  validateMeaningTextBuilder(meaningTextInput).returnValidationResult()
    }

    private fun validateMeaningTextBuilder(meaningTextInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(meaningTextInput)
            .validateNotEmptyString()
    }

    fun validateKana(kanaInput: TextItem, kanaList: List<TextItem>): ValidationResult<ItemKey> {
        return  validateKanaBuilder(kanaInput).validateNoDuplicate(findDuplicateIdentifiers(kanaList)).returnValidationResult()
    }

    private fun validateKanaBuilder(kanaInput: TextItem): ValidationTextItemBuilder {
        return ValidationTextItemBuilder(kanaInput).apply {
            validateNotEmptyString()
            validKana()
        }
    }


    /**
     * Validates the entire WordEntryFormData.
     *
     * Expected to return:
     * - [ValidationResult.Success]
     * - [ValidationResult.InvalidInputMap]
     * - [ValidationResult.NotFound]
     * - [ValidationResult.UnknownError]
     *
     * Should NEVER return [ValidationResult.InvalidInput] (use InvalidInputMap instead).
     */
    fun validateForm(wordEntryFormData: WordEntryFormData): ValidationResult<Unit> {

        val validationErrors = mutableMapOf<ItemKey, List<ValidationError>>()

        // primary Text
        validationErrors.putIfNotEmpty(
            validatePrimaryTextBuilder(wordEntryFormData.primaryTextInput).returnErrorEntry()
        )

        // entry notes
        val entryNoteItemList: List<TextItem> = wordEntryFormData.entryNoteInputMap.values.toList()
        validationErrors.putAll(validateListNoDuplicate(entryNoteItemList) { validateNotesBuilder(it) })

        // Entry Section Validation
        val entrySectionMap = wordEntryFormData.wordSectionMap
        for (entrySection: WordSectionFormData in entrySectionMap.values) {

            // Meaning Text
            validationErrors.putIfNotEmpty(
                validateMeaningTextBuilder(entrySection.meaningInput).returnErrorEntry()
            )

            // Kana Text
            val kanaItemList: List<TextItem> = entrySection.getKanaInputMapAsList()
            validationErrors.putAll(validateListNoDuplicate(kanaItemList) { validateKanaBuilder(it) })

            // Section Note
            val sectionNoteItemList: List<TextItem> = entrySection.getComponentNoteInputMapAsList()
            validationErrors.putAll(validateListNoDuplicate(sectionNoteItemList) { validateNotesBuilder(it) })
        }


        if (validationErrors.isNotEmpty()) {
            return ValidationResult.InvalidInputMap(validationErrors)
        }
        return ValidationResult.Success(Unit)
    }

    private fun validateListNoDuplicate(
        itemList: List<TextItem>,
        block: (TextItem) -> ValidationTextItemBuilder
    ): Map<ItemKey, List<ValidationError>> {
        val validationErrors = mutableMapOf<ItemKey, List<ValidationError>>()
        val duplicates = findDuplicateIdentifiers(itemList)
        itemList.forEach {
            val builder = block(it).validateNoDuplicate(duplicates)
            validationErrors.putIfNotEmpty(builder.returnErrorEntry())
        }
        return validationErrors
    }

    private fun MutableMap<ItemKey, List<ValidationError>>.putIfNotEmpty(
        key: ItemKey,
        errors: List<ValidationError>
    ) {
        if (errors.isNotEmpty()) this[key] = errors
    }

    private fun MutableMap<ItemKey, List<ValidationError>>.putIfNotEmpty(result: Pair<ItemKey, List<ValidationError>>
    ) {
        this.putIfNotEmpty(result.first, result.second)
    }
}

private class ValidationTextItemBuilder(private val textItem: TextItem){
    private val itemText = textItem.inputTextValue.trim()
    private val itemKey: ItemKey.DataItem = ItemKey.DataItem(textItem.itemProperties.getIdentifier())
    private val validation = mutableListOf<ValidationError>()

    fun validJapanese(): ValidationTextItemBuilder{
        if (!validJapanese(itemText)) {
            validation.add(ValidationError(ValidationType.Japanese))
        }
        return this
    }

    fun validKana(): ValidationTextItemBuilder{
        if (!validKana(itemText)) {
            validation.add(ValidationError(ValidationType.Kana))
        }
        return this
    }

    fun validateNotEmptyString(): ValidationTextItemBuilder {
        if (!validateNotEmptyString(itemText)) {
            validation.add(ValidationError(ValidationType.Empty))
        }
        return this
    }

    fun validateNoDuplicate(duplicates: Set<String>): ValidationTextItemBuilder{
        if (itemKey.id in duplicates) {
            validation.add(ValidationError(ValidationType.Duplicate))
        }
        return this
    }

    fun validateGeneralBlock(block: (String, ItemKey.DataItem, MutableList<ValidationError>)-> Unit): ValidationTextItemBuilder{
        block(itemText, itemKey, validation)
        return this
    }

    fun returnValidationResult(): ValidationResult<ItemKey>{
        return if (validation.isEmpty()) ValidationResult.Success(itemKey) else ValidationResult.InvalidInput(itemKey, validation)
    }

    fun returnErrorEntry(): Pair<ItemKey, List<ValidationError>>{
        return Pair(itemKey, validation)
    }
}