package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SubClassRepositoryInterface {

    suspend fun insertLinkToMainClass(mainClassID:Long, idName: String, displayText: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Long>
    
    suspend fun insert(idName: String, displayText: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRowById(subClassId: Long, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<SubClassContainer>

    suspend fun selectRowByIdName(idName: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<SubClassContainer>

    suspend fun selectAllByMainClassId(mainClassId:Long, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<List<SubClassContainer>>

    suspend fun update(subClassId: Long, idName: String? =null, displayText: String? =null, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Unit>

    suspend fun  delete(subClassId: Long, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Unit>
}

data class SubClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
