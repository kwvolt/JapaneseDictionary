package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface

class ConjugationRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationQueries,
): ConjugationRepositoryInterface {
    override suspend fun insert(
        conjugationPatternId: Long,
        conjugationPreprocessId: Long,
        conjugationSuffixId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.insert(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        conjugationPatternId: Long?,
        conjugationPreprocessId: Long?,
        conjugationSuffixId: Long?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.update(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId, id)
        }.map {  }
    }

    override suspend fun delete(id: Long, returnNotFoundOnNull: Boolean): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.delete(id)
        }.map {  }
    }

    override suspend fun selectId(
        conjugationPatternId: Long,
        conjugationPreprocessId: Long,
        conjugationSuffixId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectId in ConjugationRepository for conjugationPatternId: $conjugationPatternId, conjugationPreprocessId: $conjugationPreprocessId, conjugationSuffixId: $conjugationSuffixId",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            queries.selectId(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationContainer> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectRow in ConjugationRepository for id: $id",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationContainer(id, it.conjugation_pattern_id, it.conjugation_preprocess_id, it.conjugation_suffix_id)
        }
    }
}