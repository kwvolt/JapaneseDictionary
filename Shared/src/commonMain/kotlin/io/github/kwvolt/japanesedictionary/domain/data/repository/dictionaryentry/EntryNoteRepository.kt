package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntryNoteQueries

class EntryNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val dictionaryEntryNoteQueries: DictionaryEntryNoteQueries
): EntryNoteRepositoryInterface {
    override suspend fun insertDictionaryEntryNote(dictionaryEntryId:Long, noteDescription:String):DatabaseResult<Long> {
        return dbHandler.wrapResult(dictionaryEntryNoteQueries.insertDictionaryEntryNote(
                    dictionaryEntryId,
                    noteDescription
                ).awaitAsOneOrNull())
    }

    override suspend fun selectDictionaryEntryNoteById(dictionaryEntryNoteId: Long): DatabaseResult<String> {
        return dbHandler.withContextDispatcherWithException("Error in selectDictionaryEntryNoteById for dictionaryEntryNoteId: $dictionaryEntryNoteId"){
            dictionaryEntryNoteQueries.selectDictionaryEntryNoteById(dictionaryEntryNoteId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectAllDictionaryEntryNoteByDictionaryEntryId(dictionaryEntryId: Long): DatabaseResult<List<DictionaryEntryNoteContainer>> {
        return dbHandler.selectAll(
            "Error in selectDictionaryEntryNoteByEntry for dictionaryEntryId: $dictionaryEntryId",
            queryBlock = { dictionaryEntryNoteQueries.selectDictionaryEntryNoteByEntry(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionaryEntryNoteContainer(item.id, item.note_description) }
        )
    }

    override suspend fun updateDictionaryEntryNote(
        dictionaryEntryNoteId: Long,
        newNoteDescription: String
    ):DatabaseResult<Unit> {
        return dbHandler.wrapResult(dictionaryEntryNoteQueries.updateDictionaryEntryNote(newNoteDescription, dictionaryEntryNoteId))
    }

    override suspend fun deleteDictionaryEntryNote(dictionaryEntryNoteId: Long):DatabaseResult<Unit> {
        return dbHandler.wrapResult(dictionaryEntryNoteQueries.deleteDictionaryEntryNote(dictionaryEntryNoteId))
    }
}