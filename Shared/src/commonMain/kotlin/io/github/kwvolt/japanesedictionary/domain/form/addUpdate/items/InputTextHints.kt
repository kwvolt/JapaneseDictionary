package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

object InputTextHints {
    fun forType(type: InputTextType): String = when (type) {
        InputTextType.PRIMARY_TEXT -> "Enter word"
        InputTextType.MEANING -> "Enter meaning"
        InputTextType.KANA -> "Enter hiragana/katakana spelling"
        InputTextType.ENTRY_NOTE_DESCRIPTION -> "Enter additional information on the specific details"
        InputTextType.SECTION_NOTE_DESCRIPTION -> "Enter additional information on the general details"
    }
}