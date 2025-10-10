package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationSuffixQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface

class ConjugationSuffixRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationSuffixQueries,
): ConjugationSuffixRepositoryInterface {
    override suspend fun insert(
        suffixText: String?,
        isShortForm: Boolean?,
        isPositive: Boolean?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(suffixText, isShortForm?.let {if(it) 1 else 0 }, isPositive?.let {if(it) 1 else 0 }).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        suffixTextProvided: Boolean,
        suffixText: String?,
        isShortFormProvided: Boolean,
        isShortForm: Boolean?,
        isPositiveProvided: Boolean,
        isPositive: Boolean?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.update(
                suffixTextProvided,
                suffixText,
                isShortFormProvided,
                isShortForm?.let{if(it) 1 else 0},
                isPositiveProvided,
                isPositive?.let{if(it) 1 else 0},
                id
            )
        }
    }

    override suspend fun delete(id: Long, returnNotFoundOnNull: Boolean): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) { queries.delete(id) }
    }

    override suspend fun selectId(
        suffixText: String?,
        isShortForm: Boolean?,
        isPositive: Boolean?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectId(suffixText, isShortForm?.let{if(it) 1 else 0}, isPositive?.let{if(it) 1 else 0}).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationSuffixContainer> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationSuffixContainer(
                id,
                it.suffix_text,
                it.is_short_form == 1L,
                it.is_positve== 1L
            )
        }
    }

    override suspend fun selectExist(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Boolean> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectExistById(id).awaitAsOneOrNull()
        }
    }

}