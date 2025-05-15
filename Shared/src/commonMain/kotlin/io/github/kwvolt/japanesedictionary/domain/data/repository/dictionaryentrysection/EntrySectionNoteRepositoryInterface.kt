package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntrySectionNoteRepositoryInterface {

    /**
     * Inserts a note linked to a dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section to add the note to.
     * @param noteDescription The note content.
     * @return [DatabaseResult.Success] with the inserted note ID,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insertDictionaryEntrySectionNote(dictionaryEntrySectionId: Long, noteDescription: String): DatabaseResult<Long>

    /**
     * Retrieves a specific section note by its ID within the Dictionary Entry Section Note.
     *
     * @param dictionaryEntrySectionNoteId The ID of the note to retrieve.
     * @return [DatabaseResult.Success] with the note,
     * [DatabaseResult.NotFound] if the note doesn't exist,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectDictionaryEntrySectionNoteById(dictionaryEntrySectionNoteId: Long): DatabaseResult<String>

    /**
     * Retrieves all notes linked to a specific dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section.
     * @return [DatabaseResult.Success] with a list of notes,
     * [DatabaseResult.NotFound] if no notes are found,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId(dictionaryEntrySectionId: Long): DatabaseResult<List<DictionaryEntrySectionNoteContainer>>

    /**
     * Updates a note linked to a dictionary entry section.
     *
     * @param dictionaryEntrySectionNoteId The ID of the note to update.
     * @param newNoteDescription The new content of the note.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntrySectionNote(dictionaryEntrySectionNoteId: Long, newNoteDescription: String): DatabaseResult<Unit>

    /**
     * Deletes a note linked to a dictionary entry section.
     *
     * @param dictionaryEntrySectionNoteId The ID of the note to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteDictionaryEntrySectionNote(dictionaryEntrySectionNoteId: Long): DatabaseResult<Unit>



}

data class DictionaryEntrySectionNoteContainer(val id: Long, val note: String)