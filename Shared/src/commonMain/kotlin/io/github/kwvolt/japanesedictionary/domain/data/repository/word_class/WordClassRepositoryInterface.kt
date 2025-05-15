package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface WordClassRepositoryInterface {
    suspend fun insertByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long): DatabaseResult<Long>

    suspend fun insertByMainClassIdNameAndSubClassIdName(mainClassIdName: String, subClassIdName: String): DatabaseResult<Long>

    suspend fun selectWordClassIdByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long):DatabaseResult<Long>

    suspend fun selectWordClassIdByMainClassIdNameAndSubClassIdName(mainClassIdName: String, subClassIdName: String): DatabaseResult<Long>

    suspend fun selectWordClassMainClassIdAndSubClassIdByWordClassId(wordClassId: Long): DatabaseResult<WordClassIdContainer>

    suspend fun updateWordClassMainClassIdByWordClassId(wordClassId: Long, mainClassId: Long): DatabaseResult<Unit>


    suspend fun updateWordClassSubClassIdByWordClassId(wordClassId: Long, subClassId: Long):DatabaseResult<Unit>


    suspend fun updateWordClassMainClassIdAndSubClassIdByWordClassId(wordClassId: Long, mainClassId: Long, subClassId: Long):DatabaseResult<Unit>

    suspend fun deleteWordClassByWordClassId(wordClassId: Long):DatabaseResult<Unit>

    suspend fun deleteWordClassByMainClassIdAndSubClassId(mainClassId: Long, subClassId: Long):DatabaseResult<Unit>

}

data class WordClassIdContainer(val wordClassId: Long, val mainClassId: Long, val subClassId: Long){
}