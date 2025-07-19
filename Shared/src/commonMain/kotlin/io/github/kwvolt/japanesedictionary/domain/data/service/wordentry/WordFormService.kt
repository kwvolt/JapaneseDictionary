package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseError
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationError
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import kotlinx.collections.immutable.PersistentMap

class WordFormService(
    private val wordEntryFormBuilder: WordEntryFormBuilder,
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormDelete: WordEntryFormDelete,
    private val wordClassBuilder: WordClassBuilder,
    private val wordEntryFormValidation: WordEntryFormValidation
) {
    suspend fun getWordFormData(dictionaryEntryId: Long, formItemManager: FormItemManager): DatabaseResult<WordEntryFormData> {
        return wordEntryFormBuilder.createWordFormData(dictionaryEntryId, formItemManager)
    }

    suspend fun applyIsBookmark(dictionaryEntryId:Long, isBookmark: Boolean){
        TODO()
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
        return when (val result: ValidationResult<Unit> = wordEntryFormValidation.validateForm(wordEntryFormData)) {
            is ValidationResult.Success -> {
                when (val dbResult = wordEntryFormUpsert.upsertWordEntryFormData(wordEntryFormData, deleteList)) {
                    is DatabaseResult.Success -> UpsertResult.Success
                    is DatabaseResult.NotFound -> UpsertResult.NotFound
                    is DatabaseResult.UnknownError -> UpsertResult.UnknownError(dbResult.exception, dbResult.message)
                    is DatabaseResult.InvalidInput -> UpsertResult.DatabaseOperationFailed(dbResult.key, dbResult.error)
                }
            }
            is ValidationResult.InvalidInputMap -> UpsertResult.ValidationFailed(result.errors)
            is ValidationResult.UnknownError -> UpsertResult.UnknownError(result.exception, result.message)
            is ValidationResult.NotFound -> UpsertResult.NotFound
            else -> UpsertResult.UnknownError(IllegalStateException("Unknown Error -- ValidationResult.invalidInput managed to get passed"), "Unknown Error -- ValidationResult.invalidInput managed to get passed")
        }
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit>  {
        return wordEntryFormDelete.deleteWordEntryFormData(dictionaryEntryId)
    }

    suspend fun validatePrimaryText(primaryTextInput: TextItem): ValidationResult<Unit> {
        val result: Pair<ItemKey, List<ValidationError>> = wordEntryFormValidation.validatePrimaryText(primaryTextInput)
        return result.toValidationResult()
    }

    fun validateNotes(noteItem: TextItem, noteList: List<TextItem>): ValidationResult<ItemKey>{
        return wordEntryFormValidation.validateNotes(noteItem, noteList)
    }

    suspend fun validateKana(kana: TextItem, kanaList: List<TextItem>): ValidationResult<Unit>{
        val result: Pair<ItemKey, List<ValidationError>> = wordEntryFormValidation.validateKana(kana, kanaList)
        return result.toValidationResult()
    }
/*
    suspend fun validateKanaListNotEmpty(section: Int, kanaMap: PersistentMap<String, TextItem>): ValidationResult<Unit>{
        val result: Pair<ItemKey, List<ValidationError>> =  wordEntryFormValidation.validateHasKanaEntry(section, kanaMap)
        return result.toValidationResult()
    }

 */

    suspend fun validateMeaning(meaningTextInput: TextItem): ValidationResult<Unit>{
        val result: Pair<ItemKey, List<ValidationError>> = wordEntryFormValidation.validateMeaningText(meaningTextInput)
        return result.toValidationResult()
    }

    // no validation should happen
    suspend fun validateWordClass(wordClassItem: WordClassItem): ValidationResult<Unit>{
        return ValidationResult.Success(Unit)
    }

    private fun Pair<ItemKey, List<ValidationError>>.toValidationResult(): ValidationResult<Unit> =
        if (this.second.isEmpty()) ValidationResult.Success(Unit) else ValidationResult.InvalidInput(this.first, this.second)
}