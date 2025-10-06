package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionNoteRepositoryInterface

class SectionNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionNoteQueries: DictionaryEntrySectionNoteQueries
): SectionNoteRepositoryInterface {
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
    ): DatabaseResult<DictionarySectionNoteContainer> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectRow in EntrySectionNoteRepository for id: $id"){
            entrySectionNoteQueries.selectRow(id).awaitAsOneOrNull()
        }.map {
            result -> DictionarySectionNoteContainer(id, result)
        }
    }

    override suspend fun selectAllBySectionId(
        dictionaryEntrySectionId: Long,
        itemId: String?
    ): DatabaseResult<List<DictionarySectionNoteContainer>> {
        return dbHandler.selectAll(itemId,
            "Error at selectAllBySectionId in EntrySectionNoteRepository for dictionaryEntrySectionId: $dictionaryEntrySectionId",
            queryBlock = { entrySectionNoteQueries.selectAllBySectionId(dictionaryEntrySectionId).awaitAsList() },
            mapper = { item -> DictionarySectionNoteContainer(item.id, item.note_description) }
        )
    }

    override suspend fun updateNoteDescription(
        id: Long,
        noteDescription: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionNoteQueries.updateNoteDescription(noteDescription, id)}.map { Unit }
    }

    override suspend fun deleteRow(id: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionNoteQueries.deleteRow(id)}.map { Unit }
    }

}