package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionaryNoteRepositoryInterface {
    suspend fun insert(dictionaryEntryId: Long, noteDescription: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, noteDescription: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deleteRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionaryEntryNoteContainer>
    suspend fun selectAllById(dictionaryEntryId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<DictionaryEntryNoteContainer>>

}
data class DictionaryEntryNoteContainer(val id: Long, val note: String)