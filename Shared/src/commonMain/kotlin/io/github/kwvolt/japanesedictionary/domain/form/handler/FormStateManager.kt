package io.github.kwvolt.japanesedictionary.domain.form.handler

class FormStateManager {
    private var currentEntryCount = 1
    private val entryChildrenCountMap = mutableMapOf<String, Int>()

    // Updates the current entry count
    fun incrementEntryCount() {
        currentEntryCount += 1
    }

    // Updates the current entry count
    fun decrementEntryCount() {
        currentEntryCount -= 1
    }

    fun setCurrentEntryCount(currentEntryCount: Int){
        this.currentEntryCount = currentEntryCount
    }

    // Returns the current entry count
    fun getCurrentEntryCount(): Int = currentEntryCount

    // Updates the entryChildrenCountMap for a given entry
    fun setEntryChildrenCount(entryId: String, count: Int) {
        entryChildrenCountMap[entryId] = count
    }

    fun incrementChildrenCount(entryId: String){
        val childCount: Int =   entryChildrenCountMap[entryId] ?: 0
        entryChildrenCountMap[entryId] = childCount + 1
    }

    fun getChildrenCount(entryId: String): Int{
        return entryChildrenCountMap[entryId] ?: 0
    }

    // Returns the entryChildrenCountMap
    fun getEntryChildrenCountMap(): Map<String, Int> = entryChildrenCountMap

    fun removeIdFromChildrenCountMap(identifier: String) {
        entryChildrenCountMap.remove(identifier)
    }
}