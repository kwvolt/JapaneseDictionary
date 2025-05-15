package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.SubClassQueries

class SubClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val subClassQueries: SubClassQueries
): SubClassRepositoryInterface {
    
    override suspend fun insertSubClassLinkToMainClass(
        mainClassID: Long,
        idName: String,
        displayText: String
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(subClassQueries.insertSubClassLinkToMainClass(idName, displayText, mainClassID).awaitAsOneOrNull())
    }

    override suspend fun insertSubClass(idName: String, displayText: String): DatabaseResult<Long> {
        return dbHandler.wrapResult(subClassQueries.insertSubClass(idName, displayText).awaitAsOneOrNull())
    }

    override suspend fun selectSubClassId(idName: String): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectSubClassId For value idName: $idName"
        ) {
            subClassQueries.selectSubClassId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectSubClassById(subClassId: Long): DatabaseResult<SubClassContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectSubClassById For value subClassId: $subClassId"
        ) {
            subClassQueries.selectSubClassById(subClassId).awaitAsOneOrNull()
        }.flatMap { result ->
            DatabaseResult.Success(SubClassContainer(subClassId, result.id_name, result.display_text)) }
    }

    override suspend fun selectSubClassByIdName(idName: String): DatabaseResult<SubClassContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectSubClassByIdName For value idName: $idName"
        ) {
            subClassQueries.selectSubClassByIdName(idName).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(
            SubClassContainer(result.id, idName, result.display_text))
        }
    }

    override suspend fun selectAllSubClassByMainClassId(mainClassId: Long): DatabaseResult<List<SubClassContainer>> {
        return dbHandler.selectAll(
            "Error at selectAllSubClassByMainClassId For value mainClassId: $mainClassId",
            queryBlock = { subClassQueries.selectAllSubClassByMainClassId(mainClassId).awaitAsList() },
            mapper = {result -> SubClassContainer(result.id, result.id_name, result.display_text) }
        )
    }


    override suspend fun updateSubClassIdName(
        subClassId: Long,
        idName: String
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(subClassQueries.updateSubClassIdName(idName, subClassId))
    }

    override suspend fun updateSubClassDisplayText(
        subClassId: Long,
        displayText: String
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(subClassQueries.updateSubClassDisplayText(displayText, subClassId))
    }

    override suspend fun deleteSubClassByIdName(idName: String): DatabaseResult<Unit> {
        return dbHandler.wrapResult(subClassQueries.deleteSubClassByIdName(idName))
    }

    override suspend fun deleteSubClassById(subClassId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(subClassQueries.deleteSubClassById(subClassId))
    }

}