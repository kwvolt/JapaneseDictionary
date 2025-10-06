package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.wordclass.WordClassQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassIdContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface

class WordClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val wordClassQueries: WordClassQueries
) : WordClassRepositoryInterface {


    override suspend fun insertByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.insertByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull()}
    }

    override suspend fun insertByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.insertByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull()}
    }

    override suspend fun selectIdByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectIdByMainClassIdNameAndSubClassIdName For value mainClassId: $mainClassId and subClassId: $subClassId"
        ){wordClassQueries.selectIdByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectIdByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectIdByMainClassIdNameAndSubClassIdName For value mainClassIdName: $mainClassIdName and subClassIdName: $subClassIdName"
        ){
            wordClassQueries.selectIdByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        wordClassId: Long,
        itemId: String?
    ): DatabaseResult<WordClassIdContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectWordClassMainClassIdAndSubClassIdByWordClassId For value wordClassId: $wordClassId"
        ){
            wordClassQueries.selectRow(wordClassId).awaitAsOneOrNull()
        }.map { result -> WordClassIdContainer(wordClassId, result.main_class_id, result.sub_class_id) }
    }

    override suspend fun updateMainClassId(
        wordClassId: Long,
        mainClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.updateMainClassId(wordClassId, mainClassId)}.map { Unit }
    }

    override suspend fun updateSubClassId(
        wordClassId: Long,
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.updateSubClassId(wordClassId, subClassId)}.map { Unit }
    }

    override suspend fun updateMainClassIdAndSubClassId(
        wordClassId: Long,
        mainClassId: Long,
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.updateMainClassIdAndSubClassId(mainClassId, subClassId, wordClassId)}.map { Unit }
    }

    override suspend fun deleteRowByWordClassId(
        wordClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.deleteRowByWordClassId(wordClassId)}.map { Unit }
    }

    override suspend fun deleteRowByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){wordClassQueries.deleteRowByMainClassIdAndSubClassId(mainClassId, subClassId)}.map { Unit }
    }

}