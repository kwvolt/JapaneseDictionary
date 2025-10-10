package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface MainClassRepositoryInterface {

    suspend fun insert(idName: String, displayText: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectId(idName: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRowById(mainClassId: Long, itemId: String? = null): DatabaseResult<MainClassContainer>

    suspend fun selectRowByIdName(idName: String, itemId: String? = null): DatabaseResult<MainClassContainer>

    suspend fun selectAll(itemId: String? = null): DatabaseResult<List<MainClassContainer>>

    suspend fun updateIdName(mainClassId: Long, idName: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun updateDisplayText(mainClassId: Long, displayText: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun deleteRowByIdName(idName: String, itemId: String? = null): DatabaseResult<Unit>

    suspend fun  deleteRowById(mainClassId: Long, itemId: String? = null): DatabaseResult<Unit>
}

data class MainClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
