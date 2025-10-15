package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationVerbSuffixSwapRepositoryInterface {
    suspend fun insert(original: String, replacement: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, original: String?, replacement: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(original: String, replacement: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationVerbSuffixSwapContainer>
    suspend fun insertLinkVerbSuffixSwapToConjugation(verbSuffixSwapId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deleteLinkVerbSuffixSwapToConjugation(verbSuffixSwapId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectVerbSwapSuffixIds(conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
    suspend fun selectReplacement(conjugationId: Long, original: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<String>
}

data class ConjugationVerbSuffixSwapContainer(val id: Long, val original: String, val replacement: String)