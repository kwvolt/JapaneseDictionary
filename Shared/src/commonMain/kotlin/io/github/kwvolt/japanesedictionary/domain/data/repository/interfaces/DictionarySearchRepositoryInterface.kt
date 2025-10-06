package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionarySearchRepositoryInterface {
    suspend fun searchIdsByPrimaryText(searchTerm: String): DatabaseResult<List<Long>>
    suspend fun searchIdsByKana(searchTerm: String): DatabaseResult<List<Long>>
    suspend fun searchIdsByMeaning(searchTerm: String): DatabaseResult<List<Long>>
    suspend fun searchIdsByWordClassId(wordClassId: Long): DatabaseResult<List<Long>>
    suspend fun searchIdsByIsBookmark(isBookmark: Boolean): DatabaseResult<List<Long>>
}