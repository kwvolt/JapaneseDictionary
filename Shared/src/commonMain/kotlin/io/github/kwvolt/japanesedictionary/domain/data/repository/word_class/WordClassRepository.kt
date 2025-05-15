package io.github.kwvolt.japanesedictionary.domain.data.repository.word_class

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.WordClassQueries

class WordClassRepository(
    private val dbHandler: DatabaseHandlerBase,
    private val wordClassQueries: WordClassQueries
) : WordClassRepositoryInterface {


    override suspend fun insertByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(wordClassQueries.insertWordClassByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull())
    }

    override suspend fun insertByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String
    ): DatabaseResult<Long> {
        return dbHandler.wrapResult(wordClassQueries.insertWordClassByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull())
    }

    override suspend fun selectWordClassIdByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectWordClassIdByMainClassIdNameAndSubClassIdName For value mainClassId: $mainClassId and subClassId: $subClassId"
        ){wordClassQueries.selectWordClassIdByMainClassIdAndSubClassId(mainClassId, subClassId).awaitAsOneOrNull()
        }
    }

    override suspend fun selectWordClassIdByMainClassIdNameAndSubClassIdName(
        mainClassIdName: String,
        subClassIdName: String
    ): DatabaseResult<Long> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectWordClassIdByMainClassIdNameAndSubClassIdName For value mainClassIdName: $mainClassIdName and subClassIdName: $subClassIdName"
        ){
            wordClassQueries.selectWordClassIdByMainClassIdNameAndSubClassIdName(mainClassIdName, subClassIdName).awaitAsOneOrNull()
        }
    }

    override suspend fun selectWordClassMainClassIdAndSubClassIdByWordClassId(wordClassId: Long): DatabaseResult<WordClassIdContainer> {
        return dbHandler.withContextDispatcherWithException(
            "Error at selectWordClassMainClassIdAndSubClassIdByWordClassId For value wordClassId: $wordClassId"
        ){
            wordClassQueries.selectWordClassMainClassIdAndSubClassIdByWordClassId(wordClassId).awaitAsOneOrNull()
        }.flatMap { result -> DatabaseResult.Success(WordClassIdContainer(wordClassId, result.main_class_id, result.sub_class_id)) }
    }

    override suspend fun updateWordClassMainClassIdByWordClassId(
        wordClassId: Long,
        mainClassId: Long
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(wordClassQueries.updateWordClassMainClassIdByWordClassId(wordClassId, mainClassId))
    }

    override suspend fun updateWordClassSubClassIdByWordClassId(
        wordClassId: Long,
        subClassId: Long
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(wordClassQueries.updateWordClassSubClassIdByWordClassId(wordClassId, subClassId))
    }

    override suspend fun updateWordClassMainClassIdAndSubClassIdByWordClassId(
        wordClassId: Long,
        mainClassId: Long,
        subClassId: Long
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(wordClassQueries.updateWordClassMainClassIdAndSubClassIdByWordClassId(mainClassId, subClassId, wordClassId))
    }

    override suspend fun deleteWordClassByWordClassId(wordClassId: Long): DatabaseResult<Unit> {
        return dbHandler.wrapResult(wordClassQueries.deleteWordClassByWordClassId(wordClassId))
    }

    override suspend fun deleteWordClassByMainClassIdAndSubClassId(
        mainClassId: Long,
        subClassId: Long
    ): DatabaseResult<Unit> {
        return dbHandler.wrapResult(wordClassQueries.deleteWordClassByMainClassIdAndSubClassId(mainClassId, subClassId))
    }

}