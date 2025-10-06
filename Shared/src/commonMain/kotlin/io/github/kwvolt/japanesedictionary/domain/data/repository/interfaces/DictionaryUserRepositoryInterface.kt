package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionaryUserRepositoryInterface {
    suspend fun selectIsBookmarked(id: Long, itemId: String? = null): DatabaseResult<Boolean>
    suspend fun updateIsBookmark(id: Long, isBookmark: Boolean, itemId: String? = null): DatabaseResult<Unit>
}