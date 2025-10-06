package io.github.kwvolt.japanesedictionary.domain.model

import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import kotlinx.collections.immutable.PersistentList

data class SimplifiedWordEntryFormData(
    val dictionaryId: Long,
    val isBookmark: Boolean,
    val wordClassInput: WordClassItem,
    val primaryTextInput: TextItem,
    val wordSectionList: PersistentList<SimplifiedWordSectionFormData>,
    val isExpanded: Boolean = false
)

data class SimplifiedWordSectionFormData(
    val meaningInput: TextItem,
    val kanaInputList: PersistentList<TextItem>,
)