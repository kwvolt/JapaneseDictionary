package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionRepositoryInterface

class WordEntryFormDelete (
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: DictionaryRepositoryInterface,
    private val entryNoteRepository: DictionaryNoteRepositoryInterface,
    private val entrySectionRepository: SectionRepositoryInterface,
    private val entrySectionKanaRepository: SectionKanaRepositoryInterface,
    private val entrySectionNoteRepository: SectionNoteRepositoryInterface,
){
    suspend fun deletePrimaryText(dictionaryId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entryRepository.delete(dictionaryId, itemId)
    }
    suspend fun deleteEntryNote(dictionaryNoteId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entryNoteRepository.deleteRow(dictionaryNoteId, itemId)
    }
    suspend fun deleteSectionMeaning(meaningId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionRepository.delete(meaningId, itemId)
    }
    suspend fun deleteSectionKana(kanaId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionKanaRepository.delete(kanaId, itemId)
    }
    suspend fun deleteSectionNote(sectionNoteId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionNoteRepository.delete(sectionNoteId, itemId)
    }
    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit> {
        val entryNoteIdList: DatabaseResult<List<Long>> = entryNoteRepository.selectAllById(dictionaryEntryId).flatMapList{ item -> item.id}
        val sectionIdList: DatabaseResult<List<Long>> = entrySectionRepository.selectAllByEntryId(dictionaryEntryId).flatMapList{ item -> item.id}

        val kanaNestedList: MutableList<List<Long>> = mutableListOf()
        val sectionNoteNestedList: MutableList<List<Long>> = mutableListOf()

        when(sectionIdList){
            is DatabaseResult.Success -> {
                for(sectionId: Long in sectionIdList.value){
                    val kanaList: List<Long> =
                        entrySectionKanaRepository.selectAllBySectionId(sectionId)
                            .flatMapList{ item -> item.id}
                            .getOrReturn { return it }
                    kanaNestedList.add(kanaList)

                    val sectionNoteList: List<Long>
                        = entrySectionNoteRepository.selectAllBySectionId(sectionId)
                            .flatMapList{ item -> item.id}
                            .getOrReturn { return it }
                    sectionNoteNestedList.add(sectionNoteList)
                }
            }
            else -> return sectionIdList.mapErrorTo()
        }

        val kanaList : List<Long> = kanaNestedList.flatten()
        val sectionNoteList: List<Long> = sectionNoteNestedList.flatten()


        return dbHandler.performTransaction {
            // entry section note
            for(sectionNoteId in sectionNoteList){
                val result = entrySectionNoteRepository.delete(sectionNoteId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo()
                }
            }
            for(kanaId in kanaList){
                val result = entrySectionKanaRepository.delete(kanaId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo()
                }
            }

            for(sectionId in sectionIdList.value){
                val result =  entrySectionRepository.delete(sectionId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo()
                }
            }

            when(entryNoteIdList){
                is DatabaseResult.Success -> {
                    for(entryNoteId in entryNoteIdList.value){
                        val result =  entryNoteRepository.deleteRow(entryNoteId)
                        if(result.isFailure){
                            return@performTransaction result.mapErrorTo()
                        }
                    }
                }
                else -> return@performTransaction entryNoteIdList.mapErrorTo()
            }

            val dictionaryResult = entryRepository.delete(dictionaryEntryId)
            if(dictionaryResult.isFailure){
                return@performTransaction dictionaryResult.mapErrorTo()
            }
            DatabaseResult.Success(Unit)
        }
    }

    private inline fun <T, R> DatabaseResult<List<T>>.flatMapList(mapper: (T) -> R): DatabaseResult<List<R>> =
        this.flatMap { list -> DatabaseResult.Success(list.map(mapper)) }
}