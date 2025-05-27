package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType
import io.github.kwvolt.japanesedictionary.domain.data.validation.findDuplicateIdentifiers
import io.github.kwvolt.japanesedictionary.domain.data.validation.validJapanese
import io.github.kwvolt.japanesedictionary.domain.data.validation.validKana
import io.github.kwvolt.japanesedictionary.domain.data.validation.validateNotEmpty
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import java.lang.IllegalArgumentException

class WordEntryFormUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: EntryRepositoryInterface,
    private val entryNoteRepository: EntryNoteRepositoryInterface,
    private val entrySectionRepository: EntrySectionRepositoryInterface,
    private val entrySectionKanaRepository: EntrySectionKanaInterface,
    private val entrySectionNoteRepository: EntrySectionNoteRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface,
) {
    suspend fun upsertWordEntryFormData(wordEntryFormData: WordEntryFormData, wordClassId: Long, deleteList: List<GenericItemProperties> = mutableListOf()): DatabaseResult<Unit> {
        return dbHandler.performTransaction {
            val sectionListToDelete: MutableList<GenericItemProperties> = mutableListOf()
            // deletes from db
            for(deleteItem in deleteList){
                val identifier = deleteItem.getIdentifier()
                if (identifier.isBlank()) {
                    return@performTransaction DatabaseResult.UnknownError(IllegalArgumentException(),"Missing identifier for item: $deleteItem")
                }
                val wordEntryType = WordEntryTable.fromValue(identifier)
                val deleteResult: DatabaseResult<Unit> =  when(wordEntryType){
                    WordEntryTable.DICTIONARY_ENTRY_NOTE -> entryNoteRepository.deleteDictionaryEntryNote(deleteItem.getId())
                    WordEntryTable.DICTIONARY_SECTION_NOTE -> entrySectionNoteRepository.deleteDictionaryEntrySectionNote(deleteItem.getId())
                    WordEntryTable.DICTIONARY_SECTION_KANA -> entrySectionKanaRepository.deleteDictionaryEntrySectionKana(deleteItem.getId())
                    WordEntryTable.DICTIONARY_SECTION -> {
                        sectionListToDelete.add(deleteItem)
                        DatabaseResult.Success(Unit)
                    }
                    // Ignored types (e.g., UI, WORD_CLASS)
                    else -> DatabaseResult.Success(Unit)
                }
                if (deleteResult.isFailure) {
                    return@performTransaction deleteResult.mapErrorTo<Unit, Unit>()
                }
            }
            for (sectionItem in sectionListToDelete) {
                val result = entrySectionRepository.deleteDictionaryEntrySection(sectionItem.getId())
                if (result.isFailure) {
                    return@performTransaction result.mapErrorTo<Unit, Unit>()
                }
            }

            // Upsert dictionary entry
            val entryId = wordEntryFormData.primaryTextInput.itemProperties.getId()
            val primaryText = wordEntryFormData.primaryTextInput.inputTextValue
            val dictionaryEntryResult: DatabaseResult<Long> =
                if (wordEntryFormData.primaryTextInput.itemProperties.isNew()) {
                    entryRepository.insertDictionaryEntry(wordClassId, primaryText)
                } else {
                    entryRepository.updateDictionaryEntryPrimaryText(entryId, primaryText)
                        .flatMap { DatabaseResult.Success(entryId) }
                }
            val dictionaryEntryId = when (dictionaryEntryResult) {
                is DatabaseResult.Success -> dictionaryEntryResult.value
                else -> return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Unit>()
            }

            // Upsert dictionary entry notes
            val entryNoteItems = wordEntryFormData.entryNoteInputMap.values
            val entryNoteResult = handleUpsertTableInput(
                entryNoteItems,
                insert = { entryNoteRepository.insertDictionaryEntryNote(dictionaryEntryId, it.inputTextValue) },
                update = { entryNoteRepository.updateDictionaryEntryNote(it.itemProperties.getId(), it.inputTextValue) },
                delete = { entryNoteRepository.deleteDictionaryEntryNote(it) }
            )
            if (entryNoteResult.isFailure) {
                return@performTransaction entryNoteResult.mapErrorTo<Unit, Unit>()
            }

            val entrySectionsUpsert = wordEntryFormData.wordSectionMap
            val entrySectionResult =
                dbHandler.processBatch(entrySectionsUpsert) { entrySection ->
                    upsertWordEntrySectionFormData(dictionaryEntryId, entrySection.value)
                }
            if (entrySectionResult.isFailure) {
                return@performTransaction entrySectionResult.mapErrorTo<Unit, Unit>()
            }

            DatabaseResult.Success(Unit)
        }
    }

    private suspend fun upsertWordEntrySectionFormData(
        dictionaryEntryId: Long,
        wordSectionFormData: WordSectionFormData
    ): DatabaseResult<Unit> {

        // meaning
        val sectionId = wordSectionFormData.meaningInput.itemProperties.getId()
        val meaningText = wordSectionFormData.meaningInput.inputTextValue
        val dictionaryEntrySectionIdResult =
            if (wordSectionFormData.meaningInput.itemProperties.isNew()) {
                entrySectionRepository.insertDictionaryEntrySection(
                    dictionaryEntryId,
                    meaningText
                )
            } else {
                entrySectionRepository.updateDictionaryEntrySectionMeaning(
                    sectionId,
                    meaningText
                ).flatMap { DatabaseResult.Success(sectionId) }
            }
        val dictionaryEntrySectionId = when (dictionaryEntrySectionIdResult) {
            is DatabaseResult.Success -> dictionaryEntrySectionIdResult.value
            else -> return dictionaryEntrySectionIdResult.mapErrorTo<Long, Unit>()
        }

        // kanas
        val kanaItems = wordSectionFormData.kanaInputMap.values
        val kanaResult = handleUpsertTableInput(
            kanaItems,
            insert = { entrySectionKanaRepository.insertDictionaryEntrySectionKana(dictionaryEntrySectionId, it.inputTextValue) },
            update = { entrySectionKanaRepository.updateDictionaryEntrySectionKana(it.itemProperties.getId(), it.inputTextValue) },
            delete = { entrySectionKanaRepository.deleteDictionaryEntrySectionKana(it) }
        )
        if (kanaResult.isFailure) {
            return kanaResult.mapErrorTo<Unit, Unit>()
        }

        // section notes
        val sectionNoteItems = wordSectionFormData.sectionNoteInputMap.values
        val sectionNoteResult = handleUpsertTableInput(
            sectionNoteItems,
            insert = { entrySectionNoteRepository.insertDictionaryEntrySectionNote(dictionaryEntrySectionId, it.inputTextValue) },
            update = { entrySectionNoteRepository.updateDictionaryEntrySectionNote(it.itemProperties.getId(), it.inputTextValue) },
            delete = { entrySectionNoteRepository.deleteDictionaryEntrySectionNote(it) }
        )
        if (sectionNoteResult.isFailure) {
            return sectionNoteResult.mapErrorTo<Unit, Unit>()
        }

        return DatabaseResult.Success(Unit)
    }

    private suspend fun handleUpsertTableInput(
        items: Collection<InputTextItem>,
        insert: suspend (InputTextItem) -> DatabaseResult<Long>,
        update: suspend (InputTextItem) -> DatabaseResult<Unit>,
        delete: suspend (Long) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        // First pass: handle delete and update
        val firstPassResult = dbHandler.processBatch(items) { item ->
            val id = item.itemProperties.getId()
            val text = item.inputTextValue
            when {
                !item.itemProperties.isNew() && text.isBlank() -> delete(id) // Existing + blank → delete
                !item.itemProperties.isNew() -> update(item) // Existing + non-blank → update
                else -> DatabaseResult.Success(Unit) // new item: skip for now
            }
        }
        if (firstPassResult.isFailure) return firstPassResult.mapErrorTo<Unit, Unit>()

        // Second pass: insert new items
        val secondPassResult = dbHandler.processBatch(items) { item ->
            val text = item.inputTextValue
            if (item.itemProperties.isNew() && text.isNotBlank()) {
                insert(item).flatMap { DatabaseResult.Success(Unit) } // New + non-blank → insert
            } else {
                DatabaseResult.Success(Unit) // New + blank → ignore
            }
        }
        return secondPassResult
    }

    // checks if it is a new Id that does not exists in table
    private fun GenericItemProperties.isNew(): Boolean = WordEntryTable.fromValue(this.getTableId()) == WordEntryTable.UI
}