package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

/**
 * Handles the access of Entry Section Component and other related tables that link to it
 */
interface SectionRepositoryInterface {
    suspend fun insert(dictionaryEntryId: Long, meaning: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun delete(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun update(id: Long, meaning: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(dictionaryEntryId: Long, meaning: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(dictionaryEntryId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionarySectionContainer>
    suspend fun selectAllByEntryId(dictionaryEntryId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<DictionarySectionContainer>>

}
data class DictionarySectionContainer(val id: Long, val meaning: String)