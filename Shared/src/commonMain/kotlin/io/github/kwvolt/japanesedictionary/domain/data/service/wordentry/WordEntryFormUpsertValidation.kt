package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.formatDatabaseErrorTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.GenericItemProperties

class WordEntryFormUpsertValidation(
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormValidation: WordEntryFormValidation
) {
    suspend fun wordEntryForm(wordEntryFormData: WordEntryFormData, deleteList: List<GenericItemProperties>): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateForm(wordEntryFormData)) {
            wordEntryFormUpsert.upsertWordEntryFormData(it, deleteList)
        }
    }

    suspend fun dictionaryEntry(primaryTextItem: TextItem, wordClassId: Long): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validatePrimaryText(primaryTextItem))
            { wordEntryFormUpsert.upsertDictionaryEntry(it, wordClassId) }
    }

    suspend fun dictionaryNote(dictionaryId: Long, entryNoteItem: TextItem, entryNoteItemList: List<TextItem>): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateNotes(entryNoteItem, entryNoteItemList)) {
            wordEntryFormUpsert.upsertDictionaryEntryNote(dictionaryId, it)
        }
    }

    suspend fun dictionarySectionMeaning(dictionaryId: Long, meaningItem: TextItem): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateMeaningText(meaningItem)) {
            wordEntryFormUpsert.upsertDictionarySection(dictionaryId, it)
        }
    }

    suspend fun dictionarySectionKana(dictionaryEntryId: Long, dictionarySectionId: Long, kanaItem: TextItem, kanaItemList: List<TextItem>): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateKana(kanaItem, kanaItemList)){
            wordEntryFormUpsert.upsertDictionarySectionKana(dictionaryEntryId, dictionarySectionId, it)
        }
    }

    suspend fun dictionarySectionNote(dictionarySectionId: Long, sectionNoteItem: TextItem, sectionNoteItemList: List<TextItem>): ValidUpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateNotes(sectionNoteItem, sectionNoteItemList)) {
            wordEntryFormUpsert.upsertDictionarySectionNote(dictionarySectionId, it)
        }
    }

    private suspend inline fun <T, R> validateAndUpsert(
        validation: ValidationResult<T>,
        crossinline upsert: suspend (T) -> DatabaseResult<R>
    ): ValidUpsertResult<R> {
        return validation.toUpsertResult { mapDatabaseResultToUpsert(upsert(it)) }
    }

    private fun <T> mapDatabaseResultToUpsert(result: DatabaseResult<T>): ValidUpsertResult<T>{
        return when(result){
            is DatabaseResult.Success -> ValidUpsertResult.Success(result.value)
            is DatabaseResult.InvalidInput -> ValidUpsertResult.SingleItemOperationFailed(result.key, formatDatabaseErrorTypeToMessage(result.error))
            DatabaseResult.NotFound -> ValidUpsertResult.NotFound
            is DatabaseResult.UnknownError -> ValidUpsertResult.UnknownError(result.exception)
        }
    }

    private suspend inline fun <T, R> ValidationResult<T>.toUpsertResult(
        crossinline onSuccess: suspend (T) -> ValidUpsertResult<R>
    ): ValidUpsertResult<R> = when (this) {
        is ValidationResult.Success -> onSuccess(this.value)
        is ValidationResult.InvalidInput -> ValidUpsertResult.SingleItemOperationFailed(key, formatValidationTypeToMessage(error))
        is ValidationResult.InvalidInputMap -> ValidUpsertResult.ItemsOperationFailed(errors.mapValues { formatValidationTypeToMessage(it.value) })
        ValidationResult.NotFound -> ValidUpsertResult.NotFound
        is ValidationResult.UnknownError -> ValidUpsertResult.UnknownError(exception)
    }
}

sealed class ValidUpsertResult<out T> {
    data class Success<T>(val value: T) : ValidUpsertResult<T>()
    data class SingleItemOperationFailed(val itemKey: ItemKey, val error: String): ValidUpsertResult<Nothing>()
    data class ItemsOperationFailed(val errors: Map<ItemKey, String>): ValidUpsertResult<Nothing>()
    data object NotFound : ValidUpsertResult<Nothing>()
    data class UnknownError(val exception: Throwable) : ValidUpsertResult<Nothing>()
}