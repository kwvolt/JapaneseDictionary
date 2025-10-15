package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.RequiresTransaction
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverrideDetailsQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverridePropertyQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverrideQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.SelectAllByConjugationTemplateIdAndConjugationId
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.SelectRowById
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.SelectRowByKeys
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverridePropertyContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationOverrideProperty

class ConjugationOverrideRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationOverrideQueries,
    private val detailsQueries: ConjugationOverrideDetailsQueries,
    private val propertyQueries: ConjugationOverridePropertyQueries
): ConjugationOverrideRepositoryInterface {

    override suspend fun insert(
        isKanji: Boolean?,
        conjugationTemplateId: Long,
        conjugationId: Long,
        overrideNote: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(isKanji?.let{ if(isKanji) 1 else 0}, conjugationTemplateId, conjugationId, overrideNote).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        conjugationOverrideId: Long,
        isKanjiProvided: Boolean,
        isKanji: Boolean?,
        conjugationTemplateId: Long?,
        conjugationId: Long?,
        overrideNoteProvided: Boolean,
        overrideNote: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.update(
                isKanjiProvided,
                isKanji?.let{ if(isKanji) 1 else 0},
                conjugationTemplateId,
                conjugationId,
                overrideNoteProvided,
                overrideNote,
                conjugationOverrideId
            )
        }
    }

    /**
     * Delete a conjugation override and its properties.
     *
     * Warning: Must be called within a database transaction. Fails if not.
     *
     **/
    @RequiresTransaction
    override suspend fun delete(
        conjugationOverrideId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.requireTransaction {
            dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
                queries.delete(conjugationOverrideId)
                detailsQueries.deleteAll(conjugationOverrideId)
            }
        }
    }

    override suspend fun selectId(
        isKanji: Boolean?,
        conjugationTemplateId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectId(isKanji?.let{ if(isKanji) 1 else 0}, conjugationTemplateId, conjugationId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRowById(
        conjugationOverrideId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationOverrideContainer> {
        val selectRowResult: DatabaseResult<SelectRowById> = dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectRowById(conjugationOverrideId).awaitAsOneOrNull()
        }
        val propertyValues: Map<ConjugationOverrideProperty, String?>
            = selectPropertyValues(conjugationOverrideId).getOrReturn { return it }

        return selectRowResult.map { row ->
            ConjugationOverrideContainer(
                conjugationOverrideId,
                row.is_kanji?.let {it == 1L},
                row.conjugation_template_id,
                row.conjugation_id,
                row.override_note,
                propertyValues
            )
        }
    }

    override suspend fun selectRowByKeys(
        isKanji: Boolean?,
        conjugationTemplateId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationOverrideContainer> {
        val selectRowResult: DatabaseResult<SelectRowByKeys> = dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectRowByKeys(isKanji?.let{ if(it) 1 else 0 }, conjugationTemplateId, conjugationId).awaitAsOneOrNull()
        }
        return selectRowResult.map { row ->
            val propertyValues: Map<ConjugationOverrideProperty, String?> = selectPropertyValues(row.id).getOrReturn { return it }
            ConjugationOverrideContainer(
                row.id,
                isKanji,
                conjugationTemplateId,
                conjugationId,
                row.override_note,
                propertyValues
            )
        }
    }

    override suspend fun selectAllByConjugationTemplateIdAndConjugationId(
        conjugationTemplateId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<ConjugationOverrideContainer>> {
        val selectRowResult: DatabaseResult<List<SelectAllByConjugationTemplateIdAndConjugationId>> = dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectAllByConjugationTemplateIdAndConjugationId(conjugationTemplateId, conjugationId).awaitAsList()
        }
        return selectRowResult.map { rows ->
            rows.map { row ->
                val propertyValues: Map<ConjugationOverrideProperty, String?> = selectPropertyValues(row.id).getOrReturn { return it }
                ConjugationOverrideContainer(
                    row.id,
                    row.is_kanji?.let {it == 1L},
                    conjugationTemplateId,
                    conjugationId,
                    row.override_note,
                    propertyValues
                )
            }
        }
    }

    override suspend fun insertPropertyValue(
        conjugationOverrideId: Long,
        overridePropertyId: Long,
        propertyValue: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            detailsQueries.insert(conjugationOverrideId, overridePropertyId, propertyValue)
        }
    }

    override suspend fun updatePropertyValue(
        conjugationOverrideId: Long,
        overridePropertyId: Long,
        propertyValueProvided: Boolean,
        propertyValue: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            detailsQueries.update(
                propertyValueProvided,
                propertyValue,
                conjugationOverrideId,
                overridePropertyId)
        }
    }

    override suspend fun deletePropertyValue(
        conjugationOverrideId: Long,
        overridePropertyId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            detailsQueries.delete(
                conjugationOverrideId,
                overridePropertyId)
        }
    }

    override suspend fun selectPropertyValue(
        conjugationOverrideId: Long,
        overridePropertyId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<String?> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            detailsQueries.selectRow(
                conjugationOverrideId,
                overridePropertyId
            ).awaitAsOneOrNull()
        }.map { it.property_value }
    }

    override suspend fun selectPropertyValues(
        conjugationOverrideId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Map<ConjugationOverrideProperty, String?>> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            detailsQueries.selectAllByOverrideId(
                conjugationOverrideId
            ).awaitAsList()
        }.flatMap { rows ->
            try {
                DatabaseResult.Success(
                    rows.associate { row ->
                        val propertyName = try {
                            ConjugationOverrideProperty.valueOf(row.property_name)
                        } catch (e: IllegalArgumentException) {
                            throw IllegalArgumentException("Invalid property name: '${row.property_name}'", e)
                        }
                        propertyName to row.property_value
                    }
                )
            } catch (e: Exception) {
                DatabaseResult.UnknownError(e)
            }
        }
    }

    override suspend fun insertProperty(
        idName: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            propertyQueries.insert(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun updateProperty(
        id: Long,
        idName: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            propertyQueries.update(idName, id)
        }
    }

    override suspend fun deleteProperty(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            propertyQueries.delete(id)
        }.toUnit()
    }

    override suspend fun selectPropertyId(
        idName: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            propertyQueries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectPropertyRow(
        id: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationOverridePropertyContainer> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            propertyQueries.selectRow(id).awaitAsOneOrNull()
        }.map { ConjugationOverridePropertyContainer(id, it) }
    }

    override suspend fun selectPropertyExist(
        id: Long?,
        idName: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Boolean> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull){
            if(id != null){
                propertyQueries.selectExistById(id).awaitAsOneOrNull()
            }
            else if(idName != null){
                propertyQueries.selectExistByIdName(idName).awaitAsOneOrNull()
            }
            else false
        }
    }
}