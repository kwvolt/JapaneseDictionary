package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntryQueries


class EntryRepository(
    private val dbHandler: DatabaseHandler,
    private val dictionaryEntryQueries: DictionaryEntryQueries
    ): EntryRepositoryInterface {

    override suspend fun insertDictionaryEntry(wordClassId: Long, primaryText: String): DatabaseResult<Long> {
        return dbHandler.wrapResult(dictionaryEntryQueries.insertDictionaryEntry(wordClassId, primaryText).awaitAsOneOrNull())
    }

    override suspend fun selectDictionaryEntryId(primaryText: String):DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException("Error at getDictionaryEntry For value PrimaryText: $primaryText"){
                dictionaryEntryQueries.selectDictionaryEntryId(
                    primaryText
                ).awaitAsOneOrNull()
        }
    }

    override suspend fun selectDictionaryEntry(dictionaryEntryId: Long): DatabaseResult<DictionaryEntryContainer> {
        return dbHandler.withContextDispatcherWithException("Error at getDictionaryEntry For value dictionaryEntryId: $dictionaryEntryId"){
            dictionaryEntryQueries.selectDictionaryEntry(dictionaryEntryId).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(DictionaryEntryContainer(dictionaryEntryId, result.word_class_id, result.primary_text)) }
    }

    override suspend fun updateDictionaryEntryPrimaryText(
        dictionaryEntryId: Long,
        newPrimaryText: String
    ): DatabaseResult<Unit>{
        return dbHandler.wrapResult(dictionaryEntryQueries.updateDictionaryEntryPrimaryText(
                    newPrimaryText,
                    dictionaryEntryId
                )
        )
    }


    override suspend fun updateDictionaryEntryWordClass(
        dictionaryEntryId: Long,
        newWordClassId: Long
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(dictionaryEntryQueries.updateDictionaryEntryWordClass(
                    newWordClassId,
                    dictionaryEntryId
                )
        )
    }

    override suspend fun deleteDictionaryEntry(dictionaryEntryId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(dictionaryEntryQueries.deleteDictionaryEntry(dictionaryEntryId))
    }
}