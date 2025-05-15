package io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionKanaQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionNoteQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntrySectionQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.DictionaryEntryContainer

class EntrySectionRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionQueries: DictionaryEntrySectionQueries
): EntrySectionRepositoryInterface {

    override suspend fun insertDictionaryEntrySection(dictionaryEntryId:Long, meaning:String):DatabaseResult<Long> {
        return dbHandler.wrapResult(
                entrySectionQueries.insertDictionaryEntrySection(dictionaryEntryId, meaning)
                    .awaitAsOneOrNull()
        )
    }

    override suspend fun selectDictionaryEntrySectionId(dictionaryEntryId:Long, meaning:String): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectDictionaryEntrySectionId For value dictionaryEntryId: $dictionaryEntryId and meaning: $meaning"
        ) {
            entrySectionQueries.selectDictionaryEntrySectionId(dictionaryEntryId, meaning).awaitAsOneOrNull()
        }
    }

    override suspend fun selectDictionaryEntrySection(dictionaryEntryId: Long): DatabaseResult<DictionaryEntrySectionContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectDictionaryEntrySection For value dictionaryEntryId: $dictionaryEntryId"
        ){
            entrySectionQueries.selectDictionaryEntrySection(dictionaryEntryId).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(DictionaryEntrySectionContainer(result.dictionary_entry_id, result.meaning)) }
    }

    override suspend fun selectAllDictionaryEntrySectionByEntry(dictionaryEntryId: Long): DatabaseResult<List<DictionaryEntrySectionContainer>> {
        return dbHandler.selectAll(
            "Error in selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId for dictionaryEntryId: $dictionaryEntryId",
            queryBlock = { entrySectionQueries.selectAllDictionaryEntrySectionByEntry(dictionaryEntryId).awaitAsList() },
            mapper = { item -> DictionaryEntrySectionContainer(item.id, item.meaning) }
        )
    }

    override suspend fun updateDictionaryEntrySectionMeaning(dictionaryEntrySectionId: Long, newMeaning: String): DatabaseResult<Unit>{
        return dbHandler.wrapResult(entrySectionQueries.updateDictionaryEntrySectionMeaning(
                newMeaning,
                dictionaryEntrySectionId
            )
        )
    }

    override suspend fun deleteDictionaryEntrySection(dictionaryEntrySectionId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(entrySectionQueries.deleteDictionaryEntrySection(dictionaryEntrySectionId))
    }
}