package io.github.kwvolt.japanesedictionary.domain.model

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class WordEntryFormData(
    val wordClassInput: WordClassItem,
    val primaryTextInput: TextItem,
    val entryNoteInputMap: PersistentMap<String, TextItem>,
    val wordSectionMap: PersistentMap<Int, WordSectionFormData>
) {
    fun getEntryNoteMapAsList(): List<TextItem> {
        return entryNoteInputMap.values.toList()
    }
    companion object {
        fun buildDefault(formItemManager: FormItemManager): WordEntryFormData {
            // primary Text
            val primaryTextInput: TextItem = formItemManager.createNewTextItem(InputTextType.PRIMARY_TEXT, formItemManager.createItemProperties())

            // Word Class Item
            val wordClassItem: WordClassItem = formItemManager.createNewWordClassItem(genericItemProperties= formItemManager.createItemProperties())

            // Entry Note
            val entryNoteItem: TextItem = formItemManager.createNewTextItem(InputTextType.ENTRY_NOTE_DESCRIPTION, formItemManager.createItemProperties())
            val entryNoteInputMap: PersistentMap<String, TextItem> = persistentMapOf(
                entryNoteItem.itemProperties.getIdentifier() to entryNoteItem)


            val (section: Int, wordSectionFormData: WordSectionFormData) = WordSectionFormData.buildDefault(
                formItemManager
            )
            val wordSectionMap = persistentMapOf(section to wordSectionFormData)

            return WordEntryFormData(wordClassItem, primaryTextInput, entryNoteInputMap, wordSectionMap)
        }
    }
}

data class WordSectionFormData(
    val meaningInput: TextItem,
    val kanaInputMap: PersistentMap<String, TextItem>,
    val sectionNoteInputMap: PersistentMap<String, TextItem>,
) {
    fun getKanaInputMapAsList(): List<TextItem> {
        return kanaInputMap.values.toList()
    }
    fun getComponentNoteInputMapAsList(): List<TextItem> {
        return sectionNoteInputMap.values.toList()
    }

    companion object {
        fun buildDefault(formItemManager: FormItemManager): WordSectionFormReturn {
            val section: Int = formItemManager.getThenIncrementEntrySectionId()
            return WordSectionFormReturn(section, buildDefault(section, formItemManager))
        }

        fun buildDefault(section: Int, formItemManager: FormItemManager): WordSectionFormData {
            // Meaning Text
            val meaningInput: TextItem = formItemManager.createNewTextItem(InputTextType.MEANING, formItemManager.createItemSectionProperties(sectionId = section))

            // Entry Section Kana
            val entrySectionKanaItem: TextItem = formItemManager.createNewTextItem(InputTextType.KANA, formItemManager.createItemSectionProperties(sectionId = section))
            val entrySectionKanaInputMap: PersistentMap<String, TextItem> = persistentMapOf(
                entrySectionKanaItem.itemProperties.getIdentifier() to  entrySectionKanaItem)

            // Entry Section Note
            val entrySectionNoteItem: TextItem = formItemManager.createNewTextItem(InputTextType.SECTION_NOTE_DESCRIPTION, formItemManager.createItemSectionProperties(sectionId = section))
            val entrySectionNoteInputMap: PersistentMap<String, TextItem> = persistentMapOf(
                entrySectionNoteItem.itemProperties.getIdentifier() to entrySectionNoteItem)

            // Word Section
            return WordSectionFormData(
                meaningInput,
                entrySectionKanaInputMap,
                entrySectionNoteInputMap)
        }
    }
}

data class WordSectionFormReturn(val sectionId: Int, val wordSectionFormData: WordSectionFormData)