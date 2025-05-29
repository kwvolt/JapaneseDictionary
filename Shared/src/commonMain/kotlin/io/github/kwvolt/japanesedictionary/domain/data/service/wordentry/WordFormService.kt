package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.Kana
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationError
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import kotlinx.collections.immutable.PersistentMap

class WordFormService(
    private val wordEntryFormBuilder: WordEntryFormBuilder,
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormDelete: WordEntryFormDelete,
    private val wordClassBuilder: WordClassBuilder,
    private val wordEntryFormValidation: WordEntryFormValidation
) {
    suspend fun getWordFormData(dictionaryEntryId: Long, formSectionManager: FormSectionManager): DatabaseResult<WordEntryFormData> {
        return wordEntryFormBuilder.createWordFormData(dictionaryEntryId, formSectionManager)
    }


    suspend fun getMainClassList(): DatabaseResult<List<MainClassContainer>>{
        return wordClassBuilder.getMainClassList()
    }

    suspend fun getSubClassMap(mainClassList: List<MainClassContainer>): DatabaseResult<Map<Long, List<SubClassContainer>>>  {
        return wordClassBuilder.getSubClassMap(mainClassList)
    }

    suspend fun upsertWordEntryFormDataIntoDatabase(
        wordEntryFormData: WordEntryFormData,
        deleteList: List<GenericItemProperties>
    ): UpsertResult {
        return when (val result = wordEntryFormValidation.validateForm(wordEntryFormData)) {
            is ValidationResult.Success -> {
                val wordClassId = result.value
                when (val dbResult = wordEntryFormUpsert.upsertWordEntryFormData(wordEntryFormData, wordClassId, deleteList)) {
                    is DatabaseResult.Success -> UpsertResult.Success
                    is DatabaseResult.NotFound -> UpsertResult.NotFound
                    is DatabaseResult.UnknownError -> UpsertResult.UnknownError(dbResult.exception, dbResult.message)
                }
            }
            is ValidationResult.InvalidInputMap -> UpsertResult.ValidationFailed(result.errors)
            is ValidationResult.UnknownError -> UpsertResult.UnknownError(result.exception, result.message)
            is ValidationResult.NotFound -> UpsertResult.NotFound
            else -> UpsertResult.UnknownError(null, "Unknown Error")
        }
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit>  {
        return wordEntryFormDelete.deleteWordEntryFormData(dictionaryEntryId)
    }

    suspend fun validatePrimaryText(primaryTextInput: InputTextItem): ValidationResult<Unit> {
        val result: Pair<ValidationKey, List<ValidationError>> = wordEntryFormValidation.validatePrimaryText(primaryTextInput)
        return result.toValidationResult()
    }

    suspend fun validateNotes(noteItem: InputTextItem, noteList: List<InputTextItem>): ValidationResult<Unit>{
        val result: Pair<ValidationKey, List<ValidationError>> = wordEntryFormValidation.validateNotes(noteItem, noteList)
        return result.toValidationResult()
    }

    suspend fun validateKana(kana: InputTextItem, kanaList: List<InputTextItem>): ValidationResult<Unit>{
        val result: Pair<ValidationKey, List<ValidationError>> = wordEntryFormValidation.validateKana(kana, kanaList)
        return result.toValidationResult()
    }

    suspend fun validateKanaListNotEmpty(section: Int, kanaMap: PersistentMap<String, InputTextItem>): ValidationResult<Unit>{
        val result: Pair<ValidationKey, List<ValidationError>> =  wordEntryFormValidation.validateHasKanaEntry(section, kanaMap)
        return result.toValidationResult()
    }

    suspend fun validateMeaning(meaningTextInput: InputTextItem): ValidationResult<Unit>{
        val result: Pair<ValidationKey, List<ValidationError>> = wordEntryFormValidation.validateMeaningText(meaningTextInput)
        return result.toValidationResult()
    }

    suspend fun validateWordClass(wordClassItem: WordClassItem): ValidationResult<Unit>{
        return wordEntryFormValidation.validateWordClass(wordClassItem).flatMap { ValidationResult.Success(Unit) }

    }

    private fun Pair<ValidationKey, List<ValidationError>>.toValidationResult(): ValidationResult<Unit> =
        if (this.second.isEmpty()) ValidationResult.Success(Unit) else ValidationResult.InvalidInput(this.first, this.second)
}

sealed class UpsertResult {
    data object Success : UpsertResult()
    data class ValidationFailed(val errors: Map<ValidationKey, List<ValidationError>>) : UpsertResult()
    data object NotFound : UpsertResult()
    data class UnknownError(val exception: Throwable?, val message: String?) : UpsertResult()
}