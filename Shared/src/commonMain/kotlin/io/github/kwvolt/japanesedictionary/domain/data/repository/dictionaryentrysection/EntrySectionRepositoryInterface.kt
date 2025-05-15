package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

/**
 * Handles the access of Entry Section Component and other related tables that link to it
 */
interface EntrySectionRepositoryInterface {

    /**
     * Inserts a new section into the Dictionary Entry Section table.
     *
     * @param dictionaryEntryId The ID of the Dictionary Entry this word belongs to.
     * @param meaning The meaning of the word (specific to the context within this component).
     * @return [DatabaseResult.Success] with the inserted ID,
     * [DatabaseResult.NotFound] if no entry exists (ideally should never happen),
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun insertDictionaryEntrySection(dictionaryEntryId: Long, meaning: String): DatabaseResult<Long>

    /**
     * Retrieves the ID of a dictionary entry section based on its dictionary entry ID and meaning.
     *
     * @param dictionaryEntryId The ID of the Dictionary Entry this word belongs to.
     * @param meaning The meaning of the word (specific to the context within this component).
     * @return [DatabaseResult.Success] with the section ID,
     * [DatabaseResult.NotFound] if no entry exists,
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun selectDictionaryEntrySectionId(dictionaryEntryId: Long, meaning: String): DatabaseResult<Long>

    suspend fun selectDictionaryEntrySection(dictionaryEntryId: Long): DatabaseResult<DictionaryEntrySectionContainer>

    /**
     * Retrieves all entry section linked to a specific dictionary entry.
     *
     * @param dictionaryEntryId The ID of the entry to retrieve from.
     * @return [DatabaseResult.Success] with the entry section id and meaning,
     * [DatabaseResult.NotFound] if no entry exists,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllDictionaryEntrySectionByEntry(dictionaryEntryId: Long): DatabaseResult<List<DictionaryEntrySectionContainer>>

    /**
     * Deletes a dictionary entry section by its ID.
     *
     * @param dictionaryEntrySectionId The ID of the section to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteDictionaryEntrySection(dictionaryEntrySectionId: Long): DatabaseResult<Unit>

    /**
     * Updates the meaning text of a dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the section to update.
     * @param newMeaning The new meaning text.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntrySectionMeaning(dictionaryEntrySectionId: Long, newMeaning: String): DatabaseResult<Unit>

}
data class DictionaryEntrySectionContainer(val id: Long, val meaning: String)