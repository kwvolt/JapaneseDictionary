package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySectionContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.SimplifiedWordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.SimplifiedWordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
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
            val dictionaryEntry: DictionaryEntryContainer = dictionaryEntryDeferred.await().getOrReturn {
                return@coroutineScope  it.mapErrorTo()
            }
            val entryNoteItemMap: PersistentMap<String, TextItem> = entryNotesDeferred.await().getOrReturn {
                return@coroutineScope it.mapErrorTo()
            }
            val entrySectionMap: PersistentMap<Int, WordSectionFormData> = entrySectionsDeferred.await().getOrReturn {
                return@coroutineScope it.mapErrorTo()
            }

            val wordClassItem: WordClassItem = wordEntryFormItemFetcher.fetchEntryWordClassItem(dictionaryEntry.wordClassId).getOrReturn {
                return@coroutineScope it.mapErrorTo()
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
                return it.mapErrorTo()
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
                    return it.mapErrorTo()
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

        val dictionaryEntry: DictionaryEntryContainer = dictionaryEntryResult.getOrReturn {
            return@coroutineScope it.mapErrorTo()
        }

        // fetch items
        val wordClass: WordClassItem = wordEntryFormItemFetcher.fetchEntryWordClassItem(dictionaryEntry.wordClassId).getOrReturn {
            return@coroutineScope it.mapErrorTo()
        }

        val isBookmark: Boolean = bookmarkResult.getOrReturn {
            return@coroutineScope bookmarkResult.mapErrorTo()
        }

        val entrySections: List<SimplifiedWordSectionFormData> = entrySectionsResult.getOrReturn {
            return@coroutineScope entrySectionsResult.mapErrorTo()
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
            return it.mapErrorTo()
        }

        val resultList: MutableList<SimplifiedWordSectionFormData> = mutableListOf()

        sectionList.forEachIndexed { section: Int, container: DictionarySectionContainer ->
            val sectionData: SimplifiedWordSectionFormData =
                buildSimplifiedWordSectionFormData(
                    container.id,
                    container.meaning,
                    section
                ).getOrReturn {
                    return it.mapErrorTo()
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

        val kanaItems: PersistentMap<String, TextItem> = kanaDeferred.await().getOrReturn {
            return@coroutineScope it.mapErrorTo()
        }

        val noteItems: PersistentMap<String, TextItem> = noteDeferred.await().getOrReturn {
            return@coroutineScope it.mapErrorTo()
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
        val kanaItems: PersistentList<TextItem> = kanaResult.getOrReturn {
            return@coroutineScope it.mapErrorTo()
        }

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

    private inline fun <T> DatabaseResult<T>.getOrReturn(
        errorTo : (DatabaseResult<T>) -> T
    ) : T{
        return when (this) {
            is DatabaseResult.Success -> value
            else -> errorTo(this)
        }
    }
}