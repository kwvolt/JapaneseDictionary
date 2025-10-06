package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SectionNoteRepositoryInterface {

    /**
     * Inserts a note linked to a dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section to add the note to.
     * @param noteDescription The note content.
     * @return [DatabaseResult.Success] with the inserted note ID,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun insert(dictionaryEntrySectionId: Long, noteDescription: String, itemId: String? = null): DatabaseResult<Long>

    /**
     * Retrieves a specific section note by its ID within the Dictionary Entry Section Note.
     *
     * @param id The ID of the note to retrieve.
     * @return [DatabaseResult.Success] with the note,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectRow(id: Long, itemId: String? = null): DatabaseResult<DictionarySectionNoteContainer>

    /**
     * Retrieves all notes linked to a specific dictionary entry section.
     *
     * @param dictionaryEntrySectionId The ID of the dictionary entry section.
     * @return [DatabaseResult.Success] with a list of notes,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun selectAllBySectionId(dictionaryEntrySectionId: Long, itemId: String? = null): DatabaseResult<List<DictionarySectionNoteContainer>>

    /**
     * Updates a note linked to a dictionary entry section.
     *
     * @param id The ID of the note to update.
     * @param noteDescription The new content of the note.
     * @return [DatabaseResult.Success] if update was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun updateNoteDescription(id: Long, noteDescription: String, itemId: String? = null): DatabaseResult<Unit>

    /**
     * Deletes a note linked to a dictionary entry section.
     *
     * @param id The ID of the note to delete.
     * @return [DatabaseResult.Success] if deletion was successful,
     * [DatabaseResult.InvalidInput] if database Error occurs (unique constraint, foreign key, not null, not found)
     * [DatabaseResult.NotFound] if no value is returned and not tied to item else it returns InvalidInput
     * [DatabaseResult.UnknownError] if an error occurred.
     */
    suspend fun deleteRow(id: Long, itemId: String? = null): DatabaseResult<Unit>
}

data class DictionarySectionNoteContainer(val id: Long, val note: String)