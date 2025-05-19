package io.github.kwvolt.japanesedictionary.domain.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.DictionaryEntrySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.DictionaryEntrySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.DictionaryEntrySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassIdContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormSectionManager
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.withPermit

class WordEntryFormBuilder(
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: EntryRepositoryInterface,
    private val entryNoteRepository: EntryNoteRepositoryInterface,
    private val entrySectionRepository: EntrySectionRepositoryInterface,
    private val entrySectionKanaRepository: EntrySectionKanaInterface,
    private val entrySectionNoteRepository: EntrySectionNoteRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface
) {

    private val semaphore = Semaphore(5) // Allow only 5 concurrent tasks at a time

    suspend fun createWordFormData(
        dictionaryEntryId: Long,
        formSectionManager: FormSectionManager
    ): DatabaseResult<WordEntryFormData> = coroutineScope {

        // Run DB calls concurrently
        val dictionaryEntryDeferred = async { withTimeout(5000) { semaphore.withPermit { entryRepository.selectDictionaryEntry(dictionaryEntryId) }} }
        val entryNotesDeferred = async { withTimeout(5000) { semaphore.withPermit { fetchEntryNotes(dictionaryEntryId) }} }
        val entrySectionsDeferred = async { withTimeout(5000) { semaphore.withPermit { fetchEntrySections(dictionaryEntryId, formSectionManager) }}}

        // Await results
        val dictionaryEntryResult = dictionaryEntryDeferred.await()
        val entryNotesResult = entryNotesDeferred.await()
        val entrySectionsResult = entrySectionsDeferred.await()

        val dictionaryEntry: DictionaryEntryContainer = when (dictionaryEntryResult) {
            is DatabaseResult.Success -> dictionaryEntryResult.value
            else -> return@coroutineScope dictionaryEntryResult.mapErrorTo<DictionaryEntryContainer,WordEntryFormData>()
        }

        val entryNotes: PersistentMap<String, InputTextItem> = when (entryNotesResult) {
            is DatabaseResult.Success -> entryNotesResult.value
            else -> return@coroutineScope entryNotesResult.mapErrorTo<PersistentMap<String, InputTextItem>,WordEntryFormData>()
        }

        val entrySections: PersistentMap<Int, WordSectionFormData> = when (entrySectionsResult) {
            is DatabaseResult.Success -> entrySectionsResult.value
            else -> return@coroutineScope entrySectionsResult.mapErrorTo<PersistentMap<Int, WordSectionFormData>,WordEntryFormData>()
        }

        val wordClass: WordClassItem = when (val wordClassResult = fetchWordClass(dictionaryEntry.wordClassId)) {
            is DatabaseResult.Success -> wordClassResult.value
            else -> return@coroutineScope wordClassResult.mapErrorTo<WordClassItem,WordEntryFormData>()
        }

        buildWordEntryFormData(dictionaryEntry, wordClass, entryNotes, entrySections)
    }

    private suspend fun fetchEntryNotes(dictionaryEntryId: Long): DatabaseResult<PersistentMap<String, InputTextItem>> {
        return fetchItemsAndMap(
            { entryNoteRepository.selectAllDictionaryEntryNoteByDictionaryEntryId(dictionaryEntryId) }
        ) { container ->
            InputTextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                container.note,
                ItemProperties(WordEntryTable.DICTIONARY_ENTRY_NOTE, id = container.id)
            )
        }
    }

    private suspend fun fetchEntrySections(
        dictionaryEntryId: Long,
        formSectionManager: FormSectionManager,
    ): DatabaseResult<PersistentMap<Int, WordSectionFormData>> {
        val sectionListResult: DatabaseResult<List<DictionaryEntrySectionContainer>> = entrySectionRepository.selectAllDictionaryEntrySectionByEntry(dictionaryEntryId)

        val sectionList: List<DictionaryEntrySectionContainer> = when (sectionListResult) {
            is DatabaseResult.Success -> sectionListResult.value
            else -> return sectionListResult.mapErrorTo<List<DictionaryEntrySectionContainer>,PersistentMap<Int, WordSectionFormData>>()
        }

        val resultMap: MutableMap<Int, WordSectionFormData> = mutableMapOf()

        for (container in sectionList) {
            val section = formSectionManager.getThenIncrementEntrySectionId()
            val sectionDataResult: DatabaseResult<WordSectionFormData> = createWordSectionFormData(container.id, container.meaning, section)

            val sectionData: WordSectionFormData  = when (sectionDataResult) {
                is DatabaseResult.Success -> sectionDataResult.value
                else -> return sectionDataResult.mapErrorTo<WordSectionFormData, PersistentMap<Int, WordSectionFormData>>()
            }
            resultMap[section] = sectionData
        }

        return if (resultMap.isNotEmpty()) {
            DatabaseResult.Success(resultMap.toPersistentMap())
        } else {
            DatabaseResult.NotFound
        }
    }

    private suspend fun fetchWordClass(wordClassId: Long): DatabaseResult<WordClassItem> {
        return wordClassRepository.selectWordClassMainClassIdAndSubClassIdByWordClassId(wordClassId).flatMap { wordClassResult: WordClassIdContainer ->
            subClassRepository.selectAllSubClassByMainClassId(wordClassResult.mainClassId).flatMap { subClassResult: List<SubClassContainer> ->
                DatabaseResult.Success(
                    WordClassItem(
                        wordClassResult.mainClassId,
                        wordClassResult.subClassId,
                        subClassResult,
                        ItemProperties(tableId = WordEntryTable.WORD_CLASS, id = wordClassResult.wordClassId)
                    )
                )
            }
        }
    }

    private fun buildWordEntryFormData(
        dictionaryEntryResult: DictionaryEntryContainer,
        wordClassItem: WordClassItem,
        entryNoteMap: PersistentMap<String, InputTextItem>,
        entrySectionMap: PersistentMap<Int, WordSectionFormData>
    ): DatabaseResult<WordEntryFormData> {
        val primaryTextItem = InputTextItem(
            InputTextType.PRIMARY_TEXT,
            dictionaryEntryResult.primaryText,
            ItemProperties(tableId = WordEntryTable.DICTIONARY_ENTRY, id = dictionaryEntryResult.id)
        )

        return DatabaseResult.Success(
            WordEntryFormData(
                wordClassItem,
                primaryTextItem,
                entryNoteMap,
                entrySectionMap
            )
        )
    }

    private suspend fun createWordSectionFormData(
        dictionaryEntrySectionId: Long,
        meaning: String,
        section: Int
    ): DatabaseResult<WordSectionFormData> = coroutineScope {
        val meaningItem = InputTextItem(
            InputTextType.MEANING,
            meaning,
            ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION, id = dictionaryEntrySectionId, sectionId = section)
        )

        val kanaDeferred:  Deferred<DatabaseResult<PersistentMap<String, InputTextItem>>> = async {
            withTimeout(5000) { // 5-second timeout for kana fetch
                semaphore.withPermit {
                    fetchEntrySectionKana(dictionaryEntrySectionId, section)
                }
            }
        }
        val noteDeferred:  Deferred<DatabaseResult<PersistentMap<String, InputTextItem>>>  = async {
            withTimeout(5000) {semaphore.withPermit { fetchEntrySectionNotes(dictionaryEntrySectionId, section) }}
        }

        // Limit concurrent tasks
        val kanaResult: DatabaseResult<PersistentMap<String, InputTextItem>> = kanaDeferred.await()
        val noteResult: DatabaseResult<PersistentMap<String, InputTextItem>> = noteDeferred.await()

        val kanaItems: PersistentMap<String, InputTextItem> = when (kanaResult) {
            is DatabaseResult.Success -> kanaResult.value
            else -> return@coroutineScope kanaResult.mapErrorTo<PersistentMap<String, InputTextItem>, WordSectionFormData>()
        }

        val noteItems: PersistentMap<String, InputTextItem> = when (noteResult) {
            is DatabaseResult.Success -> noteResult.value
            else -> return@coroutineScope noteResult.mapErrorTo<PersistentMap<String, InputTextItem>, WordSectionFormData>()
        }

        DatabaseResult.Success(
            WordSectionFormData(
                meaningItem,
                kanaItems,
                noteItems
            )
        )
    }
    private suspend fun fetchEntrySectionKana(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentMap<String, InputTextItem>> {
        return fetchItemsAndMap(
            { entrySectionKanaRepository.selectAllKanaByDictionaryEntrySectionId(dictionaryEntrySectionId) }
        ) { container: DictionaryEntrySectionKanaContainer ->
            InputTextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                container.wordText,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_KANA, id = container.id, sectionId = section)
            )
        }
    }

    private suspend fun fetchEntrySectionNotes(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentMap<String, InputTextItem>> {
        return fetchItemsAndMap(
            { entrySectionNoteRepository.selectAllDictionaryEntrySectionNotesByDictionaryEntrySectionId(dictionaryEntrySectionId) }
        ) { container: DictionaryEntrySectionNoteContainer ->
            InputTextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                container.note,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, id = container.id, sectionId = section)
            )
        }

    }

    private suspend fun <T, R: BaseItem> fetchItemsAndMap(
        fetchFunction: suspend () -> DatabaseResult<List<T>>,
        mapFunction: (T) -> R
    ): DatabaseResult<PersistentMap<String, R>> {
        return fetchFunction().flatMap { items ->
            DatabaseResult.Success(
                items.associate { container ->
                    val item: R = mapFunction(container)
                    item.itemProperties.getIdentifier() to item
                }.toPersistentMap()
            )
        }
    }
}