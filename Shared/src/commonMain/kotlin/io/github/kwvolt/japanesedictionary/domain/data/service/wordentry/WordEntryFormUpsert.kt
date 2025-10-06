package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryUserRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryUserRepository
import io.github.kwvolt.japanesedictionary.domain.model.items.item.BaseItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.items.item.GenericItemProperties
import kotlinx.collections.immutable.ImmutableCollection
import java.lang.IllegalArgumentException

class WordEntryFormUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: DictionaryRepositoryInterface,
    private val entryNoteRepository: DictionaryNoteRepositoryInterface,
    private val entrySectionRepository: SectionRepositoryInterface,
    private val entrySectionKanaRepository: SectionKanaRepositoryInterface,
    private val entrySectionNoteRepository: SectionNoteRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface,
    private val dictionaryUserRepository: DictionaryUserRepositoryInterface
) {

    suspend fun upsertDictionaryEntry(primaryTextItem: TextItem, wordClassId: Long): DatabaseResult<Long>{
        return upsertItem(primaryTextItem,
            insert = { identifier: String ->
                entryRepository.insert(wordClassId,primaryTextItem.inputTextValue, identifier)
            },
            update = { id: Long, identifier: String ->
                entryRepository.updateWordClassIdAndPrimaryText(id, wordClassId, primaryTextItem.inputTextValue, identifier)
            }
        )
    }

    suspend fun updateIsBookmark(dictionaryId: Long, isBookmark: Boolean): DatabaseResult<Unit>{
        return dictionaryUserRepository.updateIsBookmark(dictionaryId, isBookmark)

    }

    suspend fun upsertDictionaryEntryNote(dictionaryId: Long, entryNoteItem: TextItem): DatabaseResult<Long>{
        return upsertItem(entryNoteItem,
            insert = { identifier: String ->
                entryNoteRepository.insert(dictionaryId, entryNoteItem.inputTextValue, identifier)
            },
            update = { id: Long, identifier: String ->
                entryNoteRepository.updateNoteDescription(id, entryNoteItem.inputTextValue, identifier)
            }
        )
    }

    suspend fun upsertDictionarySection(dictionaryId: Long, meaningItem: TextItem): DatabaseResult<Long>{
        return upsertItem(meaningItem,
            insert = { identifier: String ->
                entrySectionRepository.insert(dictionaryId, meaningItem.inputTextValue, identifier)
            },
            update = { id: Long, identifier: String ->
                entrySectionRepository.updateMeaning(id, meaningItem.inputTextValue, identifier)
            }
        )
    }

    suspend fun upsertDictionarySectionKana(dictionaryEntryId: Long, dictionarySectionId: Long, kanaItem: TextItem): DatabaseResult<Long>{
        return upsertItem(kanaItem,
            insert = { identifier: String ->
                entrySectionKanaRepository.insert(dictionaryEntryId, dictionarySectionId, kanaItem.inputTextValue, identifier)
            },
            update = { id: Long, identifier: String ->
                entrySectionKanaRepository.updateKana(id, kanaItem.inputTextValue, identifier)
            }
        )
    }

    suspend fun upsertDictionarySectionNote(dictionarySectionId: Long, sectionNoteItem: TextItem): DatabaseResult<Long>{
        return upsertItem(sectionNoteItem,
            insert = { identifier: String ->
                entrySectionNoteRepository.insert(dictionarySectionId, sectionNoteItem.inputTextValue, identifier)
            },
            update = { id: Long, identifier: String ->
                entrySectionNoteRepository.updateNoteDescription(id, sectionNoteItem.inputTextValue, identifier)
            }
        )
    }

    private suspend fun <T : BaseItem> upsertItem(
        baseItem: T,
        insert: suspend (String) -> DatabaseResult<Long>,
        update: suspend (Long, String) -> DatabaseResult<Unit>
    ): DatabaseResult<Long> {
        val id = baseItem.itemProperties.getId()
        val identifier = baseItem.itemProperties.getIdentifier()
        return if (baseItem.itemProperties.isNew()) {
            insert(identifier)
        } else {
            update(id, identifier).map { id }
        }
    }


    suspend fun upsertWordEntryFormData(
        wordEntryFormData: WordEntryFormData,
        deleteList: List<GenericItemProperties> = mutableListOf()
    ): DatabaseResult<Long> {
        val wordClassItem = wordEntryFormData.wordClassInput
        return wordClassRepository.selectIdByMainClassIdAndSubClassId(
            wordClassItem.chosenMainClass.id,
            wordClassItem.chosenSubClass.id,
            wordClassItem.itemProperties.getIdentifier()
        ).flatMap{
            wordClassId: Long ->
            dbHandler.performTransaction {

                // delete unused items
                val deleteResult: DatabaseResult<Unit> = deleteItems(deleteList)
                if (deleteResult.isFailure) {
                    return@performTransaction deleteResult.mapErrorTo<Unit, Long>()
                }

                // Upsert dictionary entry
                val dictionaryEntryId = when (val dictionaryEntryResult = upsertDictionaryEntry(wordEntryFormData.primaryTextInput, wordClassId)){
                    is DatabaseResult.Success -> dictionaryEntryResult.value
                    else -> return@performTransaction dictionaryEntryResult.mapErrorTo<Long, Long>()
                }

                // Upsert dictionary entry notes
                val entryNoteItems: ImmutableCollection<TextItem> = wordEntryFormData.entryNoteInputMap.values
                val entryNoteResult: DatabaseResult<Unit> = handleUpsertTableInput(
                    entryNoteItems,
                    insert = { entryNoteRepository.insert(dictionaryEntryId, it.inputTextValue, it.itemProperties.getIdentifier()) },
                    update = {
                        entryNoteRepository.updateNoteDescription(
                            it.itemProperties.getId(),
                            it.inputTextValue,
                            it.itemProperties.getIdentifier()
                        )
                    },
                    delete = { id, itemId -> entryNoteRepository.deleteRow(id, itemId) }
                )
                if (entryNoteResult.isFailure) {
                    return@performTransaction entryNoteResult.mapErrorTo<Unit, Long>()
                }

                val entrySectionsUpsert = wordEntryFormData.wordSectionMap
                val entrySectionResult =
                    dbHandler.processBatchWrite(entrySectionsUpsert) { entrySection ->
                        upsertWordEntrySectionFormData(dictionaryEntryId, entrySection.value)
                    }
                if (entrySectionResult.isFailure) {
                    return@performTransaction entrySectionResult.mapErrorTo<Unit, Long>()
                }

                DatabaseResult.Success(dictionaryEntryId)
            }
        }
    }

    private suspend fun upsertWordEntrySectionFormData(
        dictionaryEntryId: Long,
        wordSectionFormData: WordSectionFormData
    ): DatabaseResult<Unit> {

        // meaning
        val dictionaryEntrySectionId = when (val dictionaryEntrySectionIdResult = upsertDictionarySection(dictionaryEntryId, wordSectionFormData.meaningInput)) {
            is DatabaseResult.Success -> dictionaryEntrySectionIdResult.value
            else -> return dictionaryEntrySectionIdResult.mapErrorTo<Long, Unit>()
        }

        // kana
        val kanaItems = wordSectionFormData.kanaInputMap.values
        val kanaResult = handleUpsertTableInput(
            kanaItems,
            insert = { entrySectionKanaRepository.insert(dictionaryEntryId, dictionaryEntrySectionId, it.inputTextValue, it.itemProperties.getIdentifier()) },
            update = { entrySectionKanaRepository.updateKana(it.itemProperties.getId(), it.inputTextValue, it.itemProperties.getIdentifier()) },
            delete = { id, itemId -> entrySectionKanaRepository.deleteRow(id, itemId) }
        )
        if (kanaResult.isFailure) {
            return kanaResult.mapErrorTo<Unit, Unit>()
        }

        // section notes
        val sectionNoteItems = wordSectionFormData.sectionNoteInputMap.values
        val sectionNoteResult = handleUpsertTableInput(
            sectionNoteItems,
            insert = { entrySectionNoteRepository.insert(dictionaryEntrySectionId, it.inputTextValue, it.itemProperties.getIdentifier()) },
            update = { entrySectionNoteRepository.updateNoteDescription(it.itemProperties.getId(), it.inputTextValue, it.itemProperties.getIdentifier()) },
            delete = { id, itemId -> entrySectionNoteRepository.deleteRow(id, itemId) }
        )
        if (sectionNoteResult.isFailure) {
            return sectionNoteResult.mapErrorTo<Unit, Unit>()
        }

        return DatabaseResult.Success(Unit)
    }
    private suspend fun handleUpsertTableInput(
        items: Collection<TextItem>,
        insert: suspend (TextItem) -> DatabaseResult<Long>,
        update: suspend (TextItem) -> DatabaseResult<Unit>,
        delete: suspend (Long, String) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        // First pass: handle delete and update
        val firstPassResult = dbHandler.processBatchWrite(items) { item ->
            val id = item.itemProperties.getId()
            val text = item.inputTextValue
            val result = when {
                !item.itemProperties.isNew() && text.isBlank() -> delete(id, item.itemProperties.getIdentifier()) // Existing + blank -> delete
                !item.itemProperties.isNew() -> update(item) // Existing + non-blank -> update
                else -> DatabaseResult.Success(Unit) // new item: skip for now
            }
            // return early if failure
            if(result.isFailure){
                return@processBatchWrite result
            }

            DatabaseResult.Success(Unit)
        }
        if (firstPassResult.isFailure) return firstPassResult.mapErrorTo<Unit, Unit>()

        // Second pass: insert new items
        val secondPassResult = dbHandler.processBatchWrite(items) { item ->
            val text = item.inputTextValue
            val result = if (item.itemProperties.isNew() && text.isNotBlank()) {
                    insert(item).map { Unit } // New + non-blank -> insert
                } else {
                    DatabaseResult.Success(Unit) // New + blank -> ignore
                }

            // return early if failure
            if(result.isFailure){
                return@processBatchWrite result
            }

            DatabaseResult.Success(Unit)
        }
        return secondPassResult
    }

    // checks if it is a new Id that does not exists in table
    private fun GenericItemProperties.isNew(): Boolean = WordEntryTable.fromValue(this.getTableId()) == WordEntryTable.UI

    private suspend fun deleteItems(deleteList: List<GenericItemProperties>): DatabaseResult<Unit>{
        val sectionListToDelete: MutableList<GenericItemProperties> = mutableListOf()
        // deletes from db
        for (deleteItem in deleteList) {
            val identifier = deleteItem.getIdentifier()
            if (identifier.isBlank()) {
                return DatabaseResult.UnknownError(
                    IllegalArgumentException("Missing identifier for item: $deleteItem")
                )
            }
            val wordEntryType = WordEntryTable.fromValue(identifier)
            val deleteResult: DatabaseResult<Unit> = when (wordEntryType) {
                WordEntryTable.DICTIONARY_ENTRY_NOTE -> entryNoteRepository.deleteRow(
                    deleteItem.getId()
                )

                WordEntryTable.DICTIONARY_SECTION_NOTE -> entrySectionNoteRepository.deleteRow(
                    deleteItem.getId()
                )

                WordEntryTable.DICTIONARY_SECTION_KANA -> entrySectionKanaRepository.deleteRow(
                    deleteItem.getId()
                )

                WordEntryTable.DICTIONARY_SECTION -> {
                    sectionListToDelete.add(deleteItem)
                    DatabaseResult.Success(Unit)
                }
                // Ignored types (e.g., UI, WORD_CLASS)
                else -> DatabaseResult.Success(Unit)
            }
            if (deleteResult.isFailure) {
                return deleteResult.mapErrorTo<Unit, Unit>()
            }
        }
        for (sectionItem in sectionListToDelete) {
            val result = entrySectionRepository.deleteRow(sectionItem.getId(), sectionItem.getIdentifier())
            if (result.isFailure) {
                return result.mapErrorTo<Unit, Unit>()
            }
        }
        return DatabaseResult.Success(Unit)
    }
}