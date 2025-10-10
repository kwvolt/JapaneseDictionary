package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SubClassRepositoryInterface {

    suspend fun insertLinkToMainClass(mainClassID:Long, idName: String, displayText: String, itemId: String? = null): DatabaseResult<Long>
    
    suspend fun insert(idName: String, displayText: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectId(idName: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRowById(subClassId: Long, itemId: String? = null): DatabaseResult<SubClassContainer>

    suspend fun selectRowByIdName(idName: String, itemId: String? = null): DatabaseResult<SubClassContainer>

    suspend fun selectAllByMainClassId(mainClassId:Long, itemId: String? = null): DatabaseResult<List<SubClassContainer>>

    suspend fun updateIdName(subClassId: Long, idName: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun updateDisplayText(subClassId: Long, displayText: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun deleteRowByIdName(idName: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun  deleteRowById(subClassId: Long, itemId: String? = null): DatabaseResult<Unit>
}

data class SubClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
