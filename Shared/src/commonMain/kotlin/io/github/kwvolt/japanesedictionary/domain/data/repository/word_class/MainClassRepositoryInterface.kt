package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface MainClassRepositoryInterface {

    suspend fun insertMainClass(idName: String, displayText: String): DatabaseResult<Long>

    suspend fun selectMainClassId(idName: String): DatabaseResult<Long>

    suspend fun selectMainClassById(mainClassId: Long): DatabaseResult<MainClassContainer>

    suspend fun selectMainClassByIdName(idName: String): DatabaseResult<MainClassContainer>

    suspend fun selectAllMainClass(): DatabaseResult<List<MainClassContainer>>

    suspend fun updateMainClassIdName(mainClassId: Long, idName: String): DatabaseResult<Unit>

    suspend fun updateMainClassDisplayText(mainClassId: Long, displayText: String): DatabaseResult<Unit>

    suspend fun deleteMainClassByIdName(idName: String): DatabaseResult<Unit>

    suspend fun  deleteMainClassById(mainClassId: Long): DatabaseResult<Unit>
}

data class MainClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
