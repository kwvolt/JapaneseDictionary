package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SubClassRepositoryInterface {

    suspend fun insertLinkToMainClass(
        mainClassID: Long,
        idName: String,
        displayText: String,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>
    
    suspend fun insert(
        idName: String,
        displayText: String,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>

    suspend fun selectId(
        idName: String,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Long>

    suspend fun selectRowById(
        subClassId: Long,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<SubClassContainer>

    suspend fun selectRowByIdName(
        idName: String,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<SubClassContainer>

    suspend fun selectAllByMainClassId(
        mainClassId: Long,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<List<SubClassContainer>>

    suspend fun update(
        subClassId: Long,
        idName: String? = null,
        displayText: String? = null,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>

    suspend fun  delete(
        subClassId: Long,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
}

data class SubClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
