package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormSectionManager

class WordFormService(
    private val wordEntryFormBuilder: WordEntryFormBuilder,
    private val wordEntryFormUpsert: WordEntryFormUpsert,
    private val wordEntryFormDelete: WordEntryFormDelete,
    private val wordClassBuilder: WordClassBuilder,
    private val wordEntryFormValidation: WordEntryFormValidation
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

    suspend fun upsertWordEntryFormDataIntoDatabase(wordEntryFormData: WordEntryFormData, deleteList: List<GenericItemProperties>): DatabaseResult<Unit> {
        when(val result  = wordEntryFormValidation.validateForm(wordEntryFormData)){
            is DatabaseResult.Success -> {
                val wordClassId: Long = result.value
                return wordEntryFormUpsert.upsertWordEntryFormData(wordEntryFormData, wordClassId, deleteList)
            }
            else -> {
                return result.mapErrorTo<Long, Unit>()
            }
        }
    }

    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit>  {
        return wordEntryFormDelete.deleteWordEntryFormData(dictionaryEntryId)
    }
}