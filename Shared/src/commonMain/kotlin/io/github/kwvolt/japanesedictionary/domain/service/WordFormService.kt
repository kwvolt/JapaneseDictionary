package io.github.kwvolt.japanesedictionary.domain.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormStateManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class WordFormService(
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: EntryRepositoryInterface,
    private val entryNoteRepository: EntryNoteRepositoryInterface,
    private val entrySectionRepository: EntrySectionRepositoryInterface,
    private val entrySectionKanaRepository: EntrySectionKanaInterface,
    private val entrySectionNoteRepository: EntrySectionNoteRepositoryInterface,
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface,
    private val wordEntryFormBuilder: WordEntryFormBuilder,
    private val wordEntryFormInsert: WordEntryFormInsert
) {
    suspend fun createWordFormData(dictionaryEntryId: Long, formStateManager: FormStateManager): DatabaseResult<WordEntryFormData> {
        return wordEntryFormBuilder.createWordFormData(dictionaryEntryId, formStateManager)
    }

    suspend fun getSubClassMap(): DatabaseResult<Map<Long, List<SubClassContainer>>> = coroutineScope {
        mainClassRepository.selectAllMainClass().flatMap { allMainClass ->
            val deferredMap: Map<Long, Deferred<DatabaseResult<List<SubClassContainer>>>> =
                allMainClass.associate { mainClass ->
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

    suspend fun upsertWordEntryFormDataIntoDatabase(wordEntryFormData: WordEntryFormData): DatabaseResult<Unit> {
        return wordEntryFormInsert.upsertWordEntryFormData(wordEntryFormData)
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long){
        val result = createWordFormData(dictionaryEntryId)
    }
}