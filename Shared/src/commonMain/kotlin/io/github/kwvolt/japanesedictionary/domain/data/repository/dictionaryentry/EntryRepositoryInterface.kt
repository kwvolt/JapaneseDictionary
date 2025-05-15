package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntryRepositoryInterface {

    /**
     * Inserts a new entry into the Dictionary Entry table.
     *
     * @param wordClassId The ID of the word class this word belongs to.
     * @param primaryText The main representation of the word (Kanji or Hiragana/Katakana).
     *                   Use Kanji unless unavailable or kana is more commonly used.
     * @return [DatabaseResult.Success] with the inserted ID,
     * [DatabaseResult.InvalidInput] if the input is invalid,
     * [DatabaseResult.NotFound] if no entry exists (ideally should never happen),
     * [DatabaseResult.UnknownError] if an exception occurred.
     */
    suspend fun insertDictionaryEntry(wordClassId: Long, primaryText: String): DatabaseResult<Long>

    /**
     * Retrieves the ID of a dictionary entry based on its primary text.
     *
     * @param primaryText The main representation of the word (Kanji or kana).
     * @return [DatabaseResult.Success] with the entry ID,
     * [DatabaseResult.InvalidInput] if the searched for Text is invalid,
     * [DatabaseResult.NotFound] if no entry exists,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectDictionaryEntryId(primaryText: String): DatabaseResult<Long>

    /**
     * Retrieves the word class and primary text of a dictionary entry based on its Id.
     *
     * @param dictionaryEntryId The ID of the entry to retrieve.
     * @return [DatabaseResult.Success] with the entry ID and primary text,
     * [DatabaseResult.NotFound] if no entry exists,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectDictionaryEntry(dictionaryEntryId: Long): DatabaseResult<DictionaryEntryContainer>

    /**
     * Updates the primary text of a dictionary entry.
     *
     * @param dictionaryEntryId The ID of the entry to update.
     * @param newPrimaryText The new primary text (Kanji or kana).
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntryPrimaryText(dictionaryEntryId: Long, newPrimaryText: String): DatabaseResult<Unit>

    /**
     * Updates the word class of a dictionary entry.
     *
     * @param dictionaryEntryId The ID of the entry to update.
     * @param newWordClassId The new word class ID.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntryWordClass(dictionaryEntryId: Long, newWordClassId: Long): DatabaseResult<Unit>

    /**
     * Deletes a dictionary entry by its ID.
     *
     * @param dictionaryEntryId The ID of the entry to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteDictionaryEntry(dictionaryEntryId: Long): DatabaseResult<Unit>
}
data class DictionaryEntryContainer(val id: Long, val wordClassId: Long, val primaryText: String)