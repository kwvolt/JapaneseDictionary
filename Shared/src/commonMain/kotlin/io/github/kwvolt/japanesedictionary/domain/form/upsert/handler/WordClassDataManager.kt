package io.github.kwvolt.japanesedictionary.domain.form.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassBuilder
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem

class WordClassDataManager(private val wordClassBuilder: WordClassBuilder, private val includeDefault: Boolean) {

    // word class variables
    private var _mainClassData: List<MainClassContainer> = emptyList()
    private var _subClassMapData: Map<Long, List<SubClassContainer>> = emptyMap()

    suspend fun loadWordClassData(): DatabaseResult<Unit> {
        val mainResult = wordClassBuilder.getMainClassList()
        return mainResult.flatMap { mainClasses ->
            val updatedMainClasses = if (includeDefault) listOf(DEFAULT_MAIN) + mainClasses else mainClasses
            val updatedSubMap = when(val result = wordClassBuilder.getSubClassMap(mainClasses)){
                is DatabaseResult.Success -> {
                    if (includeDefault) {
                        result.value.toMutableMap().apply {
                            put(DEFAULT_MAIN.id, listOf(DEFAULT_SUB))
                        }
                    } else {
                        result.value
                    }
                }
                else -> return result.mapErrorTo()
            }
            _mainClassData = updatedMainClasses
            _subClassMapData = updatedSubMap
            DatabaseResult.Success(Unit)
        }
    }

    fun initializeWordEntryFormDataWordClass(wordEntryFormData: WordEntryFormData): WordEntryFormData {
        val firstMainClass = _mainClassData.firstOrNull()
            ?: return wordEntryFormData

        val firstSubClass = _subClassMapData[firstMainClass.id]?.firstOrNull()
            ?: return wordEntryFormData

        val wordClassItem = wordEntryFormData.wordClassInput.copy(
            chosenMainClass = firstMainClass,
            chosenSubClass = firstSubClass
        )
        return wordEntryFormData.copy(wordClassInput = wordClassItem)
    }

    // Word Class
    fun updateMainClassId(
        wordClassItem: WordClassItem,
        selectionPosition: Int,
        handler: WordFormHandler
    ): WordClassItem? {
        val selectedMainClass = _mainClassData.getOrNull(selectionPosition) ?: return null

        if (wordClassItem.chosenMainClass == selectedMainClass) return null

        val firstSubClass = _subClassMapData[selectedMainClass.id]?.firstOrNull()

        return handler.updateMainClassWordClassItemCommand(
            wordClassItem,
            selectedMainClass,
            firstSubClass.takeIf { it != wordClassItem.chosenSubClass }
        )
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
        _mainClassData.indexOfFirst { it.id == wordClassItem.chosenMainClass.id }.takeIf { it >= 0 } ?: NO_INDEX

    fun getSubClassListIndex(wordClassItem: WordClassItem): Int =
        getSubClassList(wordClassItem).indexOfFirst { it.id == wordClassItem.chosenSubClass.id }.takeIf { it >= 0 } ?: NO_INDEX

    fun getMainClassListIndex(mainClassId: Long): Int =
        _mainClassData.indexOfFirst { it.id == mainClassId }.takeIf { it >= 0 } ?: NO_INDEX

    fun getSubClassListIndex(mainClassId: Long, subClassId: Long): Int =
        getSubClassList(mainClassId).indexOfFirst { it.id == subClassId }.takeIf { it >= 0 } ?: NO_INDEX

    fun getMainClassList(): List<MainClassContainer>{
        return _mainClassData
    }

    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>{
        return getSubClassList(wordClassItem.chosenMainClass.id)
    }

    fun getSubClassList(mainClassId: Long): List<SubClassContainer>{
        if (mainClassId == NO_ID) {
            return _subClassMapData.values.firstOrNull() ?: emptyList()
        }
        val subList: List<SubClassContainer> = _subClassMapData[mainClassId] ?: emptyList()
        return subList
    }

    fun getMainClassId(selectedPosition: Int): Long{
        val selectedMainClass = _mainClassData.getOrNull(selectedPosition) ?: return NO_ID
        return selectedMainClass.id
    }

    fun getSubClassId(mainClassId: Long, selectedPosition: Int): Long{
        val subClassList = getSubClassList(mainClassId)
        val subClass = subClassList.getOrNull(selectedPosition) ?: return NO_ID
        return subClass.id
    }

    companion object {
        const val NO_ID: Long = -1
        val DEFAULT_MAIN: MainClassContainer = MainClassContainer(NO_ID, "DEFAULT", "default")
        val DEFAULT_SUB: SubClassContainer = SubClassContainer(NO_ID, "DEFAULT", "default")
        const val NO_INDEX: Int = -1
    }
}