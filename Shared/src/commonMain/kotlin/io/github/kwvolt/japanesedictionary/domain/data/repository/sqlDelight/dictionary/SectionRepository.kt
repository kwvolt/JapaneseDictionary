package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryEntrySectionQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionRepositoryInterface

class SectionRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionQueries: DictionaryEntrySectionQueries
): SectionRepositoryInterface {

    override suspend fun insert(
        dictionaryEntryId: Long,
        meaning: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            entrySectionQueries.insert(dictionaryEntryId, meaning)
                .awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        meaning: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull) {
            entrySectionQueries.updateMeaning(
                meaning,
                id
            )
        }
    }

    override suspend fun delete(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull) {entrySectionQueries.deleteRow(id)}
    }

    override suspend fun selectId(
        dictionaryEntryId: Long,
        meaning: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            entrySectionQueries.selectId(dictionaryEntryId, meaning).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        dictionaryEntryId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<DictionarySectionContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            entrySectionQueries.selectRow(dictionaryEntryId).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(
            DictionarySectionContainer(
                result.dictionary_entry_id,
                result.meaning
            )
        ) }
    }

    override suspend fun selectAllByEntryId(
        dictionaryEntryId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<DictionarySectionContainer>> {
        return dbHandler.selectAll(itemId, returnNotFoundOnNull,
            queryBlock = { entrySectionQueries.selectAllByEntryId(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionarySectionContainer(item.id, item.meaning) }
        )
    }
}