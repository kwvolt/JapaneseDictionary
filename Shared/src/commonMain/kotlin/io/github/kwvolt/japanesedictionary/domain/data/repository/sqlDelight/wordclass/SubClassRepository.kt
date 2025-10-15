package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.wordclass.SubClassQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassRepositoryInterface

class SubClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val subClassQueries: SubClassQueries
): SubClassRepositoryInterface {
    
    override suspend fun insertLinkToMainClass(
        mainClassID: Long,
        idName: String,
        displayText: String,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){subClassQueries.insertLinkToMainClass(idName, displayText, mainClassID).awaitAsOneOrNull()}
    }

    override suspend fun insert(
        idName: String,
        displayText: String,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){subClassQueries.insert(idName, displayText).awaitAsOneOrNull()}
    }

    override suspend fun selectId(
        idName: String,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {subClassQueries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRowById(
        subClassId: Long,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<SubClassContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {subClassQueries.selectRowById(subClassId).awaitAsOneOrNull()
        }.map { result -> SubClassContainer(subClassId, result.id_name, result.display_text) }
    }

    override suspend fun selectRowByIdName(
        idName: String,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<SubClassContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            subClassQueries.selectRowByIdName(idName).awaitAsOneOrNull()
        }.map { result -> SubClassContainer(result.id, idName, result.display_text)
        }
    }

    override suspend fun selectAllByMainClassId(
        mainClassId: Long,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<List<SubClassContainer>> {
        return dbHandler.selectAll(itemId, returnNotFoundOnNull,
            queryBlock = { subClassQueries.selectAllByMainClassId(mainClassId).awaitAsList() },
            mapper = {result -> SubClassContainer(result.id, result.id_name, result.display_text) }
        )
    }

    override suspend fun update(
        subClassId: Long,
        idName: String?,
        displayText: String?,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){subClassQueries.update(idName, displayText, subClassId)}
    }


    override suspend fun delete(
        subClassId: Long,
        returnNotFoundOnNull: Boolean,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){subClassQueries.delete(subClassId)}
    }

}