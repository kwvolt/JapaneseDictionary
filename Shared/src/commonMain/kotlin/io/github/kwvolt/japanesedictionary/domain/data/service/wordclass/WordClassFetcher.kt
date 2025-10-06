package io.github.kwvolt.japanesedictionary.domain.data.service.wordclass

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassIdContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem

class WordClassFetcher(
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface
    ) {

    // word class id
    suspend fun fetchWordClassId(mainClassId: Long, subClassId: Long): DatabaseResult<Long>{
        return wordClassRepository.selectIdByMainClassIdAndSubClassId(mainClassId, subClassId)
    }

    suspend fun fetchWordClassId(mainClassId: Long, subClassIdName: String): DatabaseResult<Long> {
        val subClassResult: DatabaseResult<Long> = fetchSubClassId(subClassIdName)
        return when(subClassResult){
            is DatabaseResult.Success -> fetchWordClassId(mainClassId , subClassResult.value)
            else -> subClassResult
        }
    }

    suspend fun fetchWordClassId(mainClassIdName: String, subClassId: Long): DatabaseResult<Long>{
        val mainClassResult: DatabaseResult<Long> = fetchMainClassId(mainClassIdName)
        return when(mainClassResult){
            is DatabaseResult.Success -> fetchWordClassId(mainClassResult.value, subClassId)
            else -> mainClassResult
        }
    }

    suspend fun fetchWordClassId(mainClassIdName: String, subClassIdName: String): DatabaseResult<Long>{
        return fetchMainClassId(mainClassIdName).flatMap { mainClassId: Long ->
            fetchSubClassId(subClassIdName).flatMap { subClassId: Long ->
                fetchWordClassId(mainClassId, subClassId)
            }
        }
    }

    // main and sub id
    suspend fun fetchMainClassId(mainClassIdName: String): DatabaseResult<Long>{
        return mainClassRepository.selectId(mainClassIdName)
    }

    suspend fun fetchSubClassId(subClassIdName: String): DatabaseResult<Long>{
        return subClassRepository.selectId(subClassIdName)
    }

    suspend fun fetchMainIdAndSubId(wordClassId: Long): DatabaseResult<WordClassIdContainer>{
        return wordClassRepository.selectRow(wordClassId)
    }

    // main class container
    suspend fun fetchMainClassContainer(mainClassId: Long): DatabaseResult<MainClassContainer>{
        return mainClassRepository.selectRowById(mainClassId)
    }

    suspend fun fetchMainClassContainer(mainClassIdName: String): DatabaseResult<MainClassContainer>{
        return mainClassRepository.selectRowByIdName(mainClassIdName)
    }

    // sub class container
    suspend fun fetchSubClassContainer(subClassId: Long): DatabaseResult<SubClassContainer>{
        return subClassRepository.selectRowById(subClassId)
    }

    suspend fun fetchSubClassContainer(subClassIdName: String): DatabaseResult<SubClassContainer>{
        return subClassRepository.selectRowByIdName(subClassIdName)
    }

    // wordClassItem
    suspend fun fetchWordClassItem(wordClassId: Long): DatabaseResult<WordClassItem>{
        return fetchMainIdAndSubId(wordClassId).flatMap { wordClassResult: WordClassIdContainer ->
            fetchMainClassContainer(wordClassResult.mainClassId).flatMap { mainClassContainer: MainClassContainer ->
                fetchSubClassContainer(wordClassResult.subClassId).map { subClassContainer: SubClassContainer ->
                    WordClassItem(
                        mainClassContainer,
                        subClassContainer,
                        ItemProperties(tableId = WordEntryTable.WORD_CLASS, id = wordClassResult.wordClassId)
                    )
                }
            }
        }
    }

    suspend fun fetchWordClassItem(mainClassId: Long, subClassId: Long): DatabaseResult<WordClassItem> =
        fetchWordClassItemFromIdResult(fetchWordClassId(mainClassId, subClassId))

    suspend fun fetchWordClassItem(mainClassId: Long, subClassIdName: String): DatabaseResult<WordClassItem> =
        fetchWordClassItemFromIdResult(fetchWordClassId(mainClassId, subClassIdName))

    suspend fun fetchWordClassItem(mainClassIdName: String, subClassId: Long): DatabaseResult<WordClassItem> =
        fetchWordClassItemFromIdResult(fetchWordClassId(mainClassIdName, subClassId))

    suspend fun fetchWordClassItem(mainClassIdName: String, subClassIdName: String): DatabaseResult<WordClassItem> =
        fetchWordClassItemFromIdResult(fetchWordClassId(mainClassIdName, subClassIdName))

    private suspend fun fetchWordClassItemFromIdResult(result: DatabaseResult<Long>): DatabaseResult<WordClassItem> {
        return result.flatMap { id -> fetchWordClassItem(id) }
    }
}