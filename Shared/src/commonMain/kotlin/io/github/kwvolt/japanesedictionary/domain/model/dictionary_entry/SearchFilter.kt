package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry

import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.util.CommonParcelable
import io.github.kwvolt.japanesedictionary.util.CommonParcelize

@CommonParcelize
data class SearchFilter(
    val searchType: SearchType,
    val isBookmark: Boolean,
    val mainClassId: Long = WordClassDataManager.NO_ID,
    val subClassId: Long = WordClassDataManager.NO_ID,
): CommonParcelable

enum class SearchType(val searchText: String) {
    ALL("---------"),
    KANJI("Japanese"),
    KANA("Hiragana / Katakana"),
    MEANING("Meaning")
}