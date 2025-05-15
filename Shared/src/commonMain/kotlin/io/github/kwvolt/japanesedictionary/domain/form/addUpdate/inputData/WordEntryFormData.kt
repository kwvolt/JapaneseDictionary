package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import kotlinx.collections.immutable.PersistentMap

data class WordEntryFormData(
    val wordClassInput: WordClassItem,
    val primaryTextInput: InputTextItem,
    val entryNoteInputMap: PersistentMap<String, InputTextItem>,
    val wordSectionMap: PersistentMap<Int, WordSectionFormData>
): FormData {
    fun getEntryNoteMapAsList(): List<BaseItem> {
        return entryNoteInputMap.values.toList()
    }
}

data class WordSectionFormData(
    val meaningInput: InputTextItem,
    val kanaInputMap: PersistentMap<String, InputTextItem>,
    val sectionNoteInputMap: PersistentMap<String, InputTextItem>,
): FormSectionInterface {
    fun getKanaInputMapAsList(): List<BaseItem> {
        return kanaInputMap.values.toList()
    }
    fun getComponentNoteInputMapAsList(): List<BaseItem> {
        return sectionNoteInputMap.values.toList()
    }
}