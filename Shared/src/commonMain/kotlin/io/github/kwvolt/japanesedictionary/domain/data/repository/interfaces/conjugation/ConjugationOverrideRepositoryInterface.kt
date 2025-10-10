package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationOverrideProperty

interface ConjugationOverrideRepositoryInterface {
    suspend fun insert(
        idName: String,
        dictionaryEntryId: Long,
        conjugationId: Long,
        overrideNote: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>
    suspend fun update(
        conjugationOverrideId: Long,
        idName: String? = null,
        dictionaryEntryId: Long? = null,
        conjugationId: Long? = null,
        overrideNoteProvided: Boolean = false,
        overrideNote: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
    suspend fun delete(conjugationOverrideId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(conjugationOverrideId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationOverrideContainer>
    suspend fun selectAllByDictionaryEntryIdAndConjugationId(dictionaryEntryId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<ConjugationOverrideContainer>>
    suspend fun selectExist(id: Long?, idName: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>

    suspend fun insertPropertyValue(conjugationOverrideId: Long, overridePropertyId: Long, propertyValue: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun updatePropertyValue(
        conjugationOverrideId: Long,
        overridePropertyId: Long,
        propertyValueProvided: Boolean = false,
        propertyValue: String? = null,
        returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deletePropertyValue(conjugationOverrideId: Long, overridePropertyId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectPropertyValue(conjugationOverrideId: Long, overridePropertyId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<String?>
    suspend fun selectPropertyValues(conjugationOverrideId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Map<ConjugationOverrideProperty, String?>>


    // property
    suspend fun insertProperty(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun updateProperty(id: Long, idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deleteProperty(id: Long ,returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectPropertyId(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectPropertyRow(id: Long ,returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationOverridePropertyContainer>
    suspend fun selectPropertyExist(id: Long?, idName: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationOverrideContainer(
    val id: Long,
    val idName: String,
    val dictionaryEntryId: Long,
    val conjugationId: Long,
    val overrideNote: String? = null,
    val overrideProperty: Map<ConjugationOverrideProperty, String?>
)

data class ConjugationOverridePropertyContainer(val id: Long, val idName: String)