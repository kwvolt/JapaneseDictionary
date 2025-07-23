package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryEntryQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntryRepositoryInterface

class EntryRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: DictionaryEntryQueries
): EntryRepositoryInterface {
    override suspend fun insert(
        wordClassId: Long,
        primaryText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){queries.insert(wordClassId, primaryText).awaitAsOneOrNull()}
    }

    override suspend fun selectIdByPrimaryText(
        primaryText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            itemId,
            "Error at selectIdByPrimaryText in EntryRepository For value PrimaryText: $primaryText"
        ){
                queries.selectIdByPrimaryText(primaryText).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        itemId: String?
    ): DatabaseResult<DictionaryEntryContainer> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectRow in EntryRepository For value dictionaryEntryId: $id"){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map { result -> DictionaryEntryContainer(id, result.word_class_id, result.primary_text) }
    }

    override suspend fun selectIsBookmarked(id: Long, itemId: String?): DatabaseResult<Boolean> {
        return dbHandler.withContextDispatcherWithException(itemId, "Error at selectIsBookmarked in EntryRepository For value dictionaryEntryId: $id"){
            queries.selectIsBookmark(id).awaitAsOneOrNull()
        }.map { result -> result != 0L}
    }

    override suspend fun updatePrimaryText(
        id: Long,
        primaryText: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){ queries.updatePrimaryText(primaryText, id)}
    }

    override suspend fun updateWordClass(
        id: Long,
        wordClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) {queries.updateWordClass(wordClassId, id)}
    }

    override suspend fun updateIsBookmark(
        id: Long,
        isBookmark: Boolean,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) { queries.updateIsBookmark(if (isBookmark) 1L else 0L, id) }
    }

    override suspend fun updateWordClassIdAndPrimaryText(
        id: Long,
        wordClassId: Long,
        primaryText: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId) {queries.updateWordClassIdAndPrimaryText(wordClassId, primaryText, id) }
    }

    override suspend fun deleteRow(id: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){queries.deleteRow(id)}
    }
}