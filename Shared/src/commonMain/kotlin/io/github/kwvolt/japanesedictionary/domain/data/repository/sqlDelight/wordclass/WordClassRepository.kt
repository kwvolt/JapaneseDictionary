package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.wordclass.WordClassQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.WordClassIdContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.WordClassRepositoryInterface

class WordClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val wordClassQueries: WordClassQueries
) : WordClassRepositoryInterface {


    override suspend fun insertByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){wordClassQueries.insertByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull()}
    }

    override suspend fun insertByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){wordClassQueries.insertByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull()}
    }

    override suspend fun selectIdByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            wordClassQueries.selectIdByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectIdByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            wordClassQueries.selectIdByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        wordClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<WordClassIdContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){
            wordClassQueries.selectRow(wordClassId).awaitAsOneOrNull()
        }.map { result -> WordClassIdContainer(wordClassId, result.main_class_id, result.sub_class_id) }
    }

    override suspend fun updateMainClassId(
        wordClassId: Long,
        mainClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){wordClassQueries.updateMainClassId(wordClassId, mainClassId)}.map { Unit }
    }

    override suspend fun updateSubClassId(
        wordClassId: Long,
        subClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){wordClassQueries.updateSubClassId(wordClassId, subClassId)}
    }

    override suspend fun updateMainClassIdAndSubClassId(
        wordClassId: Long,
        mainClassId: Long,
        subClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){wordClassQueries.updateMainClassIdAndSubClassId(mainClassId, subClassId, wordClassId)}
    }

    override suspend fun deleteRowByWordClassId(
        wordClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){wordClassQueries.deleteRowByWordClassId(wordClassId)}
    }

    override suspend fun deleteRowByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){wordClassQueries.deleteRowByMainClassIdAndSubClassId(mainClassId, subClassId)}
    }

}