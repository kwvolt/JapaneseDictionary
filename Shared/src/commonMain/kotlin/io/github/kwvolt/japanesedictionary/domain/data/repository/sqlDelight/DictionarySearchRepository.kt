package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionarySearchQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySearchRepositoryInterface

class DictionarySearchRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionarySearchQueries,
): DictionarySearchRepositoryInterface {
    override suspend fun searchIdsByPrimaryText(searchTerm: String): DatabaseResult<List<Long>> {
        return dbHandler.withContextDispatcherWithException(null,
            "Error at searchIdsByPrimaryText in DictionarySearchRepository For value searchTerm: $searchTerm"
        ){
            queries.searchIdsByPrimaryText("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByKana(searchTerm: String): DatabaseResult<List<Long>> {
        return dbHandler.withContextDispatcherWithException(null,
            "Error at searchIdsByKana in DictionarySearchRepository For value searchTerm: $searchTerm"
        ){
            queries.searchIdsByKanaText("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByMeaning(searchTerm: String): DatabaseResult<List<Long>> {
        return dbHandler.withContextDispatcherWithException(null,
            "Error at searchIdsByMeaning in DictionarySearchRepository For value searchTerm: $searchTerm"
        ){
            queries.searchIdsByMeaning("$searchTerm*").awaitAsList()
        }
    }

    override suspend fun searchIdsByWordClassId(wordClassId: Long): DatabaseResult<List<Long>> {
        return dbHandler.withContextDispatcherWithException(null,
            "Error at searchIdsByMeaning in DictionarySearchRepository For value wordClassId: $wordClassId"
        ){
            queries.searchIdsByWordClassId(wordClassId).awaitAsList()
        }
    }

    override suspend fun searchIdsByIsBookmark(isBookmark: Boolean): DatabaseResult<List<Long>> {
        return dbHandler.withContextDispatcherWithException(null,
            "Error at searchIdsByIsBookmark in DictionarySearchRepository For value isBookmark: $isBookmark"
        ){
            queries.searchIdsByBookmark(if(isBookmark) 1 else 0).awaitAsList()
        }
    }
}