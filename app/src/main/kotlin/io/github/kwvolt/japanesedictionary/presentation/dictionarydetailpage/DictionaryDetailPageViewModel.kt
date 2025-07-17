package io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryDetailPageViewModel(
    private val dictionaryId: Long?,
    private val _wordFormService: WordFormService,
    private val _formItemManager: FormItemManager,
    private val _wordEntryFormUpsert: WordEntryFormUpsert
): ViewModel() {

    private val _uiState = MutableStateFlow(DisplayScreenState())
    val uiState: StateFlow<DisplayScreenState> get() = _uiState

    private var isBookmarked: Boolean = false


    fun loadEntry() {
        if(dictionaryId !=null) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                val result: DatabaseResult<WordEntryFormData> =
                    _wordFormService.getWordFormData(dictionaryId, _formItemManager)

                when (result) {
                    is DatabaseResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, entry = result.value) }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                screenStateUnknownError = ScreenStateUnknownError(
                                    IllegalStateException("meep"),
                                    "meep"
                                )
                            )
                        }

                    }
                }
            }
        }
        else {
            val wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
            _uiState.update { it.copy(entry = wordEntryFormData) }
        }
    }

    fun loadConjugation(){
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            TODO()
        }
    }

    fun isBookmarked(): Boolean{
        return isBookmarked
    }

    fun toggleBookmark(): Boolean{
        isBookmarked = !isBookmarked // toggle state
        return isBookmarked
    }

    fun deleteEntry(){
        if(dictionaryId !=null) {
            viewModelScope.launch {
                when(val result = _wordFormService.deleteWordEntryFormData(dictionaryId)){
                    is DatabaseResult.Success -> TODO()
                    is DatabaseResult.UnknownError -> _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message)) }
                    else -> {
                        _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(IllegalStateException(), "")) }
                    }
                }
            }
        }
    }

    fun generateNewNoteItem(){
        _formItemManager.createNewTextItem(InputTextType.ENTRY_NOTE_DESCRIPTION, _formItemManager.createItemSectionProperties(sectionId = 3))
    }

    fun validateNote(textItem: TextItem): String? {
        val wordEntryFormData: WordEntryFormData = uiState.value.entry ?: return null
        val noteList = when (val itemProperties = textItem.itemProperties) {
            is ItemProperties -> wordEntryFormData.getEntryNoteMapAsList()
            is ItemSectionProperties -> wordEntryFormData.wordSectionMap[itemProperties.getSectionIndex()]?.getComponentNoteInputMapAsList() ?: emptyList()
            else -> emptyList()
        }

        var errorMessage: String? = null


        val result: ValidationResult<ItemKey> = _wordFormService.validateNotes(textItem, noteList)
        when(result){
            is ValidationResult.InvalidInput -> {
                errorMessage =  formatValidationTypeToMessage(result.error)
            }
            is ValidationResult.Success -> {

            }
            is ValidationResult.UnknownError -> _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message)) }
            else -> {
                _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(IllegalStateException(), "")) }
            }
        }
        return errorMessage
    }

    fun upsertNote(textItem: TextItem){
        viewModelScope.launch {
            when(textItem.inputTextType){
                InputTextType.ENTRY_NOTE_DESCRIPTION -> {
                    if(dictionaryId != null){
                        val result: DatabaseResult<Long> = _wordEntryFormUpsert.upsertDictionaryEntryNote(dictionaryId, textItem)
                        when(result){
                            is DatabaseResult.InvalidInput -> {

                            }
                            DatabaseResult.NotFound -> TODO()
                            is DatabaseResult.Success<*> -> TODO()
                            is DatabaseResult.UnknownError -> TODO()
                        }
                    }
                }
                InputTextType.SECTION_NOTE_DESCRIPTION -> {
                    if(dictionaryId != null){
                        val result: DatabaseResult<Long> = _wordEntryFormUpsert.upsertDictionarySectionNote(dictionaryId, textItem)
                    }

                }
                else -> _uiState.update { it.copy(screenStateUnknownError = ScreenStateUnknownError(IllegalStateException(), "")) }
            }
        }
    }
}