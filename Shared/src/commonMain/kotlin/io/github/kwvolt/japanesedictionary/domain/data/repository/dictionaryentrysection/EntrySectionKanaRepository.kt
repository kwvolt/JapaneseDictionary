package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionKanaQueries

class EntrySectionKanaRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionKanaQueries: DictionaryEntrySectionKanaQueries
): EntrySectionKanaInterface {
    override suspend fun insertDictionaryEntrySectionKana(
        dictionaryEntrySectionId: Long,
        wordText: String
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(entrySectionKanaQueries.insertDictionaryEntrySectionKana(dictionaryEntrySectionId, wordText).awaitAsOneOrNull())
    }

    override suspend fun selectDictionaryEntrySectionKanaId(
        dictionaryEntrySectionId: Long,
        wordText: String
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectDictionaryEntrySectionKanaId For value dictionaryEntrySectionNoteId: $dictionaryEntrySectionId and wordText: $wordText"
        ){
            entrySectionKanaQueries.selectDictionaryEntrySectionKanaId(dictionaryEntrySectionId, wordText).awaitAsOneOrNull()
        }
    }

    override suspend fun selectAllKanaByDictionaryEntrySectionId(dictionaryEntrySectionId: Long): DatabaseResult<List<DictionaryEntrySectionKanaContainer>> {
        return dbHandler.selectAll(
            "Error in selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId for dictionaryEntrySectionId: $dictionaryEntrySectionId",
            queryBlock = { entrySectionKanaQueries.selectDictionaryEntrySectionKanaByEntry(dictionaryEntrySectionId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionKanaContainer(item.id, item.wordText) }
        )
    }

    override suspend fun updateDictionaryEntrySectionKana(
        kanaId: Long,
        newWordText: String
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(entrySectionKanaQueries.updateDictionaryEntrySectionKana(newWordText, kanaId))
    }

    override suspend fun deleteDictionaryEntrySectionKana(kanaId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(entrySectionKanaQueries.deleteDictionaryEntrySectionKana(kanaId))
    }

}