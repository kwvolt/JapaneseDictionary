package io.github.kwvolt.japanesedictionary.ui.upsert

sealed class FormKeys {
    abstract val key: String

    // Static keys
    data object WordClassLabel : FormKeys() {
        override val key = "word_class_label"
    }

    data object PrimaryTextLabel : FormKeys() {
        override val key = "primary_text_label"
    }

    data object DictionaryNoteLabel : FormKeys() {
        override val key = "entry_description_label"
    }

    data object DictionaryNoteAddButton : FormKeys() {
        override val key = "entry_description_add_button"
    }

    data object SectionAddButton : FormKeys() {
        override val key = "section_add_button"
    }

    // Dynamic keys
    data class KanaLabel(val sectionId: Int) : FormKeys() {
        override val key = "kana_${sectionId}_label"
    }

    data class KanaAddButton(val sectionId: Int) : FormKeys() {
        override val key = "kana_${sectionId}_add_button"
    }

    data class MeaningLabel(val sectionId: Int) : FormKeys() {
        override val key = "meaning_${sectionId}_label"
    }

    data class SectionNoteLabel(val sectionId: Int) : FormKeys() {
        override val key = "section_description_${sectionId}_label"
    }

    data class SectionNoteAddButton(val sectionId: Int) : FormKeys() {
        override val key = "section_description_${sectionId}_add_button"
    }

    data class SectionLabel(val sectionId: Int) : FormKeys() {
        override val key = "section_${sectionId}_label"
    }
}