package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.util.CoroutineEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class WordEntryFormFetcher(
    private val dictionaryEntryRepository: DictionaryRepositoryInterface,
    private val dictionaryEntryNoteRepository: DictionaryNoteRepositoryInterface,
    private val dictionarySectionRepository: SectionRepositoryInterface,
    private val dictionarySectionKanaRepository: SectionKanaRepositoryInterface,
    private val dictionarySectionNoteRepository: SectionNoteRepositoryInterface,
) {
    private val semaphore = Semaphore(CoroutineEnvironment.semaphoreCount)

    suspend fun fetchDictionaryEntryContainer(dictionaryEntryId: Long): DatabaseResult<DictionaryEntryContainer>{
        return safeDbCall {dictionaryEntryRepository.selectRow(dictionaryEntryId)}
    }

    suspend fun fetchDictionaryEntryNoteContainer(dictionaryEntryId: Long):  DatabaseResult<DictionaryEntryNoteContainer>{
        return safeDbCall {dictionaryEntryNoteRepository.selectRow(dictionaryEntryId)}
    }

    suspend fun fetchDictionaryEntryNoteContainerList(dictionaryEntryId: Long):  DatabaseResult<List<DictionaryEntryNoteContainer>>{
        return safeDbCall {dictionaryEntryNoteRepository.selectAllById(dictionaryEntryId)}
    }

    suspend fun fetchDictionarySectionContainer(dictionarySectionId: Long):DatabaseResult<DictionarySectionContainer>{
        return safeDbCall {dictionarySectionRepository.selectRow(dictionarySectionId)}
    }

    suspend fun fetchDictionarySectionContainerList(dictionaryEntryId: Long):  DatabaseResult<List<DictionarySectionContainer>>{
        return safeDbCall {dictionarySectionRepository.selectAllByEntryId(dictionaryEntryId)}
    }

    suspend fun fetchDictionarySectionKanaContainer(kanaId: Long):  DatabaseResult<DictionarySectionKanaContainer>{
        return safeDbCall {dictionarySectionKanaRepository.selectRow(kanaId)}
    }

    suspend fun fetchDictionarySectionKanaContainerList(dictionarySectionId: Long):  DatabaseResult<List<DictionarySectionKanaContainer>>{
        return safeDbCall {dictionarySectionKanaRepository.selectAllBySectionId(dictionarySectionId)}
    }

    suspend fun fetchDictionarySectionNoteContainer(sectionNoteId: Long):  DatabaseResult<DictionarySectionNoteContainer>{
        return safeDbCall {dictionarySectionNoteRepository.selectRow(sectionNoteId)}
    }

    suspend fun fetchDictionarySectionNoteContainerList(dictionarySectionId: Long):  DatabaseResult<List<DictionarySectionNoteContainer>>{
        return safeDbCall {dictionarySectionNoteRepository.selectAllBySectionId(dictionarySectionId)}
    }

    private suspend fun <T> safeDbCall(block: suspend () -> T): T{
        return withDbTimeout { semaphore.withPermit { block() } }
    }

    private suspend fun <T> withDbTimeout(block: suspend () -> T): T {
        return if (CoroutineEnvironment.isTestEnvironment) {
            withContext(Dispatchers.Default.limitedParallelism(1)) {
                withTimeout(CoroutineEnvironment.dbTimeOutMillis) { block() }
            }
        } else {
            withTimeout(CoroutineEnvironment.dbTimeOutMillis) { block() }
        }
    }
}