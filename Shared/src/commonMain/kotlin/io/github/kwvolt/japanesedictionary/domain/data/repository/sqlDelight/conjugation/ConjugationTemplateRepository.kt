package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationTemplateLinkConjugationQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationTemplateQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface

class ConjugationTemplateRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationTemplateQueries,
    private val linkQueries: ConjugationTemplateLinkConjugationQueries
): ConjugationTemplateRepositoryInterface {
    override suspend fun insert(
        idName: String,
        displayText: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(idName, displayText).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        id: Long,
        idName: String?,
        displayText: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.update(idName, displayText, id)
        }.map {}
    }

    override suspend fun delete(id: Long, returnNotFoundOnNull: Boolean): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) { queries.delete(id) }.map {}
    }

    override suspend fun selectId(
        idName: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationTemplateContainer> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            queries.selectRow(id).awaitAsOneOrNull()
        }.map {
            ConjugationTemplateContainer(id, it.id_name, it.display_text)
        }
    }

    override suspend fun selectExist(
        id: Long?,
        idName: String?,
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

    override suspend fun insertLinkConjugationToConjugationTemplate(
        conjugationTemplateId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            linkQueries.insert(conjugationId, conjugationTemplateId)
        }.map{}
    }

    override suspend fun removeLinkConjugationToConjugationTemplate(
        conjugationTemplateId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            linkQueries.delete(conjugationId,conjugationTemplateId)
        }.map{}
    }

    override suspend fun selectConjugationIdByConjugationTemplateId(
        conjugationTemplateId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<Long>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            linkQueries.selectConjugationIdByConjugationTemplateId(conjugationTemplateId).awaitAsList()
        }.map { listOf()}
    }
}