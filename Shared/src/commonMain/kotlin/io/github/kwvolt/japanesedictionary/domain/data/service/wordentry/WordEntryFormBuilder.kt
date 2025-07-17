package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntrySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionKanaInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntrySectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassIdContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
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
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface
) {

    private val semaphore = Semaphore(5) // Allow only 5 concurrent tasks at a time

    suspend fun createWordFormData(
        dictionaryEntryId: Long,
        formItemManager: FormItemManager
    ): DatabaseResult<WordEntryFormData> = coroutineScope {

        // Run DB calls concurrently
        val dictionaryEntryDeferred = async { withDbTimeout { semaphore.withPermit { entryRepository.selectRow(dictionaryEntryId) }} }
        val entryNotesDeferred = async { withDbTimeout { semaphore.withPermit { fetchEntryNotes(dictionaryEntryId) }} }
        val entrySectionsDeferred = async { withDbTimeout { semaphore.withPermit { fetchEntrySections(dictionaryEntryId, formItemManager) }}}

        // Await results
        val dictionaryEntryResult = dictionaryEntryDeferred.await()
        val entryNotesResult = entryNotesDeferred.await()
        val entrySectionsResult = entrySectionsDeferred.await()

        val dictionaryEntry: DictionaryEntryContainer = when (dictionaryEntryResult) {
            is DatabaseResult.Success -> dictionaryEntryResult.value
            else -> return@coroutineScope dictionaryEntryResult.mapErrorTo<DictionaryEntryContainer, WordEntryFormData>()
        }

        val entryNotes: PersistentMap<String, TextItem> = when (entryNotesResult) {
            is DatabaseResult.Success -> entryNotesResult.value
            else -> return@coroutineScope entryNotesResult.mapErrorTo<PersistentMap<String, TextItem>, WordEntryFormData>()
        }

        val entrySections: PersistentMap<Int, WordSectionFormData> = when (entrySectionsResult) {
            is DatabaseResult.Success -> entrySectionsResult.value
            else -> return@coroutineScope entrySectionsResult.mapErrorTo<PersistentMap<Int, WordSectionFormData>, WordEntryFormData>()
        }

        val wordClass: WordClassItem = when (val wordClassResult = fetchWordClass(dictionaryEntry.wordClassId)) {
            is DatabaseResult.Success -> wordClassResult.value
            else -> return@coroutineScope wordClassResult.mapErrorTo<WordClassItem, WordEntryFormData>()
        }

        buildWordEntryFormData(dictionaryEntry, wordClass, entryNotes, entrySections)
    }

    private suspend fun fetchEntrySections(
        dictionaryEntryId: Long,
        formItemManager: FormItemManager,
    ): DatabaseResult<PersistentMap<Int, WordSectionFormData>> {
        val sectionListResult: DatabaseResult<List<DictionaryEntrySectionContainer>> = entrySectionRepository.selectAllByEntryId(dictionaryEntryId)

        val sectionList: List<DictionaryEntrySectionContainer> = when (sectionListResult) {
            is DatabaseResult.Success -> sectionListResult.value
            else -> return sectionListResult.mapErrorTo<List<DictionaryEntrySectionContainer>,PersistentMap<Int, WordSectionFormData>>()
        }

        val resultMap: MutableMap<Int, WordSectionFormData> = mutableMapOf()

        for (container in sectionList) {
            val section = formItemManager.getThenIncrementEntrySectionId()
            val sectionDataResult: DatabaseResult<WordSectionFormData> = createWordSectionFormData(container.id, container.meaning, section)

            val sectionData: WordSectionFormData = when (sectionDataResult) {
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

    private fun buildWordEntryFormData(
        dictionaryEntryResult: DictionaryEntryContainer,
        wordClassItem: WordClassItem,
        entryNoteMap: PersistentMap<String, TextItem>,
        entrySectionMap: PersistentMap<Int, WordSectionFormData>
    ): DatabaseResult<WordEntryFormData> {
        val primaryTextItem = TextItem(
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
        val meaningItem = TextItem(
            InputTextType.MEANING,
            meaning,
            ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION, id = dictionaryEntrySectionId, sectionId = section)
        )

        val kanaDeferred:  Deferred<DatabaseResult<PersistentMap<String, TextItem>>> = async {
            withTimeout(5000) { // 5-second timeout for kana fetch
                semaphore.withPermit {
                    fetchEntrySectionKana(dictionaryEntrySectionId, section)
                }
            }
        }
        val noteDeferred:  Deferred<DatabaseResult<PersistentMap<String, TextItem>>>  = async {
            withTimeout(5000) {semaphore.withPermit { fetchEntrySectionNotes(dictionaryEntrySectionId, section) }}
        }

        // Limit concurrent tasks
        val kanaResult: DatabaseResult<PersistentMap<String, TextItem>> = kanaDeferred.await()
        val noteResult: DatabaseResult<PersistentMap<String, TextItem>> = noteDeferred.await()

        val kanaItems: PersistentMap<String, TextItem> = when (kanaResult) {
            is DatabaseResult.Success -> kanaResult.value
            else -> return@coroutineScope kanaResult.mapErrorTo<PersistentMap<String, TextItem>, WordSectionFormData>()
        }

        val noteItems: PersistentMap<String, TextItem> = when (noteResult) {
            is DatabaseResult.Success -> noteResult.value
            else -> return@coroutineScope noteResult.mapErrorTo<PersistentMap<String, TextItem>, WordSectionFormData>()
        }

        DatabaseResult.Success(
            WordSectionFormData(
                meaningItem,
                kanaItems,
                noteItems
            )
        )
    }

    suspend fun fetchWordClass(wordClassId: Long): DatabaseResult<WordClassItem> {
        return wordClassRepository.selectRow(wordClassId).flatMap { wordClassResult: WordClassIdContainer ->
            mainClassRepository.selectRowById(wordClassResult.mainClassId).flatMap { mainClassContainer: MainClassContainer ->
                subClassRepository.selectRowById(wordClassResult.subClassId).map { subClassContainer: SubClassContainer ->
                    WordClassItem(
                        mainClassContainer,
                        subClassContainer,
                        ItemProperties(tableId = WordEntryTable.WORD_CLASS, id = wordClassResult.wordClassId)
                    )
                }
            }
        }
    }

    suspend fun fetchPrimaryText(dictionaryEntryId: Long): DatabaseResult<TextItem>{
        return entryRepository.selectRow(dictionaryEntryId).map { container ->
            TextItem(
            InputTextType.PRIMARY_TEXT,
            container.primaryText,
            ItemProperties(WordEntryTable.DICTIONARY_ENTRY, id = container.id))
        }
    }

    suspend fun fetchEntryNotes(dictionaryEntryId: Long): DatabaseResult<PersistentMap<String, TextItem>> {
        return fetchItemsAndMap(
            { entryNoteRepository.selectAllById(dictionaryEntryId) }
        ) { container ->
            TextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                container.note,
                ItemProperties(WordEntryTable.DICTIONARY_ENTRY_NOTE, id = container.id)
            )
        }
    }

    suspend fun fetchMeaningText(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<TextItem>{
        return entrySectionRepository.selectRow(dictionaryEntrySectionId).map { container ->
            TextItem(
                InputTextType.MEANING,
                container.meaning,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION, id = container.id, sectionId = section))
        }
    }

    suspend fun fetchEntrySectionKana(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentMap<String, TextItem>> {
        return fetchItemsAndMap(
            { entrySectionKanaRepository.selectAllBySectionId(dictionaryEntrySectionId) }
        ) { container: DictionaryEntrySectionKanaContainer ->
            TextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                container.wordText,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_KANA, id = container.id, sectionId = section)
            )
        }
    }

    suspend fun fetchEntrySectionNotes(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentMap<String, TextItem>> {
        return fetchItemsAndMap(
            { entrySectionNoteRepository.selectAllBySectionId(dictionaryEntrySectionId) }
        ) { container: DictionaryEntrySectionNoteContainer ->
            TextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                container.note,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, id = container.id, sectionId = section)
            )
        }
    }

    private suspend fun <T> withDbTimeout(block: suspend () -> T): T {
        return withTimeout(5000) { block() }
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