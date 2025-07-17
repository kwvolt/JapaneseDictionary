package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface WordClassRepositoryInterface {
    suspend fun insertByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long, itemId: String? = null): DatabaseResult<Long>

    suspend fun insertByMainClassIdNameAndSubClassIdName(mainClassIdName: String, subClassIdName: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectIdByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long, itemId: String? = null):DatabaseResult<Long>

    suspend fun selectIdByMainClassIdNameAndSubClassIdName(mainClassIdName: String, subClassIdName: String, itemId: String? = null): DatabaseResult<Long>

    suspend fun selectRow(wordClassId: Long, itemId: String? = null): DatabaseResult<WordClassIdContainer>

    suspend fun updateMainClassId(wordClassId: Long, mainClassId: Long, itemId: String? = null): DatabaseResult<Unit>

    suspend fun updateSubClassId(wordClassId: Long, subClassId: Long, itemId: String? = null):DatabaseResult<Unit>

    suspend fun updateMainClassIdAndSubClassId(wordClassId: Long, mainClassId: Long, subClassId: Long, itemId: String? = null):DatabaseResult<Unit>

    suspend fun deleteRowByWordClassId(wordClassId: Long, itemId: String? = null):DatabaseResult<Unit>

    suspend fun deleteRowByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long, itemId: String? = null):DatabaseResult<Unit>

}

data class WordClassIdContainer(val wordClassId: Long, val mainClassId: Long, val subClassId: Long)

abstract class WordChildClassContainer(open val id: Long, open val idName: String, open val displayText: String)