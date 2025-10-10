package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SectionKanaRepositoryInterface {
    suspend fun insert(entryId: Long, sectionId: Long, wordText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, wordText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(sectionId: Long, wordText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionarySectionKanaContainer>
    suspend fun selectAllBySectionId(sectionId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<DictionarySectionKanaContainer>>
}

data class DictionarySectionKanaContainer(val id: Long, val wordText: String)