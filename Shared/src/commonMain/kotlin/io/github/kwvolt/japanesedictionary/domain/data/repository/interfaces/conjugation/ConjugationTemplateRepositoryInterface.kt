package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface ConjugationTemplateRepositoryInterface {
    suspend fun insert(idName: String, displayText: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun update(id: Long, idName: String? = null, displayText: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun delete(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<ConjugationTemplateContainer>
    suspend fun selectAll(returnNotFoundOnNull: Boolean = false): DatabaseResult<List<ConjugationTemplateContainer>>
    suspend fun selectExist(id: Long?, idName: String?, returnNotFoundOnNull: Boolean = false): DatabaseResult<Boolean>
    suspend fun insertLinkConjugationToConjugationTemplate(conjugationTemplateId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun removeLinkConjugationToConjugationTemplate(conjugationTemplateId: Long, conjugationId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectConjugationIdByConjugationTemplateId(conjugationTemplateId: Long, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<Long>>
}

data class ConjugationTemplateContainer(val id: Long, val idName: String, val displayText: String)