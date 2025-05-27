package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class WordEntryFormData(
    val wordClassInput: WordClassItem,
    val primaryTextInput: InputTextItem,
    val entryNoteInputMap: PersistentMap<String, InputTextItem>,
    val wordSectionMap: PersistentMap<Int, WordSectionFormData>
): FormData {
    fun getEntryNoteMapAsList(): List<InputTextItem> {
        return entryNoteInputMap.values.toList()
    }

    companion object {
        fun buildDefault(wordClassInput: WordClassItem, formSectionManager: FormSectionManager): WordEntryFormData{
            // primary Text
            val primaryTextInput: InputTextItem = InputTextItem(
                InputTextType.PRIMARY_TEXT,
                itemProperties = ItemProperties(WordEntryTable.DICTIONARY_ENTRY))

            // Entry Note
            val entryNoteItem: InputTextItem = InputTextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                itemProperties = ItemProperties(WordEntryTable.DICTIONARY_ENTRY_NOTE))
            val entryNoteInputMap: PersistentMap<String, InputTextItem> = persistentMapOf(
                entryNoteItem.itemProperties.getIdentifier() to entryNoteItem)

            val section: Int = formSectionManager.getEntrySectionId()

            // Meaning Text
            val meaningInput: InputTextItem = InputTextItem(
                InputTextType.MEANING,
                itemProperties = ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION, sectionId = section))

            // Entry Section Kana
            val entrySectionKanaItem: InputTextItem = InputTextItem(
                InputTextType.KANA,
                itemProperties = ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_KANA, sectionId = section))
            val entrySectionKanaInputMap: PersistentMap<String, InputTextItem> = persistentMapOf(
                entrySectionKanaItem.itemProperties.getIdentifier() to  entrySectionKanaItem)

            // Entry Section Note
            val entrySectionNoteItem: InputTextItem = InputTextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                itemProperties = ItemSectionProperties(WordEntryTable.DICTIONARY_SECTION_NOTE, sectionId = section))
            val entrySectionNoteInputMap: PersistentMap<String, InputTextItem> = persistentMapOf(
                entrySectionNoteItem.itemProperties.getIdentifier() to entrySectionNoteItem)

            // Word Section
            val wordSectionFormData = WordSectionFormData(
                meaningInput,
                entrySectionKanaInputMap,
                entrySectionNoteInputMap)
            val wordSectionMap = persistentMapOf(section to wordSectionFormData)

            return WordEntryFormData(wordClassInput, primaryTextInput, entryNoteInputMap, wordSectionMap)
        }
    }

}

data class WordSectionFormData(
    val meaningInput: InputTextItem,
    val kanaInputMap: PersistentMap<String, InputTextItem>,
    val sectionNoteInputMap: PersistentMap<String, InputTextItem>,
): FormSectionInterface {
    fun getKanaInputMapAsList(): List<InputTextItem> {
        return kanaInputMap.values.toList()
    }
    fun getComponentNoteInputMapAsList(): List<InputTextItem> {
        return sectionNoteInputMap.values.toList()
    }
}