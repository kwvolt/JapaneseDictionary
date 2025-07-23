package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.formatDatabaseErrorTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class WordEntryFormUpsertValidation(
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormValidation: WordEntryFormValidation
) {
    suspend fun wordEntryForm(wordEntryFormData: WordEntryFormData, deleteList: List<GenericItemProperties>): UpsertResult<Unit>{
        return validateAndUpsert(wordEntryFormValidation.validateForm(wordEntryFormData)) {
            wordEntryFormUpsert.upsertWordEntryFormData(it, deleteList)
        }
    }

    suspend fun dictionaryEntry(primaryTextItem: TextItem, wordClassId: Long): UpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validatePrimaryText(primaryTextItem))
            { wordEntryFormUpsert.upsertDictionaryEntry(it, wordClassId) }
    }

    suspend fun dictionaryNote(dictionaryId: Long, entryNoteItem: TextItem, entryNoteItemList: List<TextItem>): UpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateNotes(entryNoteItem, entryNoteItemList)) {
            wordEntryFormUpsert.upsertDictionaryEntryNote(dictionaryId, it)
        }
    }

    suspend fun dictionarySectionMeaning(dictionaryId: Long, meaningItem: TextItem): UpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateMeaningText(meaningItem)) {
            wordEntryFormUpsert.upsertDictionarySection(dictionaryId, it)
        }
    }

    suspend fun dictionarySectionKana(dictionarySectionId: Long, kanaItem: TextItem, kanaItemList: List<TextItem>): UpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateKana(kanaItem, kanaItemList)){
            wordEntryFormUpsert.upsertDictionarySectionKana(dictionarySectionId, it)
        }
    }

    suspend fun dictionarySectionNote(dictionarySectionId: Long, sectionNoteItem: TextItem, sectionNoteItemList: List<TextItem>): UpsertResult<Long>{
        return validateAndUpsert(wordEntryFormValidation.validateNotes(sectionNoteItem, sectionNoteItemList)) {
            wordEntryFormUpsert.upsertDictionarySectionNote(dictionarySectionId, it)
        }
    }

    private suspend inline fun <T, R> validateAndUpsert(
        validation: ValidationResult<T>,
        crossinline upsert: suspend (T) -> DatabaseResult<R>
    ): UpsertResult<R> {
        return validation.toUpsertResult { mapDatabaseResultToUpsert(upsert(it)) }
    }

    private fun <T> mapDatabaseResultToUpsert(result: DatabaseResult<T>): UpsertResult<T>{
        return when(result){
            is DatabaseResult.Success -> UpsertResult.Success(result.value)
            is DatabaseResult.InvalidInput -> UpsertResult.SingleItemOperationFailed(result.key, formatDatabaseErrorTypeToMessage(result.error))
            DatabaseResult.NotFound -> UpsertResult.NotFound
            is DatabaseResult.UnknownError -> UpsertResult.UnknownError(result.exception, result.message)
        }
    }

    private suspend inline fun <T, R> ValidationResult<T>.toUpsertResult(
        crossinline onSuccess: suspend (T) -> UpsertResult<R>
    ): UpsertResult<R> = when (this) {
        is ValidationResult.Success -> onSuccess(this.value)
        is ValidationResult.InvalidInput -> UpsertResult.SingleItemOperationFailed(key, formatValidationTypeToMessage(error))
        is ValidationResult.InvalidInputMap -> UpsertResult.ItemsOperationFailed(errors.mapValues { formatValidationTypeToMessage(it.value) })
        ValidationResult.NotFound -> UpsertResult.NotFound
        is ValidationResult.UnknownError -> UpsertResult.UnknownError(exception, message)
    }
}

sealed class UpsertResult<out T> {
    data class Success<T>(val value: T) : UpsertResult<T>()
    data class SingleItemOperationFailed(val itemKey: ItemKey, val error: String): UpsertResult<Nothing>()
    data class ItemsOperationFailed(val errors: Map<ItemKey, String>): UpsertResult<Nothing>()
    data object NotFound : UpsertResult<Nothing>()
    data class UnknownError(val exception: Throwable, val message: String?) : UpsertResult<Nothing>()
}