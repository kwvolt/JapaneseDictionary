package io.github.kwvolt.japanesedictionary.domain.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable

class WordEntryFormUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: EntryRepositoryInterface,
    private val entryNoteRepository: EntryNoteRepositoryInterface,
    private val entrySectionRepository: EntrySectionRepositoryInterface,
    private val entrySectionKanaRepository: EntrySectionKanaInterface,
    private val entrySectionNoteRepository: EntrySectionNoteRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface,
) {
    suspend fun upsertWordEntryFormData(wordEntryFormData: WordEntryFormData): DatabaseResult<Unit> {

        val wordClassItem = wordEntryFormData.wordClassInput
        val wordClassIdResult = wordClassRepository.selectWordClassIdByMainClassIdAndSubClassId(wordClassItem.chosenMainClassId, wordClassItem.chosenSubClassId)
        val wordClassId = when (wordClassIdResult) {
            is DatabaseResult.Success -> wordClassIdResult.value
            else -> return wordClassIdResult.mapErrorTo<Long, Unit>()
        }

        return dbHandler.performTransaction {
            // Upsert dictionary entry
            val primaryText = wordEntryFormData.primaryTextInput.inputTextValue
            val entryId = wordEntryFormData.primaryTextInput.itemProperties.getId()
            val entryTableId: String = wordEntryFormData.primaryTextInput.itemProperties.getTableId()

            val dictionaryEntryResult = if (entryTableId == WordEntryTable.UI.toString()) {
                upsertDictionaryEntry(primaryText) {
                    entryRepository.insertDictionaryEntry(wordClassId, primaryText)
                }
            } else {
                upsertDictionaryEntry(primaryText) {
                    entryRepository.updateDictionaryEntryPrimaryText(entryId, primaryText)
                        .flatMap { DatabaseResult.Success(entryId) }
                }
            }

            val dictionaryEntryId = when (dictionaryEntryResult) {
                is DatabaseResult.Success -> dictionaryEntryResult.value
                else -> return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Unit>()
            }

            // Upsert dictionary entry notes

            val entryNotes = wordEntryFormData.entryNoteInputMap.values
            val entryNoteResult = dbHandler.processBatch(entryNotes){ entryNote ->
                val noteId = entryNote.itemProperties.getId()
                val noteTableId = entryNote.itemProperties.getTableId()
                val noteText = entryNote.inputTextValue

                if (noteTableId == WordEntryTable.UI.toString()) {
                    entryNoteRepository.insertDictionaryEntryNote(dictionaryEntryId, noteText).flatMap { DatabaseResult.Success(Unit) }
                } else {
                    entryNoteRepository.updateDictionaryEntryNote(noteId, noteText)
                }
            }
            if (entryNoteResult.isFailure) {
                return@performTransaction entryNoteResult.mapErrorTo<Unit, Unit>()
            }
            DatabaseResult.Success(Unit)
        }
    }

    private suspend fun upsertWordEntrySectionFormData(
        dictionaryEntryId: Long,
        wordSectionFormData: WordSectionFormData
    ): DatabaseResult<Unit> {

        val sectionId = wordSectionFormData.meaningInput.itemProperties.getId()
        val sectionTableId = wordSectionFormData.meaningInput.itemProperties.getTableId()
        val meaningText = wordSectionFormData.meaningInput.inputTextValue

        val dictionaryEntrySectionIdResult = if (sectionTableId == WordEntryTable.UI.toString()) {
            entrySectionRepository.insertDictionaryEntrySection(dictionaryEntryId, meaningText)
        } else {
            entrySectionRepository.updateDictionaryEntrySectionMeaning(sectionId, meaningText)
                .flatMap { DatabaseResult.Success(sectionId) }
        }

        val dictionaryEntrySectionId = when (dictionaryEntrySectionIdResult) {
            is DatabaseResult.Success -> dictionaryEntrySectionIdResult.value
            else -> return dictionaryEntrySectionIdResult.mapErrorTo<Long, Unit>()
        }

        val kanaItems = wordSectionFormData.kanaInputMap.values
        val kanaResult = dbHandler.processBatch(kanaItems){ kanaItem ->
            val kanaId = kanaItem.itemProperties.getId()
            val kanaTableId = kanaItem.itemProperties.getIdentifier()
            val kanaText = kanaItem.inputTextValue

            if (kanaTableId == WordEntryTable.UI.toString()) {
                upsertDictionaryEntryKana(kanaText) {
                    entrySectionKanaRepository.insertDictionaryEntrySectionKana(kanaId, kanaText)
                }.flatMap {  DatabaseResult.Success(Unit) }
            } else {
                upsertDictionaryEntryKana(kanaText){
                    entrySectionKanaRepository.updateDictionaryEntrySectionKana(kanaId, kanaText)
                }
            }
        }
        if (kanaResult.isFailure) {
            return kanaResult.mapErrorTo<Unit, Unit>()
        }

        val sectionNoteItems = wordSectionFormData.sectionNoteInputMap.values
        val sectionNoteResult = dbHandler.processBatch(sectionNoteItems){  sectionNoteItem ->
            val noteId =  sectionNoteItem.itemProperties.getId()
            val noteTableId =  sectionNoteItem.itemProperties.getTableId()
            val noteText =  sectionNoteItem.inputTextValue

            if (noteTableId == WordEntryTable.UI.toString()) {
                entrySectionNoteRepository.insertDictionaryEntrySectionNote(dictionaryEntrySectionId, noteText).flatMap { DatabaseResult.Success(Unit) }
            } else {
                entrySectionNoteRepository.updateDictionaryEntrySectionNote(noteId, noteText)
            }
        }

        if (sectionNoteResult.isFailure) {
            return sectionNoteResult.mapErrorTo<Unit, Unit>()
        }

        return DatabaseResult.Success(Unit)
    }

    private suspend fun <T>upsertDictionaryEntry(primaryText: String, block: suspend() -> DatabaseResult<T>): DatabaseResult<T>{
        return if(validJapanese(primaryText)){
            block()
        }
        else {
            DatabaseResult.InvalidInput(ValidationType.Japanese)
        }
    }

    private suspend fun <T>upsertDictionaryEntryKana(wordText: String, block: suspend() -> DatabaseResult<T>): DatabaseResult<T>{
        return if(validKana(wordText)){
            block()
        }
        else {
            DatabaseResult.InvalidInput(ValidationType.Kana)
        }
    }
}