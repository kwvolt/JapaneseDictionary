package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryEntryQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryRepositoryInterface

class DictionaryRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionaryEntryQueries
): DictionaryRepositoryInterface {
    override suspend fun insert(
        wordClassId: Long,
        primaryText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){queries.insert(wordClassId, primaryText).awaitAsOneOrNull()}
    }

    override suspend fun update(
        id: Long,
        wordClassId: Long?,
        primaryText: String?,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){queries.update(wordClassId, primaryText, id)}
    }

    override suspend fun delete(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){queries.delete(id)}
    }

    override suspend fun selectIdByPrimaryText(
        primaryText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            queries.selectIdByPrimaryText(primaryText).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<DictionaryEntryContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map { result -> DictionaryEntryContainer(id, result.word_class_id, result.primary_text) }
    }
}