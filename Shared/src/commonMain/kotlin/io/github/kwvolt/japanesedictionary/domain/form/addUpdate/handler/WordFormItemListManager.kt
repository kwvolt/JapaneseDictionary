package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage

class WordFormItemListManager(
    private val wordUiFormHandler: UiFormHandlerInterface
) {
    fun generateFormList(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<BaseItem> {
        return wordUiFormHandler.createUIList(wordEntryFormData, formItemManager, errors)
    }

    fun generateSectionList(
        sectionKey: Int,
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<BaseItem>{
        val section: WordSectionFormData = wordEntryFormData.wordSectionMap[sectionKey]?: WordSectionFormData.buildDefault(sectionKey, formItemManager)
        return wordUiFormHandler.createSectionItems(sectionKey, section, formItemManager, errors)
    }

    fun reBuild(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage> = emptyMap()
    ): List<BaseItem>{
        // reset the display current section text to 1
        formItemManager.setCurrentSectionCount(1)
        return wordUiFormHandler.createUIList(wordEntryFormData, formItemManager, errors)
    }


    fun addItemsAt(list: List<BaseItem>, itemsToAdd: List<BaseItem>, position: Int): List<BaseItem> {
        val safePosition = position.coerceIn(0, list.size)
        return buildList {
            addAll(list.take(safePosition))
            addAll(itemsToAdd)
            addAll(list.drop(safePosition))
        }
    }

    fun removeItemsAt(list: List<BaseItem>, position: Int, count: Int): List<BaseItem> {
        val safePosition = position.coerceIn(0, list.size)
        val safeCount = count.coerceAtLeast(0)
        val dropIndex = (safePosition + safeCount).coerceAtMost(list.size)
        return buildList {
            addAll(list.take(safePosition))
            addAll(list.drop(dropIndex))
        }
    }

    fun updateItemAt(list: List<BaseItem>, item: BaseItem, position: Int): List<BaseItem> {
        return list.toMutableList().also { it[position] = item }
    }

    fun removeItemAt(list: List<BaseItem>, position: Int): List<BaseItem> {
        return list.toMutableList().also { it.removeAt(position) }
    }

    fun addItemAt(list: List<BaseItem>, item: BaseItem, position: Int): List<BaseItem> {
        return list.toMutableList().also { it.add(position, item) }
    }

    fun removeSection(
        currentList: List<BaseItem>,
        sectionId: Int,
        sectionCount: Int,
        position: Int,
        formItemManager: FormItemManager
    ): List<BaseItem> {
        formItemManager.setCurrentSectionCount(sectionCount)
        val childrenCount = formItemManager.getChildrenCount(sectionId)
        formItemManager.removeSection(sectionId)
        val resultList = removeItemsAt(currentList, position, childrenCount).toMutableList()
        val updated = updateEntryLabelsInList(resultList, position, formItemManager)
        return updated
    }

    private fun updateEntryLabelsInList(list: MutableList<BaseItem>, position: Int, formItemManager: FormItemManager): List<BaseItem> {
        for (i in position until list.size) {
            val item = list[i]
            if (item is EntryLabelItem) {
                // Modify the item directly in the list
                val updatedItem = item.copy(sectionCount = formItemManager.getCurrentSectionCount())
                formItemManager.incrementCurrentSectionCount()
                list[i] = updatedItem
            }
        }
        return list
    }
}