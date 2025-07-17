package io.github.kwvolt.japanesedictionary.domain.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionKanaQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionKanaInterface

class EntrySectionKanaRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionKanaQueries: DictionaryEntrySectionKanaQueries
): EntrySectionKanaInterface {
    override suspend fun insert(
        sectionId: Long,
        wordText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){entrySectionKanaQueries.insert(sectionId, wordText).awaitAsOneOrNull()}
    }

    override suspend fun selectId(
        sectionId: Long,
        wordText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectId in EntrySectionKanaRepository For value dictionaryEntrySectionNoteId: $sectionId and wordText: $wordText"
        ){
            entrySectionKanaQueries.selectId(sectionId, wordText).awaitAsOneOrNull()
        }
    }

    override suspend fun selectAllBySectionId(
        sectionId: Long,
        itemId: String?
    ): DatabaseResult<List<DictionaryEntrySectionKanaContainer>> {
        return dbHandler.selectAll(itemId,
            "Error at selectAllBySectionId in EntrySectionKanaRepository for dictionaryEntrySectionId: $sectionId",
            queryBlock = { entrySectionKanaQueries.selectAllBySectionId(sectionId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionKanaContainer(item.id, item.wordText) }
        )
    }

    override suspend fun updateKana(
        id: Long,
        wordText: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionKanaQueries.updateKana(wordText, id)}
    }

    override suspend fun deleteRow(id: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){entrySectionKanaQueries.deleteRow(id)}
    }

}