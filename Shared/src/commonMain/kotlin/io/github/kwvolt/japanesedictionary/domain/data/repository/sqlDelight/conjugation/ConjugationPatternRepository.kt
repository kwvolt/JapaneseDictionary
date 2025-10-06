package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationPatternQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationPatternVariantQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface

class ConjugationPatternRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationPatternQueries,
    private val variantQueries: ConjugationPatternVariantQueries
): ConjugationPatternRepositoryInterface {
    override suspend fun insert(
        idName: String,
        displayText: String,
        descriptionText: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(idName, displayText, descriptionText).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        idName: String?,
        displayText: String?,
        descriptionTextProvided: Boolean,
        descriptionText: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.update(idName, displayText, descriptionTextProvided, descriptionText, id)
        }.map {}
    }

    override suspend fun delete(id: Long, returnNotFoundOnNull: Boolean): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) { queries.delete(id) }.map {}
    }

    override suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectId in ConjugationPatternRepository for idName: $idName",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            queries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean): DatabaseResult<ConjugationPatternContainer> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectRow in ConjugationPatternRepository for id: $id",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationPatternContainer(id, it.id_name, it.display_text)
        }
    }

    override suspend fun selectExist(
        id: Long?,
        idName: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Boolean> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectExist in ConjugationPatternRepository for id: $id",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            if(id != null){
                queries.selectExistById(id).awaitAsOneOrNull()
            }
            else if(idName != null){
                queries.selectExistByIdName(idName).awaitAsOneOrNull()
            }
            else false
        }
    }

    override suspend fun insertLinkVariantToOriginal(
        conjugationPatternId: Long,
        conjugationVariantId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            variantQueries.insert(conjugationPatternId, conjugationVariantId)
        }.map{}
    }

    override suspend fun removeLinkVariantToOriginal(
        conjugationPatternId: Long,
        conjugationVariantId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            variantQueries.delete(conjugationPatternId, conjugationVariantId)
        }.map{}
    }

    override suspend fun selectIsVariantOf(
        conjugationVariantId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            errorMessage = "Error at selectIsVariantOf in ConjugationPatternRepository for conjugationVariantId: $conjugationVariantId",
            returnNotFoundOnNull = returnNotFoundOnNull
        ){
            variantQueries.selectIsVariantOfPatternId(conjugationVariantId).awaitAsOneOrNull()
        }
    }
}