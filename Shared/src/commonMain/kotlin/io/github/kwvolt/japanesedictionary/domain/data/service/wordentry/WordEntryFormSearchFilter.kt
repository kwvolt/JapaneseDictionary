package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySearchRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.model.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.SearchType

class WordEntryFormSearchFilter(
    private val _wordClassRepository: WordClassRepositoryInterface,
    private val _dictionarySearchRepository: DictionarySearchRepositoryInterface
) {
    suspend fun searchBasedOnFilter(searchTerm: String, searchFilter: SearchFilter): DatabaseResult<List<Long>>{
        val wordClassId: Long = with(searchFilter) {
            if (mainClassId != -1L && subClassId != -1L) {
                val wordClassResult: DatabaseResult<Long> =
                    _wordClassRepository.selectIdByMainClassIdAndSubClassId(mainClassId, subClassId)
                when(wordClassResult){
                    is DatabaseResult.Success -> { wordClassResult.value }
                    else -> return wordClassResult.mapErrorTo<Long, List<Long>>()
                }
            } else {
                WordClassDataManager.NO_ID
            }
        }
        val searchBookmark = searchFilter.isBookmark
        val searchLocation = searchFilter.searchType


        val idList: MutableList<Long> = mutableListOf()

        if(searchLocation == SearchType.KANJI || searchLocation == SearchType.ALL){
            val result: DatabaseResult<List<Long>> = _dictionarySearchRepository.searchIdsByPrimaryText(searchTerm)
            when(result){
                is DatabaseResult.Success -> idList.innerJoin(result.value)
                else -> return result.mapErrorTo<List<Long>, List<Long>>()
            }
        }

        if(searchLocation == SearchType.KANA || searchLocation == SearchType.ALL){
            val result: DatabaseResult<List<Long>> = _dictionarySearchRepository.searchIdsByKana(searchTerm)
            when(result){
                is DatabaseResult.Success -> idList.innerJoin(result.value)
                else -> return result.mapErrorTo<List<Long>, List<Long>>()
            }
        }

        if(searchLocation == SearchType.MEANING || searchLocation == SearchType.ALL){
            val result: DatabaseResult<List<Long>> = _dictionarySearchRepository.searchIdsByMeaning(searchTerm)
            when(result){
                is DatabaseResult.Success -> idList.innerJoin(result.value)
                else ->  return result.mapErrorTo<List<Long>, List<Long>>()
            }
        }

        if(wordClassId != WordClassDataManager.NO_ID){
            val result: DatabaseResult<List<Long>> = _dictionarySearchRepository.searchIdsByWordClassId(wordClassId)
            when(result){
                is DatabaseResult.Success -> idList.innerJoin(result.value)
                else ->  return result.mapErrorTo<List<Long>, List<Long>>()
            }
        }

        if(searchBookmark){
            val result: DatabaseResult<List<Long>> = _dictionarySearchRepository.searchIdsByIsBookmark(
                true
            )
            when(result){
                is DatabaseResult.Success -> idList.innerJoin(result.value)
                else ->  return result.mapErrorTo<List<Long>,List<Long>>()
            }
        }
        return DatabaseResult.Success(idList.toList().sorted())
    }

    private fun MutableList<Long>.innerJoin(otherList: List<Long>) {
        if (this.isEmpty()) {
            this.addAll(otherList)
        } else {
            val intersection = this.intersect(otherList.toSet())
            this.clear()
            this.addAll(intersection)
        }
    }
}