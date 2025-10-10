package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.dictionary.DictionaryEntrySectionKanaQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionKanaRepositoryInterface

class SectionKanaRepository(
    private val dbHandler: DatabaseHandler,
    private val entrySectionKanaQueries: DictionaryEntrySectionKanaQueries
): SectionKanaRepositoryInterface {
    override suspend fun insert(
        entryId: Long,
        sectionId: Long,
        wordText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){entrySectionKanaQueries.insert(sectionId, entryId, wordText).awaitAsOneOrNull()}
    }

    override suspend fun update(
        id: Long,
        wordText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId){entrySectionKanaQueries.updateKana(wordText, id)}
    }

    override suspend fun delete(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){entrySectionKanaQueries.deleteRow(id)}
    }

    override suspend fun selectId(
        sectionId: Long,
        wordText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            entrySectionKanaQueries.selectId(sectionId, wordText).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<DictionarySectionKanaContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            entrySectionKanaQueries.selectRow(id).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(
            DictionarySectionKanaContainer(
                result.id,
                result.wordText
            )
        ) }
    }

    override suspend fun selectAllBySectionId(
        sectionId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<DictionarySectionKanaContainer>> {
        return dbHandler.selectAll(itemId, returnNotFoundOnNull,
            queryBlock = { entrySectionKanaQueries.selectAllBySectionId(sectionId).awaitAsList() },
            mapper = { item -> DictionarySectionKanaContainer(item.id, item.wordText) }
        )
    }

}