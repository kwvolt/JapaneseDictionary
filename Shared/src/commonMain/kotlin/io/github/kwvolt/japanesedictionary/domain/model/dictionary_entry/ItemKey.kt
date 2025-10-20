package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry

sealed class ItemKey {
    data class DataItem(val id: String) : ItemKey()
    data class FormItem(val key: String) : ItemKey()
}