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
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
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
    private val wordEntryFormBuilder: WordEntryFormBuilder
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

    suspend fun insertWordEntryFormDataIntoDatabase(wordEntryFormData: WordEntryFormData): DatabaseResult<Unit>{

        // WordClassId
        val wordClassItem: WordClassItem = wordEntryFormData.wordClassInput
        val wordClassIdResult:DatabaseResult<Long> = wordClassRepository.selectWordClassIdByMainClassIdAndSubClassId(wordClassItem.chosenMainClassId, wordClassItem.chosenSubClassId)
        val wordClassId: Long = when (wordClassIdResult) {
            is DatabaseResult.Success -> wordClassIdResult.value
            else -> return wordClassIdResult.mapErrorTo<Long, Unit>()
        }
        return dbHandler.performTransaction {
            // dictionary entry
            val dictionaryEntryResult: DatabaseResult<Long> = insertDictionaryEntry(wordClassId, wordEntryFormData.primaryTextInput.inputTextValue)
            val dictionaryEntryId: Long = when (dictionaryEntryResult) {
                is DatabaseResult.Success -> dictionaryEntryResult.value
                else -> return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Unit>()
            }

            // dictionary entry notes
            for(entryNote in wordEntryFormData.entryNoteInputMap.values){
                val dictionaryEntryNoteResult: DatabaseResult<Long> = entryNoteRepository.insertDictionaryEntryNote(dictionaryEntryId, entryNote.inputTextValue)
                if(dictionaryEntryNoteResult.isFailure){
                    return@performTransaction dictionaryEntryNoteResult.mapErrorTo<Long, Unit>()
                }
            }

            // dictionary entry section
            for(wordSectionFormData in wordEntryFormData.wordSectionMap.values){
                val wordSectionFormDataResult: DatabaseResult<Unit> = insertWordEntrySectionFormDataIntoDatabase(dictionaryEntryId, wordSectionFormData)
                if(wordSectionFormDataResult.isFailure){
                    return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Unit>()
                }
            }
        }.flatMap { DatabaseResult.Success(Unit) }
    }

    private suspend fun insertWordEntrySectionFormDataIntoDatabase(dictionaryEntryId: Long, wordSectionFormData: WordSectionFormData): DatabaseResult<Unit>{
        // dictionary entry section
        val dictionaryEntrySectionIdResult: DatabaseResult<Long> = entrySectionRepository.insertDictionaryEntrySection(dictionaryEntryId, wordSectionFormData.meaningInput.inputTextValue)
        val dictionaryEntrySectionId: Long = when (dictionaryEntrySectionIdResult) {
            is DatabaseResult.Success -> dictionaryEntrySectionIdResult.value
            else -> return dictionaryEntrySectionIdResult.mapErrorTo<Long, Unit>()
        }

        // kana
        for(kanaItem in wordSectionFormData.kanaInputMap.values){
            val kanaIdResult: DatabaseResult<Long> = insertDictionaryEntryKana(dictionaryEntrySectionId, kanaItem.inputTextValue)
            if(kanaIdResult.isFailure){
                return kanaIdResult.mapErrorTo<Long, Unit>()
            }
        }

        // entry section note
        for(sectionNoteItem in wordSectionFormData.sectionNoteInputMap.values){
            val sectionNoteIdResult: DatabaseResult<Long> = entrySectionNoteRepository.insertDictionaryEntrySectionNote(dictionaryEntrySectionId, sectionNoteItem.inputTextValue)
            if(sectionNoteIdResult.isFailure) {
                return sectionNoteIdResult.mapErrorTo<Long, Unit>()
            }
        }

        return DatabaseResult.Success(Unit)
    }
    /*
    suspend fun upsertWordEntryFormData(wordEntryFormData: WordEntryFormData): DatabaseResult<Unit> {
        val wordClassItem = wordEntryFormData.wordClassInput
        val wordClassIdResult = wordClassRepository
            .selectWordClassIdByMainClassIdAndSubClassId(wordClassItem.chosenMainClassId, wordClassItem.chosenSubClassId)

        val wordClassId = when (wordClassIdResult) {
            is DatabaseResult.Success -> wordClassIdResult.value
            else -> return wordClassIdResult.mapErrorTo<Long, Unit>()
        }

        return dbHandler.performTransaction {
            // Upsert dictionary entry
            val primaryText = wordEntryFormData.primaryTextInput.inputTextValue
            val entryId = wordEntryFormData.primaryTextInput.itemProperties.id
            val dictionaryEntryResult = if (entryId != null) {
                entryRepository.updateDictionaryEntryPrimaryText(entryId, primaryText)
                    .flatMap { DatabaseResult.Success(entryId) }
            } else {
                insertDictionaryEntry(wordClassId, primaryText)
            }

            val dictionaryEntryId = when (dictionaryEntryResult) {
                is DatabaseResult.Success -> dictionaryEntryResult.value
                else -> return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Unit>()
            }

            // Upsert dictionary entry notes
            for (entryNote in wordEntryFormData.entryNoteInputMap.values) {
                val noteId = entryNote.itemProperties.id
                val noteText = entryNote.inputTextValue

                val result = if (noteId != null) {
                    entryNoteRepository.updateDictionaryEntryNote(noteId, noteText)
                } else {
                    entryNoteRepository.insertDictionaryEntryNote(dictionaryEntryId, noteText)
                }

                if (result.isFailure) return@performTransaction result.mapErrorTo<Long, Unit>()
            }

            // Upsert sections
            for (sectionData in wordEntryFormData.wordSectionMap.values) {
                val result = upsertWordEntrySectionFormData(dictionaryEntryId, sectionData)
                if (result.isFailure) return@performTransaction result.mapErrorTo<Long, Unit>()
            }

            Unit
        }.flatMap { DatabaseResult.Success(Unit) }
    }

    private suspend fun upsertWordEntrySectionFormData(
        dictionaryEntryId: Long,
        wordSectionFormData: WordSectionFormData
    ): DatabaseResult<Unit> {

        val sectionId = wordSectionFormData.meaningInput.itemProperties.id
        val meaningText = wordSectionFormData.meaningInput.inputTextValue

        val dictionaryEntrySectionIdResult = if (sectionId != null) {
            entrySectionRepository.updateDictionaryEntrySectionMeaning(sectionId, meaningText)
                .flatMap { DatabaseResult.Success(sectionId) }
        } else {
            entrySectionRepository.insertDictionaryEntrySection(dictionaryEntryId, meaningText)
        }

        val dictionaryEntrySectionId = when (dictionaryEntrySectionIdResult) {
            is DatabaseResult.Success -> dictionaryEntrySectionIdResult.value
            else -> return dictionaryEntrySectionIdResult.mapErrorTo<Long, Unit>()
        }

        for (kanaItem in wordSectionFormData.kanaInputMap.values) {
            val kanaId = kanaItem.itemProperties.id
            val kanaText = kanaItem.inputTextValue

            val kanaResult = if (kanaId != null) {
                entrySectionKanaRepository.updateDictionaryEntrySectionKana(kanaId, kanaText)
            } else {
                insertDictionaryEntryKana(dictionaryEntrySectionId, kanaText)
            }

            if (kanaResult.isFailure) return kanaResult.mapErrorTo<Long, Unit>()
        }

        for (noteItem in wordSectionFormData.sectionNoteInputMap.values) {
            val noteId = noteItem.itemProperties.id
            val noteText = noteItem.inputTextValue

            val noteResult = if (noteId != null) {
                entrySectionNoteRepository.updateDictionaryEntrySectionNote(noteId, noteText)
            } else {
                entrySectionNoteRepository.insertDictionaryEntrySectionNote(dictionaryEntrySectionId, noteText)
            }

            if (noteResult.isFailure) return noteResult.mapErrorTo<Long, Unit>()
        }

        return DatabaseResult.Success(Unit)
    }*/


    private suspend fun insertDictionaryEntry(wordClassId: Long, primaryText: String): DatabaseResult<Long>{
        return if(validJapanese(primaryText)){
            entryRepository.insertDictionaryEntry(wordClassId, primaryText)
        }
        else {
            DatabaseResult.InvalidInput(ValidationType.Japanese)
        }
    }

    private suspend fun insertDictionaryEntryKana(dictionaryEntrySectionId: Long, wordText: String): DatabaseResult<Long>{
        return if(validKana(wordText)){
            entrySectionKanaRepository.insertDictionaryEntrySectionKana(dictionaryEntrySectionId, wordText)
        }
        else {
            DatabaseResult.InvalidInput(ValidationType.Kana)
        }
    }


}