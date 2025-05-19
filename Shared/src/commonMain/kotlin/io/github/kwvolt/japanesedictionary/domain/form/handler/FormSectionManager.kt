package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem

class FormSectionManager {
    private var currentSectionCount: Int = 1
    private val entryChildrenCountMap = mutableMapOf<Int, Int>()
    private var entrySectionId: Int = 0

    fun removeSection(sectionId: Int){
        entryChildrenCountMap.remove(sectionId)
    }

    fun addSection(sectionId: Int, list: List<BaseItem>){
        entryChildrenCountMap[sectionId] = list.size-1
        entrySectionId += 1
    }

    fun addSectionToMap(sectionId: Int, list: List<BaseItem>){
        entryChildrenCountMap[sectionId] = list.size-1
    }

    fun incrementCurrentSectionCount(){
        currentSectionCount += 1
    }

    fun decrementCurrentSectionCount(){
        currentSectionCount -= 1
    }

    fun getThenIncrementEntrySectionId(): Int{
        val value: Int = entrySectionId
        entrySectionId += 1
        return value
    }

    fun setCurrentEntryCount(newCurrentSectionCount: Int){
        currentSectionCount = newCurrentSectionCount
    }

    // Returns the current entry count
    fun getCurrentSectionCount(): Int = currentSectionCount

    fun getEntrySectionId(): Int = entrySectionId

    // Updates the entryChildrenCountMap for a given entry
    fun setEntryChildrenCount(sectionId: Int, count: Int) {
        if(entryChildrenCountMap.containsKey(sectionId)){
            entryChildrenCountMap[sectionId] = count
        }
    }

    fun incrementChildrenCount(sectionId: Int){
        val childCount: Int =   entryChildrenCountMap[sectionId] ?: 0
        entryChildrenCountMap[sectionId] = childCount + 1
    }

    fun decrementChildrenCount(sectionId: Int){
        val childCount: Int =   entryChildrenCountMap[sectionId] ?: 0
        entryChildrenCountMap[sectionId] = childCount - 1
    }

    fun getChildrenCount(sectionId: Int): Int{
        return entryChildrenCountMap[sectionId] ?: 0
    }

    fun clear(){
        currentSectionCount = 1
        entrySectionId = 0
        entryChildrenCountMap.clear()
    }
}