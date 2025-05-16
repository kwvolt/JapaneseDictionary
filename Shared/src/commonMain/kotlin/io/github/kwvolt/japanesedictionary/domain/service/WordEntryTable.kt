package io.github.kwvolt.japanesedictionary.domain.service

enum class WordEntryTable(private val value: String ): TableId {
    DICTIONARY_ENTRY(value = "DICTIONARY_ENTRY"),
    DICTIONARY_ENTRY_NOTE(value = "DICTIONARY_ENTRY_NOTE"),
    DICTIONARY_SECTION(value = "DICTIONARY_SECTION"),
    DICTIONARY_SECTION_NOTE(value = "DICTIONARY_SECTION_NOTE"),
    DICTIONARY_SECTION_KANA(value = "DICTIONARY_SECTION_KANA"),
    UI(value = "UI");

    override fun toString(): String = value

    companion object {
        fun fromValue(value: String): WordEntryTable? =
            entries.find { it.value == value }
    }

    override fun asString(): String {
        return value
    }
}

interface TableId {
    fun asString(): String
}