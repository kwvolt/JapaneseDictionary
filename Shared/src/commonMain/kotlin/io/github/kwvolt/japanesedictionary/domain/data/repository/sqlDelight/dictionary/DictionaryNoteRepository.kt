package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryEntryNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryNoteRepositoryInterface

class DictionaryNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val dictionaryEntryNoteQueries: DictionaryEntryNoteQueries
): DictionaryNoteRepositoryInterface {
    override suspend fun insert(
        dictionaryEntryId: Long,
        noteDescription: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            dictionaryEntryNoteQueries.insert(
                dictionaryEntryId,
                noteDescription
            ).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        noteDescription: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){ dictionaryEntryNoteQueries.update(noteDescription, id) }
    }

    override suspend fun deleteRow(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId) { dictionaryEntryNoteQueries.deleteRow(id)}
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<DictionaryEntryNoteContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            dictionaryEntryNoteQueries.selectRow(id).awaitAsOneOrNull()
        }.map { result -> DictionaryEntryNoteContainer(id, result) }
    }

    override suspend fun selectAllById(
        dictionaryEntryId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<DictionaryEntryNoteContainer>> {
        return dbHandler.selectAll(itemId, returnNotFoundOnNull,
            queryBlock = { dictionaryEntryNoteQueries.selectNotesByEntryId(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionaryEntryNoteContainer(item.id, item.note_description) }
        )
    }
}