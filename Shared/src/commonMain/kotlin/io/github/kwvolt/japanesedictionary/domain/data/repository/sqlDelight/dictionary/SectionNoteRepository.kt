package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryEntrySectionNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionNoteRepositoryInterface

class SectionNoteRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionNoteQueries: DictionaryEntrySectionNoteQueries
): SectionNoteRepositoryInterface {
    override suspend fun insert(
        dictionaryEntrySectionId: Long,
        noteDescription: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){entrySectionNoteQueries.insert(dictionaryEntrySectionId, noteDescription).awaitAsOneOrNull()}
    }

    override suspend fun update(
        id: Long,
        noteDescription: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){entrySectionNoteQueries.updateNoteDescription(noteDescription, id)}.map { Unit }
    }

    override suspend fun delete(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){entrySectionNoteQueries.deleteRow(id)}.map { Unit }
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<DictionarySectionNoteContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            entrySectionNoteQueries.selectRow(id).awaitAsOneOrNull()
        }.map {
            result ->
            DictionarySectionNoteContainer(id, result)
        }
    }

    override suspend fun selectAllBySectionId(
        dictionaryEntrySectionId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<DictionarySectionNoteContainer>> {
        return dbHandler.selectAllToList(itemId,returnNotFoundOnNull,
            queryBlock = { entrySectionNoteQueries.selectAllBySectionId(dictionaryEntrySectionId).awaitAsList() },
            mapper = { item -> DictionarySectionNoteContainer(item.id, item.note_description) }
        )
    }

}