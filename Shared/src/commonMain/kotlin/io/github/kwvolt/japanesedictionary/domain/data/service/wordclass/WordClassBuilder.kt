package io.github.kwvolt.japanesedictionary.domain.data.service.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassRepositoryInterface
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.collections.iterator

class WordClassBuilder(
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
) {
    suspend fun getMainClassList(): DatabaseResult<List<MainClassContainer>> {
        return mainClassRepository.selectAll()
    }

    suspend fun getSubClassMap(mainClassList: List<MainClassContainer>): DatabaseResult<Map<Long, List<SubClassContainer>>> = coroutineScope {
        val deferredMap: Map<Long, Deferred<DatabaseResult<List<SubClassContainer>>>> =
            mainClassList.associate { mainClass ->
                mainClass.id to async {
                    subClassRepository.selectAllByMainClassId(mainClass.id)
                }
            }
        val resultMap = mutableMapOf<Long, List<SubClassContainer>>()
        for ((mainClassId, deferredResult) in deferredMap) {
            when (val result = deferredResult.await()) {
                is DatabaseResult.Success -> resultMap[mainClassId] = result.value
                else -> return@coroutineScope result.mapErrorTo()
            }
        }
        DatabaseResult.Success(resultMap)
    }
}