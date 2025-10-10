package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionKanaContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySectionNoteContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassFetcher
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemSectionProperties
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

class WordEntryFormItemFetcher(
    private val wordClassFetcher: WordClassFetcher,
    private val wordEntryFormFetcher: WordEntryFormFetcher
) {
    suspend fun fetchEntryWordClassItem(wordClassId: Long): DatabaseResult<WordClassItem> {
        return wordClassFetcher.fetchWordClassItem(wordClassId)
    }

    suspend fun fetchEntryPrimaryTextItem(dictionaryEntryId: Long): DatabaseResult<TextItem>{
        return wordEntryFormFetcher.fetchDictionaryEntryContainer(dictionaryEntryId).map { container ->
            TextItem(
            InputTextType.PRIMARY_TEXT,
            container.primaryText,
            ItemProperties(WordEntryTable.DICTIONARY_ENTRY, id = container.id)
            )
        }
    }

    suspend fun fetchEntryNoteItemList(dictionaryEntryId: Long): DatabaseResult<PersistentList<TextItem>> {
        return wordEntryFormFetcher.fetchDictionaryEntryNoteContainerList(dictionaryEntryId).mapToTextItem { container: DictionaryEntryNoteContainer ->
            TextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                container.note,
                ItemProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, id = container.id)
            )
        }
    }

    suspend fun fetchEntryNoteItem(entryNoteId: Long): DatabaseResult<TextItem> {
        return wordEntryFormFetcher.fetchDictionaryEntryNoteContainer(entryNoteId).map { container ->
            TextItem(
                InputTextType.DICTIONARY_NOTE_DESCRIPTION,
                container.note,
                ItemProperties(WordEntryTable.DICTIONARY_ENTRY_NOTE, id = container.id)
            )
        }
    }

    suspend fun fetchSectionMeaningTextItem(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<TextItem>{
        return wordEntryFormFetcher.fetchDictionarySectionContainer(dictionaryEntrySectionId).map { container ->
            TextItem(
                InputTextType.MEANING,
                container.meaning,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION, id = container.id, sectionId = section)
            )
        }
    }

    suspend fun fetchSectionKanaItemList(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentList<TextItem>> {
        return wordEntryFormFetcher.fetchDictionarySectionKanaContainerList(dictionaryEntrySectionId).mapToTextItem { container: DictionarySectionKanaContainer ->
            TextItem(
                InputTextType.KANA,
                container.wordText,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_KANA, id = container.id, sectionId = section)
            )
        }
    }

    suspend fun fetchSectionKanaItem(kanaId: Long, section: Int): DatabaseResult<TextItem> {
        return wordEntryFormFetcher.fetchDictionarySectionKanaContainer(kanaId).map { container ->
            TextItem(
                InputTextType.KANA,
                container.wordText,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_KANA, id = container.id, sectionId = section)
            )
        }
    }

    suspend fun fetchSectionNoteItemList(dictionaryEntrySectionId: Long, section: Int): DatabaseResult<PersistentList<TextItem>> {
        return wordEntryFormFetcher.fetchDictionarySectionNoteContainerList(dictionaryEntrySectionId).mapToTextItem{ container: DictionarySectionNoteContainer ->
            TextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                container.note,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, id = container.id, sectionId = section)
            )
        }
    }

    suspend fun fetchSectionNoteItem(entryNoteId: Long, section: Int): DatabaseResult<TextItem> {
        return wordEntryFormFetcher.fetchDictionarySectionNoteContainer(entryNoteId).map { container ->
            TextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                container.note,
                ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, id = container.id, sectionId = section)
            )
        }
    }

    private fun <T> DatabaseResult<List<T>>.mapToTextItem(block: (T) -> TextItem): DatabaseResult<PersistentList<TextItem>> {
        return this.map { items ->
            items.map {
                block(it)
            }.toPersistentList()
        }
    }

    fun persistentListToMap(items: PersistentList<TextItem>): PersistentMap<String, TextItem>{
        return items.associateBy { item -> item.itemProperties.getIdentifier() }.toPersistentMap()
    }
}