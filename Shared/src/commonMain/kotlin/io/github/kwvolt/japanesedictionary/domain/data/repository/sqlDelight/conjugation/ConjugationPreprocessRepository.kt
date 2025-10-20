package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationPreprocessQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule

class ConjugationPreprocessRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationPreprocessQueries,
): ConjugationPreprocessRepositoryInterface {
    override suspend fun insert(
        idName: StemRule,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        idName: StemRule,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.update(idName, id)
        }
    }

    override suspend fun delete(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.delete(id)
        }
    }

    override suspend fun selectId(
        idName: StemRule,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationPreprocessContainer> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationPreprocessContainer(id, it)
        }
    }

    override suspend fun selectExist(
        id: Long?,
        idName: StemRule?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Boolean> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            if(id != null){
                queries.selectExistById(id).awaitAsOneOrNull()
            }
            else if(idName != null){
                queries.selectExistByIdName(idName).awaitAsOneOrNull()
            }
            else false
        }
    }
}