package io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.RequiresTransaction
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverrideDetailsQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverridePropertyQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationOverrideQueries
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.SelectAllByDictionaryEntryIdAndConjugationId
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.conjugationOverride.SelectRow
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverridePropertyContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationOverrideProperty

class ConjugationOverrideRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val queries: ConjugationOverrideQueries,
    private val detailsQueries: ConjugationOverrideDetailsQueries,
    private val propertyQueries: ConjugationOverridePropertyQueries
): ConjugationOverrideRepositoryInterface {

    override suspend fun insert(
        idName: String,
        dictionaryEntryId: Long,
        conjugationId: Long,
        overrideNote: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull= returnNotFoundOnNull) {
            queries.insert(idName, dictionaryEntryId, conjugationId, overrideNote).awaitAsOneOrNull()
        }
    }

    override suspend fun update(
        conjugationOverrideId: Long,
        idName: String?,
        dictionaryEntryId: Long?,
        conjugationId: Long?,
        overrideNoteProvided: Boolean,
        overrideNote: String?,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Unit> {
        return dbHandler.wrapRowCountQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.update(
                idName,
                dictionaryEntryId,
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
        idName: String,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<Long> {
        return dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectId(idName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectRow(
        conjugationOverrideId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<ConjugationOverrideContainer> {
        val selectRowResult: DatabaseResult<SelectRow> = dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectRow(conjugationOverrideId).awaitAsOneOrNull()
        }
        val propertyValues: Map<ConjugationOverrideProperty, String?>
            = selectPropertyValues(conjugationOverrideId).getOrReturn { return it }

        return selectRowResult.map { row ->
            ConjugationOverrideContainer(
                conjugationOverrideId,
                row.id_name,
                row.dictionary_entry_id,
                row.conjugation_id,
                row.override_note,
                propertyValues
            )
        }
    }

    override suspend fun selectAllByDictionaryEntryIdAndConjugationId(
        dictionaryEntryId: Long,
        conjugationId: Long,
        returnNotFoundOnNull: Boolean
    ): DatabaseResult<List<ConjugationOverrideContainer>> {
        val selectRowResult: DatabaseResult<List<SelectAllByDictionaryEntryIdAndConjugationId>> = dbHandler.wrapQuery(returnNotFoundOnNull = returnNotFoundOnNull) {
            queries.selectAllByDictionaryEntryIdAndConjugationId(dictionaryEntryId, conjugationId).awaitAsList()
        }

        return selectRowResult.map { rows ->
            rows.map { row ->
                val propertyValues: Map<ConjugationOverrideProperty, String?> = selectPropertyValues(row.id).getOrReturn { return it }
                ConjugationOverrideContainer(
                    row.id,
                    row.id_name,
                    dictionaryEntryId,
                    conjugationId,
                    row.override_note,
                    propertyValues
                )
            }
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