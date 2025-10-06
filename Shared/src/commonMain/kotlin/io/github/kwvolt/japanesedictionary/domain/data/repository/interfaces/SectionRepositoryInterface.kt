package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

/**
 * Handles the access of Entry Section Component and other related tables that link to it
 */
interface SectionRepositoryInterface {

    /**
     * Inserts a new section into the Dictionary Entry Section table.
     *
     * @param dictionaryEntryId The ID of the Dictionary Entry this word belongs to.
     * @param meaning The meaning of the word (specific to the context within this component).
     * @return [DatabaseResult.Success] with the inserted ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun insert(dictionaryEntryId: Long, meaning: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves the ID of a dictionary entry section based on its dictionary entry ID and meaning.
     *
     * @param dictionaryEntryId The ID of the Dictionary Entry this word belongs to.
     * @param meaning The meaning of the word (specific to the context within this component).
     * @return [DatabaseResult.Success] with the section ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun selectId(dictionaryEntryId: Long, meaning: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRow(dictionaryEntryId: Long, itemId: String? = null): DatabaseResult<DictionarySectionContainer>

    /**
     * Retrieves all entry section linked to a specific dictionary entry.
     *
     * @param dictionaryEntryId The ID of the entry to retrieve from.
     * @return [DatabaseResult.Success] with the entry section id and meaning,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllByEntryId(dictionaryEntryId: Long, itemId: String? = null): DatabaseResult<List<DictionarySectionContainer>>

    /**
     * Deletes a dictionary entry section by its ID.
     *
     * @param dictionaryEntrySectionId The ID of the section to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteRow(dictionaryEntrySectionId: Long, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Updates the meaning text of a dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the section to update.
     * @param newMeaning The new meaning text.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateMeaning(dictionaryEntrySectionId: Long, newMeaning: String, itemId: String? = null): DatabaseResult<Unit>

}
data class DictionarySectionContainer(val id: Long, val meaning: String)