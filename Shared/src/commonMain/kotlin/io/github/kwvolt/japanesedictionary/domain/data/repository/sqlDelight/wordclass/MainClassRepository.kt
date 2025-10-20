package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.wordclass.MainClassQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassRepositoryInterface

class MainClassRepository(
    private val dbHandler: DatabaseHandlerBase ,
    private val mainClassQueries: MainClassQueries
): MainClassRepositoryInterface {

    override suspend fun insert(
        idName: String,
        displayText: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull){mainClassQueries.insert(idName, displayText).awaitAsOneOrNull()}
    }

    override suspend fun selectId(
        idName: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
                mainClassQueries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRowById(
        mainClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<MainClassContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            mainClassQueries.selectRowById(mainClassId).awaitAsOneOrNull()
        }.map { result -> MainClassContainer(mainClassId, result.id_name, result.display_text)}
    }

    override suspend fun selectRowByIdName(
        idName: String,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<MainClassContainer> {
        return dbHandler.wrapQuery(itemId, returnNotFoundOnNull) {
            mainClassQueries.selectRowByIdName(idName).awaitAsOneOrNull()
        }.map { result -> MainClassContainer(result.id, idName, result.display_text) }
    }

    override suspend fun selectAll(
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<MainClassContainer>> {
        return dbHandler.selectAllToList(itemId, returnNotFoundOnNull,
            queryBlock = { mainClassQueries.selectAll().awaitAsList() },
            mapper = { item -> MainClassContainer(item.id, item.id_name, item.display_text) }
        )
    }

    override suspend fun update(
        mainClassId: Long,
        idName: String?,
        displayText: String?,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){mainClassQueries.update(idName,displayText, mainClassId)}
    }

    override suspend fun delete(
        mainClassId: Long,
        itemId: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(itemId, returnNotFoundOnNull){mainClassQueries.delete(mainClassId)}
    }
}