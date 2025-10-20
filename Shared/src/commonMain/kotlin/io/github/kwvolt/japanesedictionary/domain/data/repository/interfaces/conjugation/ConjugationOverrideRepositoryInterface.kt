package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationOverrideProperty

interface ConjugationOverrideRepositoryInterface {
    suspend fun insert(
        isKanji: Boolean? = null,
        conjugationTemplateId: Long,
        conjugationId: Long,
        overrideNote: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>
    suspend fun update(
        conjugationOverrideId: Long,
        isKanjiProvided: Boolean = false,
        isKanji: Boolean? = null,
        conjugationTemplateId: Long? = null,
        conjugationId: Long? = null,
        overrideNoteProvided: Boolean = false,
        overrideNote: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
    suspend fun delete(conjugationOverrideId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(isKanji: Boolean? = null, conjugationTemplateId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRowById(conjugationOverrideId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationOverrideContainer>
    suspend fun selectRowByKeys(isKanji: Boolean? = null, conjugationTemplateId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationOverrideContainer>
    suspend fun selectAllByConjugationTemplateIdAndConjugationId(conjugationTemplateId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<ConjugationOverrideContainer>>

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
    suspend fun insertProperty(idName: ConjugationOverrideProperty, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun updateProperty(id: Long, idName: ConjugationOverrideProperty, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deleteProperty(id: Long ,returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectPropertyId(idName: ConjugationOverrideProperty, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectPropertyRow(id: Long ,returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationOverridePropertyContainer>
    suspend fun selectPropertyExist(id: Long?, idName: ConjugationOverrideProperty?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationOverrideContainer(
    val id: Long,
    val isKanji: Boolean? = null,
    val conjugationTemplateId: Long,
    val conjugationId: Long,
    val overrideNote: String? = null,
    val overrideProperty: Map<ConjugationOverrideProperty, String?>
)

data class ConjugationOverridePropertyContainer(val id: Long, val idName: ConjugationOverrideProperty)