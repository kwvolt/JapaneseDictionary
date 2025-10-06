package io.github.kwvolt.japanesedictionary.domain.model.items

enum class WordEntryTable(private val value: String ): TableId {
    DICTIONARY_ENTRY(value = "DICTIONARY_ENTRY"),
    DICTIONARY_ENTRY_NOTE(value = "DICTIONARY_ENTRY_NOTE"),
    DICTIONARY_SECTION(value = "DICTIONARY_SECTION"),
    DICTIONARY_SECTION_NOTE(value = "DICTIONARY_SECTION_NOTE"),
    DICTIONARY_SECTION_KANA(value = "DICTIONARY_SECTION_KANA"),
    WORD_CLASS(value = "WORD_CLASS"),
    UI(value = "UI");

    override fun toString(): String = value

    override fun asString(): String {
        return value
    }

    companion object {
        fun fromValue(value: String): WordEntryTable? {
            return entries.find { it.asString().equals(value, ignoreCase = true) }
        }
    }
}

interface TableId {
    fun asString(): String
}