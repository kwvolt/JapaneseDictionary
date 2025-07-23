package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntryNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntryNoteRepositoryInterface

class EntryNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val dictionaryEntryNoteQueries: DictionaryEntryNoteQueries
): EntryNoteRepositoryInterface {
    override suspend fun insert(
        dictionaryEntryId: Long,
        noteDescription: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId) {
            dictionaryEntryNoteQueries.insert(
                dictionaryEntryId,
                noteDescription
            ).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?
    ): DatabaseResult<DictionaryEntryNoteContainer> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectRow in EntryNoteRepository for dictionaryEntryNoteId: $id"){
            dictionaryEntryNoteQueries.selectRow(id).awaitAsOneOrNull()
        }.map { result -> DictionaryEntryNoteContainer(id, result) }
    }

    override suspend fun selectAllById(
        dictionaryEntryId: Long,
        itemId: String?
    ): DatabaseResult<List<DictionaryEntryNoteContainer>> {
        return dbHandler.selectAll(itemId,
            "Error at selectAllById in EntryNoteRepository for dictionaryEntryId: $dictionaryEntryId",
            queryBlock = { dictionaryEntryNoteQueries.selectNotesByEntryId(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionaryEntryNoteContainer(item.id, item.note_description) }
        )
    }

    override suspend fun updateNoteDescription(
        id: Long,
        newNoteDescription: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){ dictionaryEntryNoteQueries.updateNoteDescription(newNoteDescription, id) }
    }

    override suspend fun deleteRow(id: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) { dictionaryEntryNoteQueries.deleteRow(id)}
    }
}