package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationRepositoryInterface {
    suspend fun insert(conjugationPatternId: Long, conjugationPreprocessId: Long, conjugationSuffixId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, conjugationPatternId: Long? = null, conjugationPreprocessId: Long? = null, conjugationSuffixId: Long? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(conjugationPatternId: Long, conjugationPreprocessId: Long, conjugationSuffixId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationContainer>
}

data class ConjugationContainer(val id: Long, val conjugationPatternId: Long, val conjugationPreprocessId: Long, val conjugationSuffixId: Long)