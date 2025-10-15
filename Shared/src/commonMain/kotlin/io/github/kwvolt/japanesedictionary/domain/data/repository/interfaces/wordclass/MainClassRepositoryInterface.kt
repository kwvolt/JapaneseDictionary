package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface MainClassRepositoryInterface {

    suspend fun insert(idName: String, displayText: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectId(idName: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRowById(mainClassId: Long, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<MainClassContainer>

    suspend fun selectRowByIdName(idName: String, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<MainClassContainer>

    suspend fun selectAll(returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<List<MainClassContainer>>

    suspend fun update(mainClassId: Long, idName: String? = null, displayText: String? = null, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Unit>

    suspend fun delete(mainClassId: Long, returnNotFoundOnNull: Boolean = false, itemId: String? = null): DatabaseResult<Unit>
}

data class MainClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
