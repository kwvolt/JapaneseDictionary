package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface MainClassRepositoryInterface {

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
        mainClassId: Long,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<MainClassContainer>

    suspend fun selectRowByIdName(
        idName: String,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<MainClassContainer>

    suspend fun selectAll(itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<List<MainClassContainer>>

    suspend fun update(
        mainClassId: Long,
        idName: String? = null,
        displayText: String? = null,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>

    suspend fun delete(
        mainClassId: Long,
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false
    ): DatabaseResult<Unit>
}

data class MainClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
