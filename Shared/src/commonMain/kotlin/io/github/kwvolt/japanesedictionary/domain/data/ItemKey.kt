package io.github.kwvolt.japanesedictionary.domain.data

sealed class ItemKey {
    data class DataItem(val id: String) : ItemKey()
    data class FormItem(val key: String) : ItemKey()
}