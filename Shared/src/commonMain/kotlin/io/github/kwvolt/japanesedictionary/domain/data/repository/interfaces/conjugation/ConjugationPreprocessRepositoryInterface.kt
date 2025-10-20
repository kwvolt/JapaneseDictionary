package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule

interface ConjugationPreprocessRepositoryInterface {
    suspend fun insert(idName: StemRule, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, idName: StemRule, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(idName: StemRule, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationPreprocessContainer>
    suspend fun selectExist(id: Long?, idName: StemRule?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationPreprocessContainer(val id: Long, val idName: StemRule)