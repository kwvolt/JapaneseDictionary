package io.github.kwvolt.japanesedictionary.domain.form.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ConjugationTemplateItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem

class ConjugationDataManager(private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface) {
    private var _conjugationTemplateData: List<ConjugationTemplateContainer> = emptyList()
    private val _kanaIdMap: MutableMap<String, Long> = mutableMapOf()

    suspend fun loadConjugationData(): DatabaseResult<Unit>{
        _conjugationTemplateData = conjugationTemplateRepository.selectAll().getOrReturn { return it }
        return DatabaseResult.Success(Unit)
    }

    fun initializeWordEntryFormDataConjugationTemplate(wordEntryFormData: WordEntryFormData): WordEntryFormData {
        val firstConjugationTemplate: ConjugationTemplateContainer = _conjugationTemplateData.firstOrNull()
            ?: return wordEntryFormData

        val conjugationTemplateItem = wordEntryFormData.conjugationTemplateInput.copy(
            chosenConjugationTemplateId = firstConjugationTemplate.id
        )
        return wordEntryFormData.copy(conjugationTemplateInput = conjugationTemplateItem)
    }

    fun updateConjugationTemplateId(
        conjugationTemplateItem: ConjugationTemplateItem,
        selectionPosition: Int,
        handler: WordFormHandler
    ): ConjugationTemplateItem? {
        val selectedConjugationTemplate: ConjugationTemplateContainer = _conjugationTemplateData.getOrNull(selectionPosition) ?: return null
        if (conjugationTemplateItem.chosenConjugationTemplateId == selectedConjugationTemplate.id) return null
        return handler.updateConjugationTemplateIdItemCommand(conjugationTemplateItem, selectedConjugationTemplate.id)
    }

    fun updateConjugationTemplateKana(
        conjugationTemplateItem: ConjugationTemplateItem,
        kanaId: Long,
        handler: WordFormHandler
    ): ConjugationTemplateItem? {
        if (conjugationTemplateItem.kanaId == kanaId) return null
        return handler.updateConjugationTemplateKanaItemCommand(conjugationTemplateItem, kanaId)
    }

    fun upsertKanaMap(textItem: TextItem){
        _kanaIdMap.put(textItem.itemProperties.getIdentifier(), textItem.inputTextValue)
    }

    fun deleteKanaMap(textItem: TextItem){
        _kanaIdMap.remove(textItem.itemProperties.getIdentifier())
    }

    fun getKanaListIndex(conjugationTemplateItem: ConjugationTemplateItem): Int =
        _conjugationTemplateData.indexOfFirst { it.id == conjugationTemplateItem.chosenConjugationTemplateId }.takeIf { it >= 0 } ?: NO_INDEX


    fun getConjugationTemplateListIndex(conjugationTemplateItem: ConjugationTemplateItem): Int =
        _conjugationTemplateData.indexOfFirst { it.id == conjugationTemplateItem.chosenConjugationTemplateId }.takeIf { it >= 0 } ?: NO_INDEX


    fun getConjugationTemplateId(selectedPosition: Int): Long{
        val selectedConjugationTemplate: ConjugationTemplateContainer = _conjugationTemplateData.getOrNull(selectedPosition) ?: return NO_ID
        return selectedConjugationTemplate.id
    }

    companion object {
        const val NO_ID: Long = -1
        const val NO_INDEX: Int = -1
    }
}