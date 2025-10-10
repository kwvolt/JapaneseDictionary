package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SectionNoteRepositoryInterface {
    suspend fun insert(dictionaryEntrySectionId: Long, noteDescription: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, noteDescription: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionarySectionNoteContainer>
    suspend fun selectAllBySectionId(dictionaryEntrySectionId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<DictionarySectionNoteContainer>>
}

data class DictionarySectionNoteContainer(val id: Long, val note: String)