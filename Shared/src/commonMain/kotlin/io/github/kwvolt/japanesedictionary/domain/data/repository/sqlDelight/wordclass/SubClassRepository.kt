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
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){subClassQueries.insertLinkToMainClass(idName, displayText, mainClassID).awaitAsOneOrNull()}
    }

    override suspend fun insert(
        idName: String,
        displayText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){subClassQueries.insert(idName, displayText).awaitAsOneOrNull()}
    }

    override suspend fun selectId(idName: String, itemId: String?): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectSubClassId For value idName: $idName"
        ) {
            subClassQueries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRowById(
        subClassId: Long,
        itemId: String?
    ): DatabaseResult<SubClassContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectSubClassById For value subClassId: $subClassId"
        ) {
            subClassQueries.selectRowById(subClassId).awaitAsOneOrNull()
        }.map { result -> SubClassContainer(subClassId, result.id_name, result.display_text) }
    }

    override suspend fun selectRowByIdName(
        idName: String,
        itemId: String?
    ): DatabaseResult<SubClassContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectSubClassByIdName For value idName: $idName"
        ) {
            subClassQueries.selectRowByIdName(idName).awaitAsOneOrNull()
        }.map { result -> SubClassContainer(result.id, idName, result.display_text)
        }
    }

    override suspend fun selectAllByMainClassId(
        mainClassId: Long,
        itemId: String?
    ): DatabaseResult<List<SubClassContainer>> {
        return dbHandler.selectAll(itemId,
            "Error at selectAllSubClassByMainClassId For value mainClassId: $mainClassId",
            queryBlock = { subClassQueries.selectAllByMainClassId(mainClassId).awaitAsList() },
            mapper = {result -> SubClassContainer(result.id, result.id_name, result.display_text) }
        )
    }


    override suspend fun updateIdName(
        subClassId: Long,
        idName: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){subClassQueries.updateIdName(idName, subClassId)}.map { Unit }
    }

    override suspend fun updateDisplayText(
        subClassId: Long,
        displayText: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){subClassQueries.updateDisplayText(displayText, subClassId)}.map { Unit }
    }

    override suspend fun deleteRowByIdName(idName: String, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){subClassQueries.deleteRowByIdName(idName)}.map { Unit }
    }

    override suspend fun deleteRowById(subClassId: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){subClassQueries.deleteRowById(subClassId)}.map { Unit }
    }

}