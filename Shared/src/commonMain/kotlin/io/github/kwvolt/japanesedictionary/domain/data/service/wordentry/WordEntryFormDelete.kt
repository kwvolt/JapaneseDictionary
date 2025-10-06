package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionRepositoryInterface

class WordEntryFormDelete (
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: DictionaryRepositoryInterface,
    private val entryNoteRepository: DictionaryNoteRepositoryInterface,
    private val entrySectionRepository: SectionRepositoryInterface,
    private val entrySectionKanaRepository: SectionKanaRepositoryInterface,
    private val entrySectionNoteRepository: SectionNoteRepositoryInterface,
){

    suspend fun deletePrimaryText(dictionaryId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entryRepository.deleteRow(dictionaryId, itemId)
    }

    suspend fun deleteEntryNote(dictionaryNoteId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entryNoteRepository.deleteRow(dictionaryNoteId, itemId)
    }

    suspend fun deleteSectionMeaning(meaningId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionRepository.deleteRow(meaningId, itemId)
    }

    suspend fun deleteSectionKana(kanaId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionKanaRepository.deleteRow(kanaId, itemId)
    }

    suspend fun deleteSectionNote(sectionNoteId: Long, itemId: String? = null): DatabaseResult<Unit>{
        return entrySectionNoteRepository.deleteRow(sectionNoteId, itemId)
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit> {
        val entryNoteIdList: DatabaseResult<List<Long>> = entryNoteRepository.selectAllById(dictionaryEntryId).flatMapList{ item -> item.id}

        val sectionIdList: DatabaseResult<List<Long>> = entrySectionRepository.selectAllByEntryId(dictionaryEntryId).flatMapList{ item -> item.id}

        val kanaNestedList: MutableList<List<Long>> = mutableListOf()
        val sectionNoteNestedList: MutableList<List<Long>> = mutableListOf()

        when(sectionIdList){
            is DatabaseResult.Success -> {
                for(sectionId: Long in sectionIdList.value){
                    val kanaResult: DatabaseResult<List<Long>> = entrySectionKanaRepository.selectAllBySectionId(sectionId).flatMapList{ item -> item.id}
                    when(kanaResult){
                        is DatabaseResult.Success -> {
                            kanaNestedList.add(kanaResult.value)
                        }
                        else -> return kanaResult.mapErrorTo<List<Long>, Unit>()
                    }
                    val sectionNoteResult: DatabaseResult<List<Long>> = entrySectionNoteRepository.selectAllBySectionId(sectionId).flatMapList{ item -> item.id}
                    when(sectionNoteResult){
                        is DatabaseResult.Success -> {
                            sectionNoteNestedList.add(sectionNoteResult.value)
                        }
                        else -> return sectionNoteResult.mapErrorTo<List<Long>, Unit>()
                    }
                }
            }
            else -> return sectionIdList.mapErrorTo<List<Long>, Unit>()
        }

        val kanaList : List<Long> = kanaNestedList.flatten()
        val sectionNoteList: List<Long> = sectionNoteNestedList.flatten()


        return dbHandler.performTransaction {
            // entry section note
            for(sectionNoteId in sectionNoteList){
                val result = entrySectionNoteRepository.deleteRow(sectionNoteId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo<Unit, Unit>()
                }
            }
            for(kanaId in kanaList){
                val result = entrySectionKanaRepository.deleteRow(kanaId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo<Unit, Unit>()
                }
            }

            for(sectionId in sectionIdList.value){
                val result =  entrySectionRepository.deleteRow(sectionId)
                if(result.isFailure){
                    return@performTransaction result.mapErrorTo<Unit, Unit>()
                }
            }

            when(entryNoteIdList){
                is DatabaseResult.Success -> {
                    for(entryNoteId in entryNoteIdList.value){
                        val result =  entryNoteRepository.deleteRow(entryNoteId)
                        if(result.isFailure){
                            return@performTransaction result.mapErrorTo<Unit, Unit>()
                        }
                    }
                }
                else -> return@performTransaction entryNoteIdList.mapErrorTo<List<Long>, Unit>()
            }

            val dictionaryResult = entryRepository.deleteRow(dictionaryEntryId)
            if(dictionaryResult.isFailure){
                return@performTransaction dictionaryResult.mapErrorTo<Unit, Unit>()
            }
            DatabaseResult.Success(Unit)
        }
    }

    private inline fun <T, R> DatabaseResult<List<T>>.flatMapList(mapper: (T) -> R): DatabaseResult<List<R>> =
        this.flatMap { list -> DatabaseResult.Success(list.map(mapper)) }
}