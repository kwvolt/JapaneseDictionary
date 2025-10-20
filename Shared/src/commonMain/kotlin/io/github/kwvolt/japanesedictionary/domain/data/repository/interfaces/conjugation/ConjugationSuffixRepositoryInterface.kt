package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationSuffixRepositoryInterface {
    suspend fun insert(
        suffixText: String? = null,
        isShortForm: Boolean? = null,
        isPositive: Boolean? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>
    suspend fun update(
        id: Long,
        suffixTextProvided: Boolean = false,
        suffixText: String? = null,
        isShortFormProvided: Boolean = false,
        isShortForm: Boolean? = null,
        isPositiveProvided: Boolean = false,
        isPositive: Boolean? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(
        suffixText: String?,
        isShortForm: Boolean?,
        isPositive: Boolean?,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationSuffixContainer>
    suspend fun selectExist(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationSuffixContainer(val id: Long, val suffixText: String?, val isShortForm: Boolean?, val isPositive: Boolean?)