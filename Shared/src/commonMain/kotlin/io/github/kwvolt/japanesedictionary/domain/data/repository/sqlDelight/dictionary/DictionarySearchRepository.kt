package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionarySearchQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySearchRepositoryInterface

class DictionarySearchRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionarySearchQueries,
): DictionarySearchRepositoryInterface {
    override suspend fun searchIdsByPrimaryText(
        searchTerm: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull){
            queries.searchIdsByPrimaryText("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByKana(
        searchTerm: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull){
            queries.searchIdsByKanaText("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByMeaning(
        searchTerm: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull){
            queries.searchIdsByMeaning("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByWordClassId(
        wordClassId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull){
            queries.searchIdsByWordClassId(wordClassId).awaitAsList()
        }
    }

    override suspend fun searchIdsByIsBookmark(
        isBookmark: Boolean,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull){
            queries.searchIdsByBookmark(if(isBookmark) 1 else 0).awaitAsList()
        }
    }
}