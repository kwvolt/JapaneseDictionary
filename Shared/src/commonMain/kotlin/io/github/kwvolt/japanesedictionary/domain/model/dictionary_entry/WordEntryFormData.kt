package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ConjugationTemplateItem
import io.github.kwvolt.japanesedictionary.util.CommonParcelable
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.util.CommonParcelize
import io.github.kwvolt.japanesedictionary.util.CommonRawValue
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

@CommonParcelize
data class WordEntryFormData(
    val wordClassInput: @CommonRawValue WordClassItem,
    val primaryTextInput: @CommonRawValue  TextItem,
    val noteInputMap: @CommonRawValue PersistentMap<String, TextItem>,
    val wordSectionMap: @CommonRawValue PersistentMap<Int, WordSectionFormData>,
    val conjugationTemplateInput: @CommonRawValue ConjugationTemplateItem
): CommonParcelable {
    fun getNoteInputMapAsList(): List<TextItem> {
        return noteInputMap.values.toList()
    }
    companion object {
        fun buildDefault(formItemManager: FormItemManager): WordEntryFormData {
            with(formItemManager){
                // primary Text Item
                val primaryTextInput: TextItem = createNewTextItem(InputTextType.PRIMARY_TEXT, genericItemProperties = createItemProperties())

                // Word Class Item
                val wordClassItem: WordClassItem = createNewWordClassItem(genericItemProperties=createItemProperties())

                // Entry Note Item
                val entryNoteItem: TextItem = createNewTextItem(InputTextType.DICTIONARY_NOTE_DESCRIPTION, genericItemProperties = createItemProperties())
                val entryNoteInputMap: PersistentMap<String, TextItem> = persistentMapOf(
                    entryNoteItem.itemProperties.getIdentifier() to entryNoteItem)

                // Section
                val (section: Int, wordSectionFormData: WordSectionFormData) = WordSectionFormData.buildDefault(formItemManager)
                val wordSectionMap = persistentMapOf(section to wordSectionFormData)

                // Conjugation Template Item
                val conjugationTemplateItem =  createNewConjugationTemplateItem(genericItemProperties = createItemProperties())

                return WordEntryFormData(
                    wordClassItem,
                    primaryTextInput,
                    entryNoteInputMap,
                    wordSectionMap,
                    conjugationTemplateItem
                )
            }
        }
    }
}

@CommonParcelize
data class WordSectionFormData(
    val meaningInput: @CommonRawValue TextItem,
    val kanaInputMap: @CommonRawValue  PersistentMap<String, TextItem>,
    val noteInputMap: @CommonRawValue PersistentMap<String, TextItem>,
): CommonParcelable {
    fun getKanaInputMapAsList(): List<TextItem> {
        return kanaInputMap.values.toList()
    }
    fun getNoteInputMapAsList(): List<TextItem> {
        return noteInputMap.values.toList()
    }

    companion object {
        fun buildDefault(formItemManager: FormItemManager): WordSectionFormReturn {
            val section: Int = formItemManager.getThenIncrementEntrySectionId()
            return WordSectionFormReturn(section, buildDefault(section, formItemManager))
        }

        fun buildDefault(section: Int, formItemManager: FormItemManager): WordSectionFormData {
            with(formItemManager){
                // Meaning Text
                val meaningInput: TextItem = createNewTextItem(InputTextType.MEANING, genericItemProperties = createItemSectionProperties(sectionId = section))

                // Section Kana
                val kanaItem: TextItem = createNewTextItem(InputTextType.KANA,genericItemProperties = createItemSectionProperties(sectionId = section))
                val kanaInputMap: PersistentMap<String, TextItem> = persistentMapOf(kanaItem.itemProperties.getIdentifier() to  kanaItem)

                // Section Note
                val noteItem: TextItem = createNewTextItem(InputTextType.SECTION_NOTE_DESCRIPTION,genericItemProperties = createItemSectionProperties(sectionId = section))
                val noteInputMap: PersistentMap<String, TextItem> = persistentMapOf(noteItem.itemProperties.getIdentifier() to noteItem)

                // Word Section
                return WordSectionFormData(
                    meaningInput,
                    kanaInputMap,
                    noteInputMap
                )
            }
        }
    }
}
@CommonParcelize
data class WordSectionFormReturn(val sectionId: Int, val wordSectionFormData: WordSectionFormData): CommonParcelable