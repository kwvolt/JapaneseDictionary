package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.items.item.BaseItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage

class WordFormItemListManager(
    private val _wordUiFormHandler: UiFormHandlerInterface,
    private val _formSectionManager: FormSectionManager
) {
    fun generateFormList(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<DisplayItem> {
        return _wordUiFormHandler.createUIList(wordEntryFormData, formItemManager, _formSectionManager, errors)
    }

    fun generateSectionList(
        sectionKey: Int,
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<DisplayItem>{
        val section: WordSectionFormData = wordEntryFormData.wordSectionMap[sectionKey]?: WordSectionFormData.buildDefault(sectionKey, formItemManager)
        return _wordUiFormHandler.createSectionItems(sectionKey, section, formItemManager, _formSectionManager,  errors)
    }

    fun reBuild(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<DisplayItem>{
        // reset the display current section text to 1
        _formSectionManager.setCurrentSectionCount(1)
        return _wordUiFormHandler.createUIList(wordEntryFormData, formItemManager, _formSectionManager,errors)
    }


    fun addItemsAt(list: List<DisplayItem>, itemsToAdd: List<DisplayItem>, position: Int): List<DisplayItem> {
        val safePosition = position.coerceIn(0, list.size)
        return buildList {
            addAll(list.take(safePosition))
            addAll(itemsToAdd)
            addAll(list.drop(safePosition))
        }
    }

    fun removeItemsAt(list: List<DisplayItem>, position: Int, count: Int): List<DisplayItem> {
        val safePosition = position.coerceIn(0, list.size)
        val safeCount = count.coerceAtLeast(0)
        val dropIndex = (safePosition + safeCount).coerceAtMost(list.size)
        return buildList {
            addAll(list.take(safePosition))
            addAll(list.drop(dropIndex))
        }
    }

    fun updateItemAt(list: List<DisplayItem>, item: DisplayItem, position: Int): List<DisplayItem> {
        return list.toMutableList().also { it[position] = item }
    }

    fun removeItemAt(list: List<DisplayItem>, position: Int, sectionId: Int? = null): List<DisplayItem> {
        sectionId?.let { _formSectionManager.decrementChildrenCount(it) }
        return list.toMutableList().also { it.removeAt(position) }
    }

    fun addItemAt(list: List<DisplayItem>, item: DisplayItem, position: Int, sectionId: Int? = null): List<DisplayItem> {
        sectionId?.let { _formSectionManager.incrementChildrenCount(it) }
        return list.toMutableList().also { it.add(position, item) }
    }

    fun removeSection(
        currentList: List<DisplayItem>,
        sectionId: Int,
        sectionCount: Int,
        position: Int,
    ): List<DisplayItem> {
        _formSectionManager.setCurrentSectionCount(sectionCount)
        val childrenCount = _formSectionManager.getChildrenCount(sectionId)
        _formSectionManager.removeSection(sectionId)
        val resultList = removeItemsAt(currentList, position, childrenCount).toMutableList()
        val updated = updateEntryLabelsInList(resultList, position)
        return updated
    }

    private fun updateEntryLabelsInList(list: MutableList<DisplayItem>, position: Int): List<DisplayItem> {
        for (i in position until list.size) {
            val displayItem: DisplayItem = list[i]
            if(displayItem is DisplayItem.DisplayLabelItem){
                val baseItem: BaseItem = displayItem.item
                if (baseItem is SectionLabelItem) {
                    // Modify the item directly in the list
                    val updatedItem: SectionLabelItem = baseItem.copy(sectionCount = _formSectionManager.getThenIncrementCurrentSectionCount())
                    val updatedDisplayItem: DisplayItem = displayItem.copy(item = updatedItem)
                    list[i] = updatedDisplayItem
                }
            }
        }
        return list
    }
}