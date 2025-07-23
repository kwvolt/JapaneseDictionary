package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TableId
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordEntryTable

class FormItemManager {
    private var currentSectionCount: Int = 1
    private val entryChildrenCountMap = mutableMapOf<Int, Int>()

    private var entrySectionId: Int = 0

    private var nextAvailableId: Long = 0

    fun generateNewItemId(): Long {
        return nextAvailableId++
    }

    fun removeSection(sectionId: Int){
        entryChildrenCountMap.remove(sectionId)
    }

    fun addSectionToMap(sectionId: Int, list: List<BaseItem>){
        entryChildrenCountMap[sectionId] = list.size
    }

    fun incrementCurrentSectionCount(){
        currentSectionCount += 1
    }

    fun getThenIncrementEntrySectionId(): Int{
        val value: Int = entrySectionId
        entrySectionId += 1
        return value
    }

    fun setCurrentSectionCount(newCurrentSectionCount: Int){
        currentSectionCount = newCurrentSectionCount
    }

    // Returns the current entry count
    fun getCurrentSectionCount(): Int = currentSectionCount

    fun incrementChildrenCount(sectionId: Int){
        entryChildrenCountMap[sectionId] = entryChildrenCountMap.getOrPut(sectionId) { 0 } + 1
    }

    fun getChildrenCount(sectionId: Int): Int{
        return entryChildrenCountMap[sectionId] ?: 0
    }

    fun clear(){
        entrySectionId = 0
        nextAvailableId = 0
    }

    fun createNewTextItem(inputTextType: InputTextType, genericItemProperties: GenericItemProperties): TextItem{
        return TextItem(inputTextType, "", genericItemProperties)
    }

    fun createNewWordClassItem(
        choseMainClass: MainClassContainer = MainClassContainer(-1, "---", "-----"),
        chosenSubClass: SubClassContainer = SubClassContainer(-1, "---", "-----"),
        genericItemProperties: GenericItemProperties
    ): WordClassItem {
        return WordClassItem(choseMainClass, chosenSubClass, genericItemProperties)
    }

    fun createStaticLabelItem(name: String = "", labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER, genericItemProperties: GenericItemProperties): StaticLabelItem {
        return StaticLabelItem(name, labelHeaderType, genericItemProperties)
    }

    fun createEntryLabelItem(name: String = "Section", sectionCount: Int,labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER, itemSectionProperties: ItemSectionProperties): EntryLabelItem {
        return EntryLabelItem(name, sectionCount, labelHeaderType, itemSectionProperties)
    }

    fun createButtonItem(name: String = "", action: ButtonAction, genericItemProperties: GenericItemProperties): ButtonItem{
        return ButtonItem(name, action, genericItemProperties)
    }

    fun createItemProperties(tableId: TableId = WordEntryTable.UI, id: Long = generateNewItemId()): ItemProperties {
        return ItemProperties(tableId, id = id)
    }

    fun createItemSectionProperties(tableId: TableId = WordEntryTable.UI, sectionId: Int): ItemSectionProperties {
        return ItemSectionProperties(tableId = tableId, id = generateNewItemId(), sectionId = sectionId)
    }
}