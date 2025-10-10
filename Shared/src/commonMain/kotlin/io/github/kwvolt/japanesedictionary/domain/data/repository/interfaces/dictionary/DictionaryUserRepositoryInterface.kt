package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionaryUserRepositoryInterface {
    suspend fun selectIsBookmarked(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
    suspend fun updateIsBookmark(id: Long, isBookmark: Boolean, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
}