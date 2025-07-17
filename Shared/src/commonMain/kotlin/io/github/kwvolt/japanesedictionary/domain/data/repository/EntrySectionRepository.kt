package io.github.kwvolt.japanesedictionary.domain.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionRepositoryInterface

class EntrySectionRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionQueries: DictionaryEntrySectionQueries
): EntrySectionRepositoryInterface {

    override suspend fun insert(
        dictionaryEntryId: Long,
        meaning: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId) {
            entrySectionQueries.insert(dictionaryEntryId, meaning)
                .awaitAsOneOrNull()
        }
    }

    override suspend fun selectId(
        dictionaryEntryId: Long,
        meaning: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectId in EntrySectionRepository For value dictionaryEntryId: $dictionaryEntryId and meaning: $meaning"
        ) {
            entrySectionQueries.selectId(dictionaryEntryId, meaning).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        dictionaryEntryId: Long,
        itemId: String?
    ): DatabaseResult<DictionaryEntrySectionContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectRow in EntrySectionRepository For value dictionaryEntryId: $dictionaryEntryId"
        ){
            entrySectionQueries.selectRow(dictionaryEntryId).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(DictionaryEntrySectionContainer(result.dictionary_entry_id, result.meaning)) }
    }

    override suspend fun selectAllByEntryId(
        dictionaryEntryId: Long,
        itemId: String?
    ): DatabaseResult<List<DictionaryEntrySectionContainer>> {
        return dbHandler.selectAll(itemId,
            "Error in selectAllByEntryId in EntrySectionRepository for dictionaryEntryId: $dictionaryEntryId",
            queryBlock = { entrySectionQueries.selectAllByEntryId(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionContainer(item.id, item.meaning) }
        )
    }

    override suspend fun updateMeaning(
        dictionaryEntrySectionId: Long,
        newMeaning: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) {
            entrySectionQueries.updateMeaning(
                newMeaning,
                dictionaryEntrySectionId
            )
        }
    }

    override suspend fun deleteRow(
        dictionaryEntrySectionId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionQueries.deleteRow(dictionaryEntrySectionId)}
    }
}