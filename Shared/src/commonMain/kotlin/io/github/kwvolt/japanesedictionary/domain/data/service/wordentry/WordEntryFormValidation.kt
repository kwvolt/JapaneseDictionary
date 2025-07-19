package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.TextPreprocessor
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
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import kotlinx.collections.immutable.toPersistentMap

class WordEntryFormValidation() {

    fun validatePrimaryText(primaryTextInput: TextItem): ValidationResult<TextItem> = validatePrimaryTextBuilder(primaryTextInput).toValidationResult()

    private fun validatePrimaryTextBuilder(primaryTextInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(primaryTextInput)
            .validateNotEmptyString()
            .validJapanese()
    }

    fun validateNotes(entryNoteInput: TextItem, entryNoteList: List<TextItem>): ValidationResult<TextItem> {
        return  validateNotesBuilder(entryNoteInput).validateNoDuplicate(findDuplicateIdentifiers(entryNoteList)).toValidationResult()
    }

    private fun validateNotesBuilder(entryNoteInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(entryNoteInput).validateNotEmptyString()
    }

    fun validateMeaningText(meaningTextInput: TextItem): ValidationResult<TextItem> {
        return  validateMeaningTextBuilder(meaningTextInput).toValidationResult()
    }

    private fun validateMeaningTextBuilder(meaningTextInput: TextItem): ValidationTextItemBuilder{
        return ValidationTextItemBuilder(meaningTextInput)
            .validateNotEmptyString()
    }

    fun validateKana(kanaInput: TextItem, kanaList: List<TextItem>): ValidationResult<TextItem> {
        return  validateKanaBuilder(kanaInput).validateNoDuplicate(findDuplicateIdentifiers(kanaList)).toValidationResult()
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
    fun validateForm(wordEntryFormData: WordEntryFormData): ValidationResult<WordEntryFormData> {

        val validationErrors = mutableMapOf<ItemKey, List<ValidationError>>()

        val wordClassItem: WordClassItem = wordEntryFormData.wordClassInput
        var cleanedPrimary: TextItem? = null
        val cleanedEntryNotes = mutableMapOf<String, TextItem>()
        val cleanedSections = mutableMapOf<Int, WordSectionFormData>()

        // primary Text
        val primaryTextValidationBuilder: ValidationTextItemBuilder = validatePrimaryTextBuilder(wordEntryFormData.primaryTextInput)
        validationErrors.putIfNotEmpty(primaryTextValidationBuilder.returnErrorEntry())
        cleanedPrimary = primaryTextValidationBuilder.getCleanedItem()

        // entry notes
        val entryNoteItemList: List<TextItem> = wordEntryFormData.entryNoteInputMap.values.toList()
        validationErrors.putAll(
            validateListNoDuplicate(
                entryNoteItemList,
                { validateNotesBuilder(it) },
                { id: String, item:TextItem -> cleanedEntryNotes[id] = item}
            )
        )

        // Entry Section Validation
        val entrySectionMap = wordEntryFormData.wordSectionMap
        for ((index: Int, entrySection: WordSectionFormData) in entrySectionMap.entries) {

            var cleanedMeaning: TextItem? = null
            val cleanedKana = mutableMapOf<String, TextItem>()
            val cleanedNotes = mutableMapOf<String, TextItem>()

            // Meaning Text
            val meaningTextValidationBuilder: ValidationTextItemBuilder = validateMeaningTextBuilder(entrySection.meaningInput)
            validationErrors.putIfNotEmpty(meaningTextValidationBuilder.returnErrorEntry())
            cleanedMeaning = meaningTextValidationBuilder.getCleanedItem()

            // Kana Text
            val kanaItemList: List<TextItem> = entrySection.getKanaInputMapAsList()
            validationErrors.putAll(validateListNoDuplicate(kanaItemList, { validateKanaBuilder(it) }, { id: String, item: TextItem -> cleanedKana[id] = item}))

            // Section Note
            val sectionNoteItemList: List<TextItem> = entrySection.getComponentNoteInputMapAsList()
            validationErrors.putAll(validateListNoDuplicate(sectionNoteItemList, { validateNotesBuilder(it) }, { id: String, item: TextItem -> cleanedNotes[id] = item}))

            cleanedSections[index] = WordSectionFormData(cleanedMeaning, cleanedKana.toPersistentMap(), cleanedNotes.toPersistentMap())
        }


        val cleanedWordEntryFormData = WordEntryFormData(wordClassItem, cleanedPrimary, cleanedEntryNotes.toPersistentMap(), cleanedSections.toPersistentMap())
        if (validationErrors.isNotEmpty()) {
            return ValidationResult.InvalidInputMap(validationErrors)
        }
        return ValidationResult.Success(cleanedWordEntryFormData)
    }

    private fun validateListNoDuplicate(
        itemList: List<TextItem>,
        block: (TextItem) -> ValidationTextItemBuilder,
        assignCleanItem: (String, TextItem) -> Unit
    ): Map<ItemKey, List<ValidationError>> {
        val validationErrors = mutableMapOf<ItemKey, List<ValidationError>>()
        val duplicates = findDuplicateIdentifiers(itemList)
        itemList.forEach {
            val builder = block(it).validateNoDuplicate(duplicates)
            validationErrors.putIfNotEmpty(builder.returnErrorEntry())
            val cleanItem: TextItem = builder.getCleanedItem()
            assignCleanItem(cleanItem.itemProperties.getIdentifier(), cleanItem)
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
    private val itemText: String = TextPreprocessor.cleanInput(textItem.inputTextValue)
    private val itemKey: ItemKey.DataItem = ItemKey.DataItem(textItem.itemProperties.getIdentifier())
    private val validation = mutableListOf<ValidationError>()
    private val cleanedItem: TextItem by lazy { textItem.copy(inputTextValue = itemText) }


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


    fun returnErrorEntry(): Pair<ItemKey, List<ValidationError>>{
        return Pair(itemKey, validation)
    }

    fun toValidationResult(): ValidationResult<TextItem> {
        return if (validation.isEmpty()) ValidationResult.Success(cleanedItem)
        else ValidationResult.InvalidInput(itemKey, validation)
    }

    fun getCleanedItem(): TextItem {
        return cleanedItem
    }
}