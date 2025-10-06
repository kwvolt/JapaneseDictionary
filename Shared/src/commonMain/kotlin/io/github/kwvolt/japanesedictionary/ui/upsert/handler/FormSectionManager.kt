package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem

class FormSectionManager {
    private var currentSectionCount: Int = 1
    private val sectionChildrenCountMap = mutableMapOf<Int, Int>()

    fun removeSection(sectionId: Int){
        sectionChildrenCountMap.remove(sectionId)
    }

    fun addSectionToMap(sectionId: Int, list: MutableList<DisplayItem>){
        sectionChildrenCountMap[sectionId] = list.size
    }

    fun getThenIncrementCurrentSectionCount(): Int{
        val currentCount: Int = currentSectionCount
        currentSectionCount += 1
        return currentCount
    }

    fun setCurrentSectionCount(newCurrentSectionCount: Int){
        currentSectionCount = newCurrentSectionCount
    }

    // Returns the current entry count
    fun getCurrentSectionCount(): Int = currentSectionCount

    fun incrementChildrenCount(sectionId: Int){
        sectionChildrenCountMap[sectionId] = sectionChildrenCountMap.getOrPut(sectionId) { 0 } + 1
    }

    fun decrementChildrenCount(sectionId: Int){
        val count = sectionChildrenCountMap.getOrDefault(sectionId, 1)
        sectionChildrenCountMap[sectionId] = maxOf(count - 1, 0)
    }

    fun getChildrenCount(sectionId: Int): Int{
        return sectionChildrenCountMap[sectionId] ?: 0
    }

    fun clear(){
        currentSectionCount = 1
        sectionChildrenCountMap.clear()
    }
}