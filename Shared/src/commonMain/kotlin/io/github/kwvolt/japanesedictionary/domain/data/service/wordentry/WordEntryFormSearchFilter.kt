package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySearchRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchType

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
                    else -> return wordClassResult.mapErrorTo()
                }
            } else {
                WordClassDataManager.NO_ID
            }
        }
        val searchBookmark = searchFilter.isBookmark
        val searchLocation = searchFilter.searchType


        val idList: MutableList<Long> = mutableListOf()

        idList.searchFilter(searchLocation == SearchType.KANJI || searchLocation == SearchType.ALL,
            searchQuery = {_dictionarySearchRepository.searchIdsByPrimaryText(searchTerm)},
            errorTo = { return it }
        )

        idList.searchFilter(searchLocation == SearchType.KANA || searchLocation == SearchType.ALL,
            searchQuery = {_dictionarySearchRepository.searchIdsByKana(searchTerm)},
            errorTo = { return it }
        )

        idList.searchFilter(searchLocation == SearchType.MEANING || searchLocation == SearchType.ALL,
            searchQuery = { _dictionarySearchRepository.searchIdsByMeaning(searchTerm)},
            errorTo = { return it }
        )

        idList.searchFilter(wordClassId != WordClassDataManager.NO_ID,
            searchQuery = { _dictionarySearchRepository.searchIdsByWordClassId(wordClassId)},
            errorTo = { return it }
        )

        idList.searchFilter(searchBookmark,
            searchQuery = { _dictionarySearchRepository.searchIdsByIsBookmark(true)},
            errorTo = { return it }
        )
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

    private suspend inline fun MutableList<Long>.searchFilter(
        condition: Boolean,
        searchQuery: suspend() -> DatabaseResult<List<Long>>,
        errorTo: (DatabaseResult<List<Long>>) -> Nothing) {
        if(condition){
            val result: DatabaseResult<List<Long>> = searchQuery()
            when(result){
                is DatabaseResult.Success -> innerJoin(result.value)
                else ->  errorTo(result.mapErrorTo())
            }
        }
    }
}