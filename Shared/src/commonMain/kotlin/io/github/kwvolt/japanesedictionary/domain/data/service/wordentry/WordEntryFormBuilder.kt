package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SimplifiedWordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SimplifiedWordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.collections.set

class WordEntryFormBuilder(
    private val wordEntryFormItemFetcher: WordEntryFormItemFetcher,
    private val wordEntryFormFetcher: WordEntryFormFetcher,
) {
    suspend fun buildDetailedFormData(
        dictionaryEntryId: Long,
        formItemManager: FormItemManager
    ): DatabaseResult<WordEntryFormData> = coroutineScope {
        with(wordEntryFormItemFetcher) {
            // Run DB calls concurrently
            val dictionaryEntryDeferred: Deferred<DatabaseResult<DictionaryEntryContainer>> = fetchDeferred {
                fetchDictionaryEntryContainer(dictionaryEntryId)
            }
            val entryNotesDeferred: Deferred<DatabaseResult<PersistentMap<String, TextItem>>> = fetchItemDeferred{
                fetchEntryNoteItemList(dictionaryEntryId).map {
                    persistentListToMap(it)
                }
            }
            val entrySectionsDeferred: Deferred<DatabaseResult<PersistentMap<Int, WordSectionFormData>>> =  fetchDeferred  {
                fetchEntrySections(dictionaryEntryId, formItemManager)
            }

            // Await results
            val dictionaryEntry: DictionaryEntryContainer = dictionaryEntryDeferred.await().getOrReturn<WordEntryFormData> { return@coroutineScope  it }
            val entryNoteItemMap: PersistentMap<String, TextItem> = entryNotesDeferred.await().getOrReturn<WordEntryFormData> { return@coroutineScope it }
            val entrySectionMap: PersistentMap<Int, WordSectionFormData> = entrySectionsDeferred.await().getOrReturn<WordEntryFormData> { return@coroutineScope it }

            val wordClassItem: WordClassItem =
                wordEntryFormItemFetcher.fetchEntryWordClassItem(dictionaryEntry.wordClassId).getOrReturn<WordEntryFormData> {
                    return@coroutineScope it
                }

            val primaryTextItem = TextItem(
                InputTextType.PRIMARY_TEXT,
                dictionaryEntry.primaryText,
                ItemProperties(tableId = WordEntryTable.DICTIONARY_ENTRY, id = dictionaryEntry.id)
            )

            DatabaseResult.Success(
                WordEntryFormData(
                    wordClassItem,
                    primaryTextItem,
                    entryNoteItemMap,
                    entrySectionMap
                )
            )
        }
    }

    private suspend fun fetchEntrySections(
        dictionaryEntryId: Long,
        formItemManager: FormItemManager,
    ): DatabaseResult<PersistentMap<Int, WordSectionFormData>> {
        val sectionList: List<DictionarySectionContainer> =
            wordEntryFormFetcher.fetchDictionarySectionContainerList(dictionaryEntryId).getOrReturn {
                return it
            }
        val resultMap: MutableMap<Int, WordSectionFormData> = mutableMapOf()
        for (container in sectionList) {
            val sectionId: Int = formItemManager.getThenIncrementEntrySectionId()
            val sectionData: WordSectionFormData =
                buildDetailedWordSectionFormData(
                    container.id,
                    container.meaning,
                    sectionId
                ).getOrReturn {
                    return it
                }
            resultMap[sectionId] = sectionData
        }

        return if (resultMap.isNotEmpty()) {
            DatabaseResult.Success(resultMap.toPersistentMap())
        } else {
            DatabaseResult.NotFound
        }
    }

    suspend fun buildSimplifiedWordFormData(
        userFetcher: UserFetcher,
        dictionaryEntryId: Long,
    ): DatabaseResult<SimplifiedWordEntryFormData> = coroutineScope {
        // Run DB calls concurrently
        val dictionaryEntryDeferred: Deferred<DatabaseResult<DictionaryEntryContainer>> = fetchDeferred {
            fetchDictionaryEntryContainer(dictionaryEntryId)
        }
        val bookmarkDeferred: Deferred<DatabaseResult<Boolean>> = fetchDeferred {
            userFetcher.fetchIsBookmarked(dictionaryEntryId)
        }
        val entrySectionsDeferred: Deferred<DatabaseResult<List<SimplifiedWordSectionFormData>>> =
            fetchDeferred {
                fetchSimplifiedSections(dictionaryEntryId)
            }

        // Await results
        val dictionaryEntryResult = dictionaryEntryDeferred.await()
        val bookmarkResult = bookmarkDeferred.await()
        val entrySectionsResult = entrySectionsDeferred.await()

        val dictionaryEntry: DictionaryEntryContainer = dictionaryEntryResult.getOrReturn<SimplifiedWordEntryFormData> {
            return@coroutineScope it
        }

        // fetch items
        val wordClass: WordClassItem = wordEntryFormItemFetcher.fetchEntryWordClassItem(dictionaryEntry.wordClassId).getOrReturn<SimplifiedWordEntryFormData> {
            return@coroutineScope it
        }

        val isBookmark: Boolean = bookmarkResult.getOrReturn<SimplifiedWordEntryFormData> {
            return@coroutineScope it
        }

        val entrySections: List<SimplifiedWordSectionFormData> = entrySectionsResult.getOrReturn<SimplifiedWordEntryFormData> {
            return@coroutineScope it
        }

        val primaryTextItem = TextItem(
            InputTextType.PRIMARY_TEXT,
            dictionaryEntry.primaryText,
            ItemProperties(tableId = WordEntryTable.DICTIONARY_ENTRY, id = dictionaryEntry.id)
        )

        DatabaseResult.Success(
            SimplifiedWordEntryFormData(
                dictionaryEntry.id,
                isBookmark,
                wordClass,
                primaryTextItem,
                entrySections.toPersistentList()
            )
        )
    }

    private suspend fun fetchSimplifiedSections(
        dictionaryEntryId: Long
    ): DatabaseResult<List<SimplifiedWordSectionFormData>> {
        val sectionList: List<DictionarySectionContainer> = wordEntryFormFetcher.fetchDictionarySectionContainerList(dictionaryEntryId).getOrReturn {
            return it
        }

        val resultList: MutableList<SimplifiedWordSectionFormData> = mutableListOf()

        sectionList.forEachIndexed { section: Int, container: DictionarySectionContainer ->
            val sectionData: SimplifiedWordSectionFormData =
                buildSimplifiedWordSectionFormData(
                    container.id,
                    container.meaning,
                    section
                ).getOrReturn {
                    return it
                }
            resultList.add(sectionData)
        }

        return if (resultList.isNotEmpty()) {
            DatabaseResult.Success(resultList)
        } else {
            DatabaseResult.NotFound
        }
    }

    private suspend fun buildDetailedWordSectionFormData(
        dictionaryEntrySectionId: Long,
        meaning: String,
        section: Int
    ): DatabaseResult<WordSectionFormData> = coroutineScope {
        val meaningItem = TextItem(
            InputTextType.MEANING,
            meaning,
            ItemSectionProperties(
                WordEntryTable.DICTIONARY_SECTION,
                id = dictionaryEntrySectionId,
                sectionId = section
            )
        )
        val kanaDeferred: Deferred<DatabaseResult<PersistentMap<String, TextItem>>> = fetchItemDeferred {
            fetchSectionKanaItemList(dictionaryEntrySectionId, section).map {
                persistentListToMap(it)
            }
        }
        val noteDeferred: Deferred<DatabaseResult<PersistentMap<String, TextItem>>> = fetchItemDeferred {
            fetchSectionNoteItemList(dictionaryEntrySectionId, section).map {
                persistentListToMap(it)
            }
        }

        val kanaItems: PersistentMap<String, TextItem> = kanaDeferred.await().getOrReturn<WordSectionFormData> {
            return@coroutineScope it
        }

        val noteItems: PersistentMap<String, TextItem> = noteDeferred.await().getOrReturn<WordSectionFormData> {
            return@coroutineScope it
        }

        DatabaseResult.Success(
            WordSectionFormData(
                meaningItem,
                kanaItems,
                noteItems
            )
        )
    }

    private suspend fun buildSimplifiedWordSectionFormData(
        dictionaryEntrySectionId: Long,
        meaning: String,
        section: Int
    ): DatabaseResult<SimplifiedWordSectionFormData> = coroutineScope {
        val meaningItem = TextItem(
            InputTextType.MEANING,
            meaning,
            ItemSectionProperties(
                WordEntryTable.DICTIONARY_SECTION,
                id = dictionaryEntrySectionId,
                sectionId = section
            )
        )
        val kanaDeferred: Deferred<DatabaseResult<PersistentList<TextItem>>> = fetchItemDeferred {
            fetchSectionKanaItemList(dictionaryEntrySectionId, section)
        }
        // Limit concurrent tasks
        val kanaResult: DatabaseResult<PersistentList<TextItem>> = kanaDeferred.await()
        val kanaItems: PersistentList<TextItem> = kanaResult.getOrReturn<SimplifiedWordSectionFormData> { return@coroutineScope it }

        DatabaseResult.Success(
            SimplifiedWordSectionFormData(
                meaningItem,
                kanaItems
            )
        )
    }

    private fun <T>CoroutineScope.fetchDeferred(
        block: suspend WordEntryFormFetcher.()-> DatabaseResult<T>
    ): Deferred<DatabaseResult<T>>{
        return async { block(wordEntryFormFetcher) }
    }

    private fun <T>CoroutineScope.fetchItemDeferred(
        block: suspend WordEntryFormItemFetcher.()-> DatabaseResult<T>
    ): Deferred<DatabaseResult<T>>{
        return async { block(wordEntryFormItemFetcher) }
    }
}