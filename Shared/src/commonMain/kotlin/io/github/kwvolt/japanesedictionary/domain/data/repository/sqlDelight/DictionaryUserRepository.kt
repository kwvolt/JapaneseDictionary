package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryUserQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryUserRepositoryInterface

class DictionaryUserRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionaryUserQueries
    ): DictionaryUserRepositoryInterface {
    override suspend fun selectIsBookmarked(id: Long, itemId: String?): DatabaseResult<Boolean> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectIsBookmarked in DictionaryUserRepository For value dictionaryEntryId: $id"){
            queries.selectIsBookmark(id).awaitAsOneOrNull()
        }.map { result -> result != 0L}
    }

    override suspend fun updateIsBookmark(
        id: Long,
        isBookmark: Boolean,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) { queries.updateIsBookmark(if (isBookmark) 1L else 0L, id) }.map { Unit }
    }
}