package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.MainClassQueries

class MainClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val mainClassQueries: MainClassQueries
): MainClassRepositoryInterface {

    override suspend fun insertMainClass(
        idName: String,
        displayText: String
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(mainClassQueries.insertMainClass(idName, displayText).awaitAsOneOrNull())
    }

    override suspend fun selectMainClassId(idName: String): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectMainClassId For value idName: $idName"
        ) {
                mainClassQueries.selectMainClassId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectMainClassById(mainClassId: Long): DatabaseResult<MainClassContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectMainClassById For value mainClassId: $mainClassId"
        ) {
            mainClassQueries.selectMainClassById(mainClassId).awaitAsOneOrNull()
        }.flatMap { result ->
            DatabaseResult.Success(MainClassContainer(mainClassId, result.id_name, result.display_text)) }
    }

    override suspend fun selectMainClassByIdName(idName: String): DatabaseResult<MainClassContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectMainClassByIdName For value idName: $idName"
        ) {
            mainClassQueries.selectMainClassByIdName(idName).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(
            MainClassContainer(result.id, idName, result.display_text))
        }
    }

    override suspend fun selectAllMainClass(): DatabaseResult<List<MainClassContainer>> {
        return dbHandler.selectAll(
            "Error in selectAllMainClass",
            queryBlock = { mainClassQueries.selectAllMainClass().awaitAsList() },
            mapper = { item -> MainClassContainer(item.id, item.id_name, item.display_text) }
        )
    }

    override suspend fun updateMainClassIdName(mainClassId: Long, idName: String): DatabaseResult<Unit> {
        return dbHandler.wrapResult(mainClassQueries.updateMainClassIdName(idName, mainClassId))
    }

    override suspend fun updateMainClassDisplayText(mainClassId: Long, displayText: String): DatabaseResult<Unit> {
        return dbHandler.wrapResult(mainClassQueries.updateMainClassDisplayText(displayText, mainClassId))
    }

    override suspend fun deleteMainClassByIdName(idName: String): DatabaseResult<Unit> {
        return dbHandler.wrapResult(mainClassQueries.deleteMainClassByIdName(idName))
    }

    override suspend fun deleteMainClassById(mainClassId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(mainClassQueries.deleteMainClassById(mainClassId))
    }
}