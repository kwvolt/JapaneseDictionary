package io.github.kwvolt.japanesedictionary.domain.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepositoryInterface
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WordClassBuilder(
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
) {
    suspend fun getMainClassList(): DatabaseResult<List<MainClassContainer>> {
        return mainClassRepository.selectAllMainClass()
    }

    suspend fun getSubClassMap(mainClassList: List<MainClassContainer>): DatabaseResult<Map<Long, List<SubClassContainer>>> = coroutineScope {
        val deferredMap: Map<Long, Deferred<DatabaseResult<List<SubClassContainer>>>> =
            mainClassList.associate { mainClass ->
                mainClass.id to async {
                    subClassRepository.selectAllSubClassByMainClassId(mainClass.id)
                }
            }
        val resultMap = mutableMapOf<Long, List<SubClassContainer>>()
        for ((mainClassId, deferredResult) in deferredMap) {
            when (val result = deferredResult.await()) {
                is DatabaseResult.Success -> resultMap[mainClassId] = result.value
                else -> return@coroutineScope result.mapErrorTo<List<SubClassContainer>, Map<Long, List<SubClassContainer>>>()
            }
        }
        DatabaseResult.Success(resultMap)
    }
}