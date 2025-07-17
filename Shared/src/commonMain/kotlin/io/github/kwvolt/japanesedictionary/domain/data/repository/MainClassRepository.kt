package io.github.kwvolt.japanesedictionary.domain.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.MainClassQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface

class MainClassRepository(
    private val dbHandler: DatabaseHandlerBase ,
    private val mainClassQueries: MainClassQueries
): MainClassRepositoryInterface {

    override suspend fun insert(
        idName: String,
        displayText: String,
        itemId: String?
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId){mainClassQueries.insert(idName, displayText).awaitAsOneOrNull()}
    }

    override suspend fun selectId(idName: String, itemId: String?): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectId in MainClassRepository For value idName: $idName"
        ) {
                mainClassQueries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRowById(
        mainClassId: Long,
        itemId: String?
    ): DatabaseResult<MainClassContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectRowById in MainClassRepository For value mainClassId: $mainClassId"
        ) {
            mainClassQueries.selectRowById(mainClassId).awaitAsOneOrNull()
        }.map { result -> MainClassContainer(mainClassId, result.id_name, result.display_text)}
    }

    override suspend fun selectRowByIdName(
        idName: String,
        itemId: String?
    ): DatabaseResult<MainClassContainer> {
        return dbHandler.withContextDispatcherWithException(itemId,
            "Error at selectRowByIdName in MainClassRepository For value idName: $idName"
        ) {
            mainClassQueries.selectRowByIdName(idName).awaitAsOneOrNull()
        }.map { result -> MainClassContainer(result.id, idName, result.display_text)

        }
    }

    override suspend fun selectAll(itemId: String?): DatabaseResult<List<MainClassContainer>> {
        return dbHandler.selectAll(itemId,
            "Error in selectAll in MainClassRepository",
            queryBlock = { mainClassQueries.selectAll().awaitAsList() },
            mapper = { item -> MainClassContainer(item.id, item.id_name, item.display_text) }
        )
    }

    override suspend fun updateIdName(
        mainClassId: Long,
        idName: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){mainClassQueries.updateIdName(idName, mainClassId)}
    }

    override suspend fun updateDisplayText(
        mainClassId: Long,
        displayText: String,
        itemId: String?
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){mainClassQueries.updateDisplayText(displayText, mainClassId)}
    }

    override suspend fun deleteRowByIdName(idName: String, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){mainClassQueries.deleteRowByIdName(idName)}
    }

    override suspend fun deleteRowById(mainClassId: Long, itemId: String?): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(itemId){mainClassQueries.deleteRowById(mainClassId)}
    }
}