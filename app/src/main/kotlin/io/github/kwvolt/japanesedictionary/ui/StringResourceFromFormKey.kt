package io.github.kwvolt.japanesedictionary.ui

import androidx.annotation.StringRes
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys

object StringResourceFromFormKey {

    @StringRes
    fun getStringResource(formKeys: FormKeys): Int{
        return when(formKeys){
            FormKeys.DictionaryNoteAddButton -> R.string.upsert_button_vh_add_dictionary_note
            FormKeys.DictionaryNoteLabel -> R.string.upsert_label_vh_dictionary_note_label
            is FormKeys.KanaAddButton -> R.string.upsert_button_vh_add_kana
            is FormKeys.KanaLabel -> R.string.upsert_label_vh_kana_label
            is FormKeys.MeaningLabel -> R.string.upsert_label_vh_meaning_label
            FormKeys.PrimaryTextLabel -> R.string.upsert_label_vh_japanese_label
            FormKeys.SectionAddButton -> R.string.upsert_button_vh_add_section
            is FormKeys.SectionNoteAddButton -> R.string.upsert_button_vh_add_section_note
            is FormKeys.SectionNoteLabel -> R.string.upsert_label_vh_section_note_label
            is FormKeys.SectionLabel -> R.string.upsert_label_vh_section_label
            FormKeys.WordClassLabel -> R.string.upsert_label_vh_word_class_label
        }
    }

    @StringRes
    fun getLabelHint(formKeys: FormKeys): Int?{
        return when(formKeys){
            FormKeys.DictionaryNoteLabel -> null
            is FormKeys.KanaLabel -> R.string.upsert_label_vh_kana_hint
            is FormKeys.MeaningLabel -> R.string.upsert_label_vh_meaning_hint
            FormKeys.PrimaryTextLabel -> R.string.upsert_label_vh_japanese_hint
            is FormKeys.SectionNoteLabel -> null
            FormKeys.WordClassLabel -> null
            else -> null
        }
    }


}