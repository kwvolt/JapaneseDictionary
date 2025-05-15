package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntrySectionKanaInterface {
    /**
     * Inserts a Kana representation for the dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section to link the Kana to.
     * @param wordText The kana text to be inserted.
     * @return [DatabaseResult.Success] with the inserted Kana ID,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insertDictionaryEntrySectionKana(dictionaryEntrySectionId: Long, wordText: String): DatabaseResult<Long>

    /**
     * Retrieves the Kana ID for a specific Kana text and entry component ID.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section.
     * @param wordText The Kana text.
     * @return [DatabaseResult.Success] with the Kana ID,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectDictionaryEntrySectionKanaId(dictionaryEntrySectionId: Long, wordText: String): DatabaseResult<Long>

    /**
     * Retrieves all Kana representations linked to a specific dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section.
     * @return [DatabaseResult.Success] with a list of Kana representations,
     * [DatabaseResult.NotFound] if no Kana representations exist,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllKanaByDictionaryEntrySectionId(dictionaryEntrySectionId: Long): DatabaseResult<List<DictionaryEntrySectionKanaContainer>>

    /**
     * Updates the Kana text of a dictionary entry section.
     *
     * @param kanaId The ID of the Kana entry to update.
     * @param newWordText The new Kana text.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntrySectionKana(kanaId: Long, newWordText: String): DatabaseResult<Unit>

    /**
     * Deletes a Kana entry for a dictionary entry section.
     *
     * @param kanaId The ID of the Kana entry to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteDictionaryEntrySectionKana(kanaId: Long): DatabaseResult<Unit>

}

data class DictionaryEntrySectionKanaContainer(val id: Long, val wordText: String)