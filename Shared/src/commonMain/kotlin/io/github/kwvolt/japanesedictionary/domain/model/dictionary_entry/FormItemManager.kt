package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.TableId
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.WordEntryTable
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ConjugationTemplateItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemSectionProperties

class FormItemManager {
    private var sectionId: Int = 0

    private var nextAvailableId: Long = 0

    fun generateNewItemId(): Long {
        return nextAvailableId++
    }

    fun getThenIncrementEntrySectionId(): Int{
        val value: Int = sectionId
        sectionId += 1
        return value
    }

    fun clear(){
        sectionId = 0
        nextAvailableId = 0
    }

    fun createNewTextItem(
        inputTextType: InputTextType,
        inputTextValue: String = "",
        genericItemProperties: GenericItemProperties
    ): TextItem {
        return TextItem(inputTextType, inputTextValue, genericItemProperties)
    }

    fun createNewWordClassItem(
        choseMainClass: MainClassContainer = WordClassDataManager.DEFAULT_MAIN,
        chosenSubClass: SubClassContainer = WordClassDataManager.DEFAULT_SUB,
        genericItemProperties: GenericItemProperties
    ): WordClassItem {
        return WordClassItem(choseMainClass, chosenSubClass, genericItemProperties)
    }


    fun createNewConjugationTemplateItem(
        chosenConjugationTemplateId: Long = -1 ,
        kana: String? = null,
        genericItemProperties: GenericItemProperties
    ): ConjugationTemplateItem {
        TODO("")
        return ConjugationTemplateItem(chosenConjugationTemplateId, kana, genericItemProperties)
    }

    fun createStaticLabelItem(
        key: FormKeys,
        labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
        genericItemProperties: GenericItemProperties
    ): StaticLabelItem {
        return StaticLabelItem(key, labelHeaderType, genericItemProperties)
    }

    fun createSectionLabelItem(
        sectionCount: Int,
        labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
        itemSectionProperties: ItemSectionProperties
    ): SectionLabelItem {
        return SectionLabelItem(sectionCount, labelHeaderType, itemSectionProperties)
    }

    fun createButtonItem(key: FormKeys, action: ButtonAction, genericItemProperties: GenericItemProperties): ButtonItem {
        return ButtonItem(key, action, genericItemProperties)
    }

    fun createItemProperties(tableId: TableId = WordEntryTable.UI, id: Long = generateNewItemId()): ItemProperties {
        return ItemProperties(tableId, id = id)
    }

    fun createItemSectionProperties(tableId: TableId = WordEntryTable.UI, sectionId: Int): ItemSectionProperties {
        return ItemSectionProperties(tableId = tableId, id = generateNewItemId(), sectionId = sectionId)
    }
}