package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionNoteRepositoryInterface

class EntrySectionNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionNoteQueries: DictionaryEntrySectionNoteQueries
): EntrySectionNoteRepositoryInterface {
    override suspend fun insert(
        dictionaryEntrySectionId: Long,
        noteDescription: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){entrySectionNoteQueries.insert(dictionaryEntrySectionId, noteDescription).awaitAsOneOrNull()}
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?
    ): DatabaseResult<DictionaryEntrySectionNoteContainer> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectRow in EntrySectionNoteRepository for id: $id"){
            entrySectionNoteQueries.selectRow(id).awaitAsOneOrNull()
        }.map {
            result -> DictionaryEntrySectionNoteContainer(id, result)
        }
    }

    override suspend fun selectAllBySectionId(
        dictionaryEntrySectionId: Long,
        itemId: String?
    ): DatabaseResult<List<DictionaryEntrySectionNoteContainer>> {
        return dbHandler.selectAll(itemId,
            "Error at selectAllBySectionId in EntrySectionNoteRepository for dictionaryEntrySectionId: $dictionaryEntrySectionId",
            queryBlock = { entrySectionNoteQueries.selectAllBySectionId(dictionaryEntrySectionId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionNoteContainer(item.id, item.note_description) }
        )
    }

    override suspend fun updateNoteDescription(
        id: Long,
        noteDescription: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionNoteQueries.updateNoteDescription(noteDescription, id)}
    }

    override suspend fun deleteRow(id: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionNoteQueries.deleteRow(id)}
    }

}