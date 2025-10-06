package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SectionKanaRepositoryInterface {
    /**
     * Inserts a Kana representation for the dictionary entry section.
     *
     * @param entryId The ID of the dictionary entry to link the Kana to.
     * @param sectionId The ID of the dictionary entry section to link the Kana to.
     * @param wordText The kana text to be inserted.
     * @return [DatabaseResult.Success] with the inserted Kana ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insert(entryId: Long, sectionId: Long, wordText: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves the Kana ID for a specific Kana text and entry component ID.
     *
     * @param sectionId The ID of the dictionary entry section.
     * @param wordText The Kana text.
     * @return [DatabaseResult.Success] with the Kana ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectId(sectionId: Long, wordText: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRow(id: Long, itemId: String? = null): DatabaseResult<DictionarySectionKanaContainer>

    /**
     * Retrieves all Kana representations linked to a specific dictionary entry section.
     *
     * @param sectionId The ID of the dictionary entry section.
     * @return [DatabaseResult.Success] with a list of Kana representations,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllBySectionId(sectionId: Long, itemId: String? = null): DatabaseResult<List<DictionarySectionKanaContainer>>

    /**
     * Updates the Kana text of a dictionary entry section.
     *
     * @param id The ID of the Kana entry to update.
     * @param wordText The new Kana text.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateKana(id: Long, wordText: String, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Deletes a Kana entry for a dictionary entry section.
     *
     * @param id The ID of the Kana entry to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteRow(id: Long, itemId: String? = null): DatabaseResult<Unit>

}

data class DictionarySectionKanaContainer(val id: Long, val wordText: String)