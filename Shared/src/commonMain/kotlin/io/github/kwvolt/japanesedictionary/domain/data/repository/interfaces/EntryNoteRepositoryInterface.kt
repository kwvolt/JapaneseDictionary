package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface EntryNoteRepositoryInterface {
    /**
     * Inserts a note linked to a dictionary entry.
     *
     * @param dictionaryEntryId The ID of the dictionary entry to add the note to.
     * @param noteDescription The note content.
     * @return [DatabaseResult.Success] with the inserted note ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insert(dictionaryEntryId: Long, noteDescription: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves a specific note by its ID within the Dictionary Entry Note.
     *
     * @param id The ID of the note to retrieve.
     * @return [DatabaseResult.Success] with the [DictionaryEntryNoteContainer],
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectRow(id: Long, itemId: String? = null): DatabaseResult<DictionaryEntryNoteContainer>

    /**
     * Retrieves all notes linked to a specific dictionary entry.
     *
     * @param dictionaryEntryId The ID of the dictionary entry.
     * @return [DatabaseResult.Success] with a list of notes,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllById(dictionaryEntryId: Long, itemId: String? = null): DatabaseResult<List<DictionaryEntryNoteContainer>>

    /**
     * Updates a note on a dictionary entry.
     *
     * @param id The ID of the note to update.
     * @param newNoteDescription The new content of the note.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateNoteDescription(id: Long, newNoteDescription: String, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Deletes a note by its ID.
     *
     * @param id The ID of the note to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteRow(id: Long, itemId: String? = null): DatabaseResult<Unit>
}
data class DictionaryEntryNoteContainer(val id: Long, val note: String)