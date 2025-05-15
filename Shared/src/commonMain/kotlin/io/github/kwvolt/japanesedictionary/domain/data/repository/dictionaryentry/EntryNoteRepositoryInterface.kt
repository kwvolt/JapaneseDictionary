package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntryNoteRepositoryInterface {
    /**
     * Inserts a note linked to a dictionary entry.
     *
     * @param dictionaryEntryId The ID of the dictionary entry to add the note to.
     * @param noteDescription The note content.
     * @return [DatabaseResult.Success] with the inserted note ID,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insertDictionaryEntryNote(dictionaryEntryId: Long, noteDescription: String): DatabaseResult<Long>

    /**
     * Retrieves a specific note by its ID within the Dictionary Entry Note.
     *
     * @param dictionaryEntryNoteId The ID of the note to retrieve.
     * @return [DatabaseResult.Success] with the note,
     * [DatabaseResult.NotFound] if the note doesn't exist,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectDictionaryEntryNoteById(dictionaryEntryNoteId: Long): DatabaseResult<String>

    /**
     * Retrieves all notes linked to a specific dictionary entry.
     *
     * @param dictionaryEntryId The ID of the dictionary entry.
     * @return [DatabaseResult.Success] with a list of notes,
     * [DatabaseResult.NotFound] if no notes are found (if there is at least one null/invalid value within the notes or invalid DictionaryEntryId)),
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllDictionaryEntryNoteByDictionaryEntryId(dictionaryEntryId: Long): DatabaseResult<List<DictionaryEntryNoteContainer>>

    /**
     * Updates a note on a dictionary entry.
     *
     * @param dictionaryEntryNoteId The ID of the note to update.
     * @param newNoteDescription The new content of the note.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateDictionaryEntryNote(dictionaryEntryNoteId: Long, newNoteDescription: String): DatabaseResult<Unit>

    /**
     * Deletes a note by its ID.
     *
     * @param dictionaryEntryNoteId The ID of the note to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteDictionaryEntryNote(dictionaryEntryNoteId: Long): DatabaseResult<Unit>
}
data class DictionaryEntryNoteContainer(val id: Long, val note: String)