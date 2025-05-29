package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationError
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.findDuplicateIdentifiers
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.data.validation.validateNotEmpty
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import kotlinx.collections.immutable.PersistentMap

class WordEntryFormValidation(
    private val dbHandler: DatabaseHandlerBase,
    private val wordClassRepository: WordClassRepositoryInterface
) {
    suspend fun validatePrimaryText(primaryTextInput: InputTextItem): Pair<ValidationKey, List<ValidationError>>{
        return validationCheck(
            primaryTextInput,
        ) { primaryText: String, validation: MutableList<ValidationError> ->
            if (!validateNotEmpty(primaryText)) {
                validation.add(ValidationError(ValidationType.Empty))
            }
            if (!validJapanese(primaryText)) {
                validation.add(ValidationError(ValidationType.Japanese))
            }
        }
    }

    suspend fun validateNotes(entryNoteInput: InputTextItem, entryNoteList: List<InputTextItem>):Pair<ValidationKey, List<ValidationError>> {
        return validationItemAgainstListCheck(entryNoteInput, entryNoteList)
    }

    suspend fun validateMeaningText(meaningTextInput: InputTextItem): Pair<ValidationKey, List<ValidationError>>{
        return validationCheck(
            meaningTextInput,
        ) { meaningText: String, validation: MutableList<ValidationError> ->
            if (!validateNotEmpty(meaningText)) {
                validation.add(
                    ValidationError(
                        ValidationType.Empty
                    )
                )
            }
        }
    }

    suspend fun validateHasKanaEntry(section: Int, kanaMap: PersistentMap<String, InputTextItem>): Pair<ValidationKey, List<ValidationError>>{
        val key: ValidationKey = ValidationKey.FormItem(FormKeys.kanaLabel(section))
        if (kanaMap.isEmpty()) {
            return Pair(key, listOf(ValidationError(ValidationType.KanaSection)))
        }
        return Pair(key, listOf())
    }

    suspend fun validateKana(kanaInput: InputTextItem, kanaList: List<InputTextItem>): Pair<ValidationKey, List<ValidationError>> {
        return validationItemAgainstListCheck(kanaInput, kanaList){
            kanaText: String, validation: MutableList<ValidationError> ->
            if (!validKana(kanaText)) {
                validation.add(
                    ValidationError(
                        ValidationType.Kana
                    )
                )
            }
        }
    }

    suspend fun validateWordClass(item: WordClassItem): ValidationResult<Long> {
        val wordClassIdResult = wordClassRepository.selectWordClassIdByMainClassIdAndSubClassId(
            item.chosenMainClassId,
            item.chosenSubClassId
        )
        return when (wordClassIdResult) {
            is DatabaseResult.Success -> ValidationResult.Success(wordClassIdResult.value)
            is DatabaseResult.NotFound -> {
                val errors: Map<ValidationKey, List<ValidationError>> = mapOf(
                    ValidationKey.DataItem(item.itemProperties.getIdentifier()) to listOf(
                        ValidationError(ValidationType.InvalidSelection)
                    )
                )
                ValidationResult.InvalidInputMap(errors)
            }

            is DatabaseResult.UnknownError -> ValidationResult.UnknownError(
                wordClassIdResult.exception,
                wordClassIdResult.message
            )

        }
    }



    suspend fun validateForm(wordEntryFormData: WordEntryFormData): ValidationResult<Long> {
        val validationErrors = mutableMapOf<ValidationKey, List<ValidationError>>()

        // word class
        val wordClassItem = wordEntryFormData.wordClassInput
        val wordClassResult = validateWordClass(wordClassItem)
        var wordClassId: Long? = null
        when(wordClassResult){
            is ValidationResult.Success<Long> -> wordClassId = wordClassResult.value
            is ValidationResult.InvalidInputMap -> validationErrors.putAll(wordClassResult.errors)
            else -> return wordClassResult
        }

        val primaryPair: Pair<ValidationKey, List<ValidationError>> = validatePrimaryText(wordEntryFormData.primaryTextInput)
        validationErrors.putIfNotEmpty(primaryPair.first, primaryPair.second)

        val entryNotes = wordEntryFormData.entryNoteInputMap.values.toList()
        validationErrors.putAll(validationListCheck(entryNotes))

        // Entry Section Validation
        val entrySections = wordEntryFormData.wordSectionMap
        for (entrySectionEntries in entrySections.entries) {
            val entrySection: WordSectionFormData = entrySectionEntries.value
            val section: Int = entrySectionEntries.key

            val meaningPair: Pair<ValidationKey, List<ValidationError>> = validateMeaningText(entrySection.meaningInput)
            validationErrors.putIfNotEmpty(meaningPair.first, meaningPair.second)


            val kanaIsEmptyPair: Pair<ValidationKey, List<ValidationError>> = validateHasKanaEntry(section, entrySection.kanaInputMap)
            validationErrors.putIfNotEmpty(kanaIsEmptyPair.first, kanaIsEmptyPair.second)

            val entrySectionKanaItems = entrySection.kanaInputMap.values.toList()
            validationErrors.putAll(validationListCheck(entrySectionKanaItems) { kanaText, validation ->
                if (!validKana(kanaText)) {
                    validation.add(
                        ValidationError(
                            ValidationType.Kana
                        )
                    )
                }
            })

            val entrySectionNotes = entrySection.sectionNoteInputMap.values.toList()
            validationErrors.putAll(validationListCheck(entrySectionNotes))
        }


        if (wordClassId == null || validationErrors.isNotEmpty()) {
            return ValidationResult.InvalidInputMap(validationErrors)
        }
        return ValidationResult.Success(wordClassId)
    }

    private suspend fun validationCheck(
        item: InputTextItem,
        block: (String, MutableList<ValidationError>) -> Unit
    ): Pair<ValidationKey, List<ValidationError>> {
        val itemText = item.inputTextValue.trim()
        val itemKey = ValidationKey.DataItem(item.itemProperties.getIdentifier())
        val validationList = mutableListOf<ValidationError>()
        block(itemText, validationList)
        return Pair(itemKey, validationList)
    }

    private suspend fun validationItemAgainstListCheck(
        item: InputTextItem,
        itemsAgainst: List<InputTextItem>,
        validator: (String, MutableList<ValidationError>) -> Unit = { _, _ -> }
    ): Pair<ValidationKey, List<ValidationError>> {
        val duplicates = findDuplicateIdentifiers(itemsAgainst)
        val identifier = item.itemProperties.getIdentifier()
        return validationCheck(item) { text, errorList ->
            if (identifier in duplicates) {
                errorList.add(ValidationError(ValidationType.Duplicate))
            }
            validator(text, errorList)
        }
    }

    private suspend fun validationListCheck(
        items: List<InputTextItem>,
        validator: (String, MutableList<ValidationError>) -> Unit = { _, _ -> }
    ): Map<ValidationKey, List<ValidationError>> {
        val errors = mutableMapOf<ValidationKey, List<ValidationError>>()
        val duplicates = findDuplicateIdentifiers(items)

        for (item in items) {
            val identifier = item.itemProperties.getIdentifier()
            val result = validationCheck(item) { text, errorList ->
                if (identifier in duplicates) {
                    errorList.add(ValidationError(ValidationType.Duplicate))
                }
                validator(text, errorList)
            }
            if (result.second.isNotEmpty()) {
                errors[result.first] = result.second
            }
        }

        return errors
    }

    private fun MutableMap<ValidationKey, List<ValidationError>>.putIfNotEmpty(
        key: ValidationKey,
        errors: List<ValidationError>
    ) {
        if (errors.isNotEmpty()) this[key] = errors
    }
}
