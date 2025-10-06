package io.github.kwvolt.japanesedictionary.domain.model

import io.github.kwvolt.japanesedictionary.util.CommonParcelable
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.util.CommonParcelize
import io.github.kwvolt.japanesedictionary.util.CommonRawValue
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

@CommonParcelize
data class WordEntryFormData(
    val wordClassInput: @CommonRawValue WordClassItem,
    val primaryTextInput: @CommonRawValue  TextItem,
    val entryNoteInputMap: @CommonRawValue PersistentMap<String, TextItem>,
    val wordSectionMap: @CommonRawValue PersistentMap<Int, WordSectionFormData>
): CommonParcelable {
    fun getEntryNoteMapAsList(): List<TextItem> {
        return entryNoteInputMap.values.toList()
    }
    companion object {
        fun buildDefault(formItemManager: FormItemManager): WordEntryFormData {
            // primary Text
            val primaryTextInput: TextItem = formItemManager.createNewTextItem(InputTextType.PRIMARY_TEXT, genericItemProperties = formItemManager.createItemProperties())

            // Word Class Item
            val wordClassItem: WordClassItem = formItemManager.createNewWordClassItem(genericItemProperties= formItemManager.createItemProperties())

            // Entry Note
            val entryNoteItem: TextItem = formItemManager.createNewTextItem(InputTextType.DICTIONARY_NOTE_DESCRIPTION, genericItemProperties = formItemManager.createItemProperties())
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

@CommonParcelize
data class WordSectionFormData(
    val meaningInput: @CommonRawValue TextItem,
    val kanaInputMap: @CommonRawValue  PersistentMap<String, TextItem>,
    val sectionNoteInputMap: @CommonRawValue PersistentMap<String, TextItem>,
): CommonParcelable {
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
            val meaningInput: TextItem = formItemManager.createNewTextItem(InputTextType.MEANING, genericItemProperties = formItemManager.createItemSectionProperties(sectionId = section))

            // Entry Section Kana
            val entrySectionKanaItem: TextItem = formItemManager.createNewTextItem(InputTextType.KANA,genericItemProperties =  formItemManager.createItemSectionProperties(sectionId = section))
            val entrySectionKanaInputMap: PersistentMap<String, TextItem> = persistentMapOf(
                entrySectionKanaItem.itemProperties.getIdentifier() to  entrySectionKanaItem)

            // Entry Section Note
            val entrySectionNoteItem: TextItem = formItemManager.createNewTextItem(InputTextType.SECTION_NOTE_DESCRIPTION,genericItemProperties =  formItemManager.createItemSectionProperties(sectionId = section))
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
@CommonParcelize
data class WordSectionFormReturn(val sectionId: Int, val wordSectionFormData: WordSectionFormData): CommonParcelable