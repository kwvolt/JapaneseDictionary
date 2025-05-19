package io.github.kwvolt.japanesedictionary.domain.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormSectionManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class WordFormService(
    private val wordEntryFormBuilder: WordEntryFormBuilder,
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormDelete: WordEntryFormDelete,
    private val wordClassBuilder: WordClassBuilder
) {
    suspend fun getWordFormData(dictionaryEntryId: Long, formSectionManager: FormSectionManager): DatabaseResult<WordEntryFormData> {
        return wordEntryFormBuilder.createWordFormData(dictionaryEntryId, formSectionManager)
    }


    suspend fun getMainClassList(): DatabaseResult<List<MainClassContainer>>{
        return wordClassBuilder.getMainClassList()
    }

    suspend fun getSubClassMap(mainClassList: List<MainClassContainer>): DatabaseResult<Map<Long, List<SubClassContainer>>>  {
        return wordClassBuilder.getSubClassMap(mainClassList)
    }

    suspend fun upsertWordEntryFormDataIntoDatabase(wordEntryFormData: WordEntryFormData): DatabaseResult<Unit> {
        return wordEntryFormUpsert.upsertWordEntryFormData(wordEntryFormData)
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit>  {
        return wordEntryFormDelete.deleteWordEntryFormData(dictionaryEntryId)
    }
}