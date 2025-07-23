package io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.UpsertResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormDelete
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.model.DisplayEntryUIModel
import io.github.kwvolt.japanesedictionary.ui.model.DisplayNote
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import io.github.kwvolt.japanesedictionary.ui.model.DisplaySection
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryDetailPageViewModel(
    private val dictionaryId: Long?,
    private val _formItemManager: FormItemManager,
    private val _wordEntryFormUpsertValidation: WordEntryFormUpsertValidation,
    private val _wordEntryFormFetcher: WordEntryFormFetcher,
    private val _wordEntryFormDelete: WordEntryFormDelete,
    private val _wordEntryFormUpsert: WordEntryFormUpsert,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiState = MutableStateFlow(DisplayScreenState())
    val uiState: StateFlow<DisplayScreenState> get() = _uiState

    // used to send information to dialog to update note value / create new notes
    private val _noteUpsertEvents = Channel<NoteUpsertResult>()
    val noteUpsertEvents = _noteUpsertEvents.receiveAsFlow()

    private var isBookmarked: Boolean
        get() = savedStateHandle[IS_BOOKMARKED] ?: false
        set(value) = savedStateHandle.set(IS_BOOKMARKED, value)

    private var _wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
    private val wordEntryFormData: WordEntryFormData get()  = _wordEntryFormData

    // Store the expanded states (default empty)
    private var expandedSectionStates: MutableMap<Int, Boolean>
        get() = savedStateHandle[EXPANDED_STATES_KEY] ?: mutableMapOf()
        set(value) = savedStateHandle.set(EXPANDED_STATES_KEY, value)

    fun loadEntry() {
        if(dictionaryId !=null) {
            _uiState.update { it.copy(isLoading = true) }

            _formItemManager.clear()

            viewModelScope.launch {
                val formResult = _wordEntryFormFetcher.createWordFormData(dictionaryId, _formItemManager)
                val bookmarkResult = _wordEntryFormFetcher.fetchIsBookmarked(dictionaryId)

                handleDatabaseResult(formResult) { formData ->
                    _wordEntryFormData = formData
                    handleDatabaseResult(bookmarkResult) { isBookmarked = it }
                    _uiState.update { it.copy(isLoading = false, entry = formatText(wordEntryFormData)) }
                }
            }
        }
        else {
            _wordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
            _uiState.update { it.copy(entry = formatText(wordEntryFormData)) }
        }
    }

    private fun formatText(wordFormEntryDataToFormat: WordEntryFormData): DisplayEntryUIModel = with(wordFormEntryDataToFormat){
        val mainText: String = wordClassInput.chosenMainClass.displayText
        val subText: String = wordClassInput.chosenSubClass.displayText

        val entryNoteTextList: List<DisplayNote> = getEntryNoteMapAsList().map { DisplayNote(it.itemProperties.getId(), it.inputTextValue) }

        val sectionList: List<DisplaySection> = wordSectionMap.map{
            val kanaList: List<String> =  it.value.getKanaInputMapAsList().map { kanaItem -> kanaItem.inputTextValue }

            val sectionNoteList: List<DisplayNote> =  it.value.getComponentNoteInputMapAsList().map {
                    sectionNoteItem -> DisplayNote(sectionNoteItem.itemProperties.getId(), sectionNoteItem.inputTextValue)
            }
            DisplaySection(
                it.key,
                it.value.meaningInput.inputTextValue,
                kanaList,
                sectionNoteList
            )
        }

        DisplayEntryUIModel(
            primaryTextInput.inputTextValue,
            mainText,
            subText,
            entryNoteTextList,
            sectionList
        )
    }

    fun loadConjugation(){
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            TODO("implement the tab for conjugation")
        }
    }

    fun deleteNote(noteId: Long, isSection: Boolean){
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result: DatabaseResult<Unit> = if(!isSection){
                _wordEntryFormDelete.deleteEntryNote(noteId)
            }
            else {
                _wordEntryFormDelete.deleteSectionNote(noteId)
            }

            handleDatabaseResult(result){
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteEntry(){
        _uiState.update { it.copy(isLoading = true) }
        if(dictionaryId !=null) {
            viewModelScope.launch {
                handleDatabaseResult( _wordEntryFormDelete.deleteWordEntryFormData(dictionaryId)){
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun isBookmarked(): Boolean{
        return isBookmarked
    }

    fun toggleBookmark(): Boolean{
        if(dictionaryId != null){
            isBookmarked = !isBookmarked // toggle state
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                handleDatabaseResult(_wordEntryFormUpsert.updateIsBookmark(dictionaryId, isBookmarked)){
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
        return isBookmarked
    }

    // Safe getter
    fun isSectionExpanded(sectionId: Int): Boolean {
        return expandedSectionStates[sectionId] ?: false
    }

    // Safe toggle
    fun toggleSectionExpanded(sectionId: Int) {
        expandedSectionStates = expandedSectionStates.toMutableMap().apply {
            this[sectionId] = !(this[sectionId] ?: false)
        }
    }

    fun getOrGenerateNewNoteItem(noteId: Long? = null, sectionsId: Int? = null): TextItem {
        val entryData: WordEntryFormData = wordEntryFormData

        return if (sectionsId == null) {
            // Entry-level note
            val existingItem: TextItem? = entryData.getEntryNoteMapAsList().firstOrNull {
                it.itemProperties.getId() == noteId
            }

            existingItem ?: _formItemManager.createNewTextItem(
                InputTextType.ENTRY_NOTE_DESCRIPTION,
                _formItemManager.createItemProperties()
            )
        } else {
            // Section-level note
            val sectionNotes: List<TextItem> = entryData.wordSectionMap[sectionsId]
                ?.getComponentNoteInputMapAsList()
                ?: emptyList()

            val existingItem: TextItem? = sectionNotes.firstOrNull {
                it.itemProperties.getId() == noteId
            }

            existingItem ?: _formItemManager.createNewTextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                _formItemManager.createItemSectionProperties(sectionId = sectionsId)
            )
        }
    }

    fun upsertValidateNote(textItem: TextItem, newText: String) {
        _uiState.update { it.copy(isLoading = true) }

        val entryData = wordEntryFormData

        // get container Id the notes links to and get list of other notes belonging to it
        val (parentId, noteList) = when (val props = textItem.itemProperties) {
            is ItemProperties -> {
                dictionaryId?.let { Pair(it, entryData.getEntryNoteMapAsList()) }
                    ?: run {
                        viewModelScope.launch {
                            _noteUpsertEvents.send(NoteUpsertResult.UnknownError(IllegalStateException("Missing dictionary ID"), "Missing dictionary ID"))
                        }
                        return
                    }
            }
            is ItemSectionProperties -> {
                val meaningId = entryData.primaryTextInput.itemProperties.getId()
                Pair(meaningId, entryData.wordSectionMap[props.getSectionIndex()]
                    ?.getComponentNoteInputMapAsList() ?: emptyList())
            }
            else -> {
                viewModelScope.launch {
                    _noteUpsertEvents.send(NoteUpsertResult.UnknownError(IllegalStateException("Invalid item properties"), "Invalid item properties"))
                }
                return
            }
        }

        val updatedItem = textItem.copy(inputTextValue = newText)

        viewModelScope.launch {
            val result: UpsertResult<Long> = if (textItem.inputTextType == InputTextType.ENTRY_NOTE_DESCRIPTION) {
                _wordEntryFormUpsertValidation.dictionaryNote(parentId, updatedItem, noteList)
            } else {
                _wordEntryFormUpsertValidation.dictionarySectionNote(parentId, updatedItem, noteList)
            }

            val finalResult = when (result) {
                is UpsertResult.SingleItemOperationFailed -> NoteUpsertResult.ValidationError(result.error)

                is UpsertResult.Success<Long> -> NoteUpsertResult.Success(result.value)

                is UpsertResult.UnknownError -> NoteUpsertResult.UnknownError(result.exception, result.message)

                else -> {
                    val message = "Should not be possible to access ${result::class} at upsertValidateNote in ${DictionaryDetailPageViewModel::class}"
                    NoteUpsertResult.UnknownError(IllegalStateException(message), message)
                }
            }

            _noteUpsertEvents.send(finalResult)
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    private inline fun <T> handleDatabaseResult(
        result: DatabaseResult<T>,
        onSuccess: (T) -> Unit
    ) {
        when (result) {
            is DatabaseResult.Success -> onSuccess(result.value)
            is DatabaseResult.NotFound -> {
                _uiState.update {
                    it.copy(
                        screenStateUnknownError = ScreenStateUnknownError(
                            NoSuchElementException("Not found"),
                            "Not found"
                        )
                    )
                }
            }
            is DatabaseResult.UnknownError -> {
                _uiState.update {
                    it.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message))
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        screenStateUnknownError = ScreenStateUnknownError(
                            IllegalStateException("Unexpected type"),
                            "Unexpected result: ${result::class}"
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val EXPANDED_STATES_KEY = "expanded_states"
        private const val IS_BOOKMARKED = "isBookmarked"
    }
}

sealed class NoteUpsertResult {
    data class Success(val updatedNoteId: Long) : NoteUpsertResult()
    data class ValidationError(val message: String) : NoteUpsertResult()
    data class UnknownError(val exception: Throwable, val message: String? = null) : NoteUpsertResult()
}