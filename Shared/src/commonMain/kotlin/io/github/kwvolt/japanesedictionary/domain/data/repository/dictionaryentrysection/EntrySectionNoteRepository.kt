package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionNoteQueries

class EntrySectionNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionNoteQueries: DictionaryEntrySectionNoteQueries
): EntrySectionNoteRepositoryInterface {
    override suspend fun insertDictionaryEntrySectionNote(
        dictionaryEntrySectionId: Long,
        noteDescription: String
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(entrySectionNoteQueries.insertDictionaryEntrySectionNote(dictionaryEntrySectionId, noteDescription).awaitAsOneOrNull())
    }

    override suspend fun selectDictionaryEntrySectionNoteById(dictionaryEntrySectionNoteId: Long): DatabaseResult<String> {
        return dbHandler.withContextDispatcherWithException("Error in selectDictionaryEntrySectionNoteById for dictionaryEntrySectionNoteId: $dictionaryEntrySectionNoteId"){
            entrySectionNoteQueries.selectDictionaryEntrySectionNoteById(dictionaryEntrySectionNoteId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId(
        dictionaryEntrySectionId: Long
    ): DatabaseResult<List<DictionaryEntrySectionNoteContainer>> {
        return dbHandler.selectAll(
            "Error in selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId for dictionaryEntrySectionId: $dictionaryEntrySectionId",
            queryBlock = { entrySectionNoteQueries.selectDictionaryEntrySectionNoteByEntry(dictionaryEntrySectionId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionNoteContainer(item.id, item.note_description) }
        )
    }

    override suspend fun updateDictionaryEntrySectionNote(
        dictionaryEntrySectionNoteId: Long,
        newNoteDescription: String
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(entrySectionNoteQueries.updateDictionaryEntrySectionNote(newNoteDescription, dictionaryEntrySectionNoteId))
    }

    override suspend fun deleteDictionaryEntrySectionNote(dictionaryEntrySectionNoteId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(entrySectionNoteQueries.deleteDictionaryEntrySectionNote(
                    dictionaryEntrySectionNoteId
                )
        )
    }

}