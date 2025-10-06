package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationPreprocessRepositoryInterface {
    suspend fun insert(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationPreprocessContainer>
    suspend fun selectExist(id: Long?, idName: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
}

data class ConjugationPreprocessContainer(val id: Long, val idName: String)