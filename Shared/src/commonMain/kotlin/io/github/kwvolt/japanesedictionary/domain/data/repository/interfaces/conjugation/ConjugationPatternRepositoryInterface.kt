package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationPatternRepositoryInterface {
    suspend fun insert(idName: String, displayText: String, descriptionText: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(
        id: Long,
        idName: String? = null,
        displayText: String? = null,
        descriptionTextProvided: Boolean,
        descriptionText: String?,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationPatternContainer>
    suspend fun selectExist(id: Long?, idName: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
    suspend fun insertLinkVariantToOriginal(conjugationPatternId: Long, conjugationVariantId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun removeLinkVariantToOriginal(conjugationPatternId: Long, conjugationVariantId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectIsVariantOf(conjugationVariantId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectCheckLinkExist(conjugationPatternId: Long, conjugationVariantId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationPatternContainer(val id: Long, val idName: String, val displayText: String, val descriptionText: String? = null)