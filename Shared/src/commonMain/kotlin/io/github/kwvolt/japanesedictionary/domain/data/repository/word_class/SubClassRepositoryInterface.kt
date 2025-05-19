package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface SubClassRepositoryInterface {

    suspend fun insertSubClassLinkToMainClass(mainClassID:Long, idName: String, displayText: String): DatabaseResult<Long>
    
    suspend fun insertSubClass(idName: String, displayText: String): DatabaseResult<Long>

    suspend fun selectSubClassId(idName: String): DatabaseResult<Long>

    suspend fun selectSubClassById(subClassId: Long): DatabaseResult<SubClassContainer>

    suspend fun selectSubClassByIdName(idName: String): DatabaseResult<SubClassContainer>

    suspend fun selectAllSubClassByMainClassId(mainClassId:Long): DatabaseResult<List<SubClassContainer>>

    suspend fun updateSubClassIdName(subClassId: Long, idName: String): DatabaseResult<Unit>

    suspend fun updateSubClassDisplayText(subClassId: Long, displayText: String): DatabaseResult<Unit>

    suspend fun deleteSubClassByIdName(idName: String): DatabaseResult<Unit>

    suspend fun  deleteSubClassById(subClassId: Long): DatabaseResult<Unit>
}

data class SubClassContainer(
    override val id: Long,
    override val idName: String,
    override val displayText: String
): WordChildClassContainer(id, idName, displayText)
