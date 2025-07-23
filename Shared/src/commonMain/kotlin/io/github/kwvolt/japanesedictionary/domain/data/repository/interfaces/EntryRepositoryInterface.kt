package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntryRepositoryInterface {

    /**
     * Inserts a new entry into the Dictionary Entry table.
     *
     * @param wordClassId The ID of the word class this word belongs to.
     * @param primaryText The main representation of the word (Kanji or Hiragana/Katakana).
     *                   Use Kanji unless unavailable or kana is more commonly used.
     * @return [DatabaseResult.Success] with the inserted ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun insert(wordClassId: Long, primaryText: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves the ID of a dictionary entry based on its primary text.
     *
     * @param primaryText The main representation of the word (Kanji or kana).
     * @return [DatabaseResult.Success] with the entry ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectIdByPrimaryText(primaryText: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves the word class and primary text of a dictionary entry based on its Id.
     *
     * @param id The ID of the entry to retrieve.
     * @return [DatabaseResult.Success] with the entry ID and primary text,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectRow(id: Long, itemId: String? = null): DatabaseResult<DictionaryEntryContainer>


    /**
     * Retrieves the bookmark boolean of a dictionary entry based on its Id
     *
     * @param id The ID of the bookmark to retrieve.
     * @return [DatabaseResult.Success] with [Boolean],
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectIsBookmarked(id: Long, itemId: String? = null): DatabaseResult<Boolean>

    /**
     * Updates the primary text of a dictionary entry.
     *
     * @param id The ID of the entry to update.
     * @param primaryText The new primary text (Kanji or kana).
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updatePrimaryText(id: Long, primaryText: String, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Updates the word class of a dictionary entry.
     *
     * @param id The ID of the entry to update.
     * @param wordClassId The new word class ID.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateWordClass(id: Long, wordClassId: Long, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Updates the bookmark of a dictionary entry.
     *
     * @param id The ID of the entry to update.
     * @param isBookmark The updated isBookmark
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateIsBookmark(id: Long, isBookmark: Boolean, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Updates the word class and primary text of a dictionary entry.
     *
     * @param id The ID of the entry to update.
     * @param wordClassId The new word class ID.
     * @param primaryText The new primary text.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateWordClassIdAndPrimaryText(id: Long, wordClassId: Long, primaryText: String, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Deletes a dictionary entry by its ID.
     *
     * @param id The ID of the entry to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteRow(id: Long, itemId: String? = null): DatabaseResult<Unit>
}
data class DictionaryEntryContainer(val id: Long, val wordClassId: Long, val primaryText: String)