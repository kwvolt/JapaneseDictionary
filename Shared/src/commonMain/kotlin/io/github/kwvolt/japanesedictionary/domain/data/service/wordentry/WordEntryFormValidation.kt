package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.findDuplicateIdentifiers
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.data.validation.validateNotEmpty
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem

class WordEntryFormValidation(
    private val dbHandler: DatabaseHandlerBase,
    private val wordClassRepository: WordClassRepositoryInterface
) {

    suspend fun validatePrimaryText(primaryTextInput: InputTextItem): Map<ValidationKey, List<DatabaseResult.InvalidInput>>{
        val validationErrors = mutableMapOf<ValidationKey, List<DatabaseResult.InvalidInput>>()
        validationCheck(
            primaryTextInput,
            validationErrors
        ) { primaryText: String, validation: MutableList<DatabaseResult.InvalidInput> ->
            if (!validateNotEmpty(primaryText)) {
                validation.add(DatabaseResult.InvalidInput(ValidationType.Empty))
            }
            if (!validJapanese(primaryText)) {
                validation.add(DatabaseResult.InvalidInput(ValidationType.Japanese))
            }
        }
        return validationErrors
    }

    suspend fun validateMeaningText(meaningTextInput: InputTextItem): Map<ValidationKey, List<DatabaseResult.InvalidInput>>{
        val validationErrors = mutableMapOf<ValidationKey, List<DatabaseResult.InvalidInput>>()
        validationCheck(
            meaningTextInput,
            validationErrors
        ) { meaningText: String, validation: MutableList<DatabaseResult.InvalidInput> ->
            if (!validateNotEmpty(meaningText)) {
                validation.add(
                    DatabaseResult.InvalidInput(
                        ValidationType.Empty
                    )
                )
            }
        }
        return validationErrors
    }

    suspend fun validateWordClass(wordClassItem: WordClassItem): DatabaseResult<Long>{
        val validationErrors = mutableMapOf<ValidationKey, List<DatabaseResult.InvalidInput>>()
        val wordClassIdResult = wordClassRepository.selectWordClassIdByMainClassIdAndSubClassId(
            wordClassItem.chosenMainClassId,
            wordClassItem.chosenSubClassId
        )
        val wordClassId = when (wordClassIdResult) {
            is DatabaseResult.Success -> wordClassIdResult.value
            is DatabaseResult.NotFound -> {
                validationErrors[ValidationKey.DataItem(wordClassItem.itemProperties.getIdentifier())] =
                    listOf(
                        DatabaseResult.InvalidInput(
                            ValidationType.InvalidSelection
                        )
                    )
                null
            }
            else -> {
                return wordClassIdResult.mapErrorTo<Long, Long>()
            }
        }
        if(wordClassId != null){
            DatabaseResult.Success(wordClassId)
        }
        return DatabaseResult.InvalidInputMap(validationErrors)
    }


    suspend fun validateForm(wordEntryFormData: WordEntryFormData): DatabaseResult<Long> {
        val validationErrors = mutableMapOf<ValidationKey, List<DatabaseResult.InvalidInput>>()

        // word class
        val wordClassItem = wordEntryFormData.wordClassInput
        val wordClassIdResult = wordClassRepository.selectWordClassIdByMainClassIdAndSubClassId(
            wordClassItem.chosenMainClassId,
            wordClassItem.chosenSubClassId
        )
        val wordClassId = when (wordClassIdResult) {
            is DatabaseResult.Success -> wordClassIdResult.value
            is DatabaseResult.NotFound -> {
                validationErrors[ValidationKey.DataItem(wordClassItem.itemProperties.getIdentifier())] =
                    listOf(
                        DatabaseResult.InvalidInput(
                            ValidationType.InvalidSelection
                        )
                    )
                null
            }
            else -> {
                return wordClassIdResult.mapErrorTo<Long, Long>()
            }
        }

        validationErrors.putAll(validatePrimaryText(wordEntryFormData.primaryTextInput))

        val entryNotes = wordEntryFormData.entryNoteInputMap.values.toList()
        validationListCheck(entryNotes, validationErrors)

        // Entry Section Validation
        val entrySections = wordEntryFormData.wordSectionMap
        for (entrySectionEntries in entrySections.entries) {
            val entrySection = entrySectionEntries.value
            val section = entrySectionEntries.key
            validationErrors.putAll(validateMeaningText(entrySection.meaningInput))

            if (entrySection.kanaInputMap.isEmpty()) {
                validationErrors[ValidationKey.FormItem(FormKeys.kanaLabel(section))] = listOf(
                    DatabaseResult.InvalidInput(
                        ValidationType.KanaSection
                    )
                )
            }
            val entrySectionKanaItems = entrySection.kanaInputMap.values.toList()
            validationListCheck(entrySectionKanaItems, validationErrors) { kanaText, validation ->
                if (!validKana(kanaText)) {
                    validation.add(
                        DatabaseResult.InvalidInput(
                            ValidationType.Kana
                        )
                    )
                }
            }

            val entrySectionNotes = entrySection.sectionNoteInputMap.values.toList()
            validationListCheck(entrySectionNotes, validationErrors)
        }


        if (wordClassId == null || validationErrors.isNotEmpty()) {
            return DatabaseResult.InvalidInputMap(validationErrors)
        }
        return DatabaseResult.Success(wordClassId)
    }

    private suspend fun validationCheck(
        item: InputTextItem,
        validationErrors: MutableMap<ValidationKey, List<DatabaseResult.InvalidInput>>,
        block: (String, MutableList<DatabaseResult.InvalidInput>) -> Unit
    ) {
        val itemIdentifier: String = item.itemProperties.getIdentifier()
        val itemText: String = item.inputTextValue.trim()
        val validationList = mutableListOf<DatabaseResult.InvalidInput>()
        block(itemText, validationList)
        if (validationList.isNotEmpty()) {
            validationErrors[ValidationKey.DataItem(itemIdentifier)] = validationList
        }
    }

    private suspend fun validationListCheck(
        items: List<InputTextItem>,
        errors: MutableMap<ValidationKey, List<DatabaseResult.InvalidInput>>,
        validator: (String, MutableList<DatabaseResult.InvalidInput>) -> Unit = { _, _ -> }
    ) {
        val duplicates = findDuplicateIdentifiers(items)
        for (item in items) {
            val identifier = item.itemProperties.getIdentifier()
            validationCheck(item, errors) { text, errorList ->
                if (identifier in duplicates) {
                    errorList.add(DatabaseResult.InvalidInput(ValidationType.Duplicate))
                }
                validator(text, errorList)
            }
        }
    }
}

sealed class ValidationKey {
    data class DataItem(val id: String) : ValidationKey()
    data class FormItem(val key: String) : ValidationKey()
}