package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.VerbSuffixSwapLinkConjugationQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.VerbSuffixSwapQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface

class VerbSuffixSwapRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: VerbSuffixSwapQueries,
    private val linkQueries: VerbSuffixSwapLinkConjugationQueries
): ConjugationVerbSuffixSwapRepositoryInterface {
    override suspend fun insert(
        original: String,
        replacement: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(original, replacement).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        original: String?,
        replacement: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.update(original, replacement, id)
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
        original: String,
        replacement: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectId(original, replacement).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationVerbSuffixSwapContainer> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationVerbSuffixSwapContainer(id, it.original, it.replacement)
        }
    }

    override suspend fun insertLinkVerbSuffixSwapToConjugation(
        verbSuffixSwapId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            linkQueries.insert(conjugationId, verbSuffixSwapId)
        }.map{}
    }

    override suspend fun deleteLinkVerbSuffixSwapToConjugation(
        verbSuffixSwapId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            linkQueries.delete(verbSuffixSwapId, conjugationId)
        }
    }

    override suspend fun selectVerbSwapSuffixIds(
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.selectAll(returnNotFoundOnNull = returnNotFoundOnNull,
            queryBlock = {linkQueries.selectVerbSwapSuffixIds(conjugationId).awaitAsList()},
            mapper = { it }
        )
    }

    override suspend fun selectReplacement(
        conjugationId: Long,
        original: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<String> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            linkQueries.selectReplacement(conjugationId, original).awaitAsOneOrNull()
        }
    }
}