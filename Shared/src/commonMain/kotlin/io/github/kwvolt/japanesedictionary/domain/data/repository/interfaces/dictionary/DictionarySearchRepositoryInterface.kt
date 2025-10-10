package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionarySearchRepositoryInterface {
    suspend fun searchIdsByPrimaryText(searchTerm: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
    suspend fun searchIdsByKana(searchTerm: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
    suspend fun searchIdsByMeaning(searchTerm: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
    suspend fun searchIdsByWordClassId(wordClassId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
    suspend fun searchIdsByIsBookmark(isBookmark: Boolean, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
}