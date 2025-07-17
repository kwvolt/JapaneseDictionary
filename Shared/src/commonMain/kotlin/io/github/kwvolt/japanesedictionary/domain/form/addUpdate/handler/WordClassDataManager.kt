package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem

class WordClassDataManager() {

    // word class variables
    private var _mainClassData: List<MainClassContainer> = listOf(MainClassContainer(0, "TEMP", "temp"))
    private var _subClassMapData: Map<Long, List<SubClassContainer>> = mapOf(0L to listOf(
        SubClassContainer(0, "TEMP", "temp")
    ))

    suspend fun loadWordClassData(wordFormService: WordFormService): DatabaseResult<Unit> {
        val main = wordFormService.getMainClassList()
        return main.flatMap { mainClasses ->
            _mainClassData = mainClasses
            wordFormService.getSubClassMap(mainClasses).flatMap { subMap ->
                _subClassMapData = subMap
                DatabaseResult.Success(Unit)
            }
        }
    }

    // Word Class
    fun updateMainClassId(wordClassItem: WordClassItem, selectionPosition: Int, handler: WordFormHandler): WordClassItem? {
        val selectedMainClass = _mainClassData.getOrNull(selectionPosition)
            ?: return null

        return if (wordClassItem.chosenMainClass != selectedMainClass) {
            handler.updateMainClassWordClassItemCommand(wordClassItem, selectedMainClass)
        } else {
            null
        }
    }

    fun updateSubClassId(
        wordClassItem: WordClassItem,
        selectedPosition: Int,
        handler: WordFormHandler
    ): WordClassItem? {
        val subClassList = getSubClassList(wordClassItem)
        val subClass = subClassList.getOrNull(selectedPosition)
            ?: return null
        return handler.updateSubClassWordClassItemCommand(wordClassItem, subClass)
    }

    fun getMainClassListIndex(wordClassItem: WordClassItem): Int =
        _mainClassData.indexOfFirst { it.id == wordClassItem.chosenMainClass.id }.takeIf { it >= 0 } ?: 0

    fun getSubClassListIndex(wordClassItem: WordClassItem): Int =
        getSubClassList(wordClassItem).indexOfFirst { it.id == wordClassItem.chosenSubClass.id }.takeIf { it >= 0 } ?: 0

    fun getMainClassList(): List<MainClassContainer>{
        return _mainClassData
    }

    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>{
        val subList: List<SubClassContainer> = _subClassMapData[wordClassItem.chosenMainClass.id] ?: emptyList()
        return subList
    }
}