package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionaryRepositoryInterface {
    suspend fun insert(wordClassId: Long, primaryText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun delete(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun update(id: Long, wordClassId: Long?=null, primaryText: String?=null, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectIdByPrimaryText(primaryText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionaryEntryContainer>
}
data class DictionaryEntryContainer(val id: Long, val wordClassId: Long, val primaryText: String)