package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryUserQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryUserRepositoryInterface

class DictionaryUserRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionaryUserQueries
    ): DictionaryUserRepositoryInterface {
    override suspend fun selectIsBookmarked(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Boolean> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            queries.selectIsBookmark(id).awaitAsOneOrNull()
        }.map { result -> result != 0L}
    }

    override suspend fun updateIsBookmark(
        id: Long,
        isBookmark: Boolean,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull) { queries.updateIsBookmark(if (isBookmark) 1L else 0L, id) }
    }
}