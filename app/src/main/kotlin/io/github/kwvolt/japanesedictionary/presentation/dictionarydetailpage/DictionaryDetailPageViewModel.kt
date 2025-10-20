package io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidUpsertResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormDelete
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormItemFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition.WordDefinitionTabFragment
import io.github.kwvolt.japanesedictionary.ui.model.DisplayEntryUIModel
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import io.github.kwvolt.japanesedictionary.util.handleResultWithErrorCopy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryDetailPageViewModel(
    private val dictionaryId: Long,
    private val _formItemManager: FormItemManager,
    private val _wordEntryFormUpsertValidation: WordEntryFormUpsertValidation,
    private val _wordEntryFormItemFetcher: WordEntryFormItemFetcher,
    private val _wordEntryFormBuilder: WordEntryFormBuilder,
    private val _wordEntryFormDelete: WordEntryFormDelete,
    private val _wordEntryFormUpsert: WordEntryFormUpsert,
    private val _userFetcher: UserFetcher,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiState = MutableStateFlow(DisplayScreenState())
    val uiState: StateFlow<DisplayScreenState> get() = _uiState

    // used to send information to dialog to update note value / create new notes
    private val _noteUpsertEvents = Channel<NoteUpsertResult>()
    val noteUpsertEvents = _noteUpsertEvents.receiveAsFlow()

    private var isBookmarked: Boolean
        get() = savedStateHandle[IS_BOOKMARKED_KEY] ?: false
        set(value) = savedStateHandle.set(IS_BOOKMARKED_KEY, value)

    private var _wordEntryFormData: WordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
    val currentFormData: WordEntryFormData get() = _wordEntryFormData

    // Store the expanded states (default empty)
    private var expandedNoteContainerStates: MutableMap<Int, Boolean>
        get() = savedStateHandle[EXPANDED_STATES_KEY] ?: mutableMapOf()
        set(value) = savedStateHandle.set(EXPANDED_STATES_KEY, value)

    suspend fun loadEntry(textFormatter: (WordEntryFormData) -> DisplayEntryUIModel) {
        if (dictionaryId != -1L) {
            _uiState.update { it.copy(isLoading = true) }
            _formItemManager.clear()
            val formResult = _wordEntryFormBuilder.buildDetailedFormData(dictionaryId, _formItemManager)
            val bookmarkResult = _userFetcher.fetchIsBookmarked(dictionaryId)
            _uiState.handleResultWithErrorCopy("loadEntry",formResult) { formData ->
                _wordEntryFormData = formData
                _uiState.handleResultWithErrorCopy("loadEntry",bookmarkResult) { isBookmarked = it }
                _uiState.update { it.copy(isLoading = false, entry = textFormatter(currentFormData)) }
            }
        } else {
            _wordEntryFormData = WordEntryFormData.buildDefault(_formItemManager)
            _uiState.update { it.copy(entry = textFormatter(currentFormData)) }
        }
    }

    fun loadConjugation() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            TODO("implement the tab for conjugation")
        }
    }

    fun deleteNote(noteId: Long, isSection: Boolean) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result: DatabaseResult<Unit> = if (!isSection) {
                _wordEntryFormDelete.deleteEntryNote(noteId)
            } else {
                _wordEntryFormDelete.deleteSectionNote(noteId)
            }
            _uiState.handleResultWithErrorCopy("deleteNote", result){ _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun deleteEntry() {
        _uiState.update { it.copy(isLoading = true) }
        if (dictionaryId != -1L) {
            viewModelScope.launch {
                val result: DatabaseResult<Unit> = _wordEntryFormDelete.deleteWordEntryFormData(dictionaryId)
                _uiState.handleResultWithErrorCopy("deleteEntry", result){ _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }

    fun getIsBookmarked(): Boolean {
        return isBookmarked
    }

    suspend fun toggleBookmark(): Boolean {
        isBookmarked = !isBookmarked
        if (dictionaryId != -1L) {
            _uiState.update { it.copy(isLoading = true) }
            val result: DatabaseResult<Unit> = _wordEntryFormUpsert.updateIsBookmark(dictionaryId, isBookmarked)
            _uiState.handleResultWithErrorCopy("toggleBookmarked", result){
                _uiState.update { it.copy(isLoading = false) }
            }
        }
        return isBookmarked
    }

    // Safe getter
    fun isNotesContainerExpanded(noteContainerId: Int): Boolean {
        return expandedNoteContainerStates[noteContainerId] ?: false
    }

    // Safe toggle
    fun toggleNoteContainerExpanded(noteContainerId: Int) {
        expandedNoteContainerStates = expandedNoteContainerStates.toMutableMap().apply {
            this[noteContainerId] = !(this[noteContainerId] ?: false)
        }
    }

    fun setToggleNoteContainerExpanded(noteContainerId: Int, isExpanded: Boolean) {
        expandedNoteContainerStates = expandedNoteContainerStates.toMutableMap().apply {
            this[noteContainerId] = isExpanded
        }
    }

    suspend fun refreshNoteInputs(noteContainerID: Int) {
        if (noteContainerID != WordDefinitionTabFragment.GENERAL_NOTE_CONTAINER_ID) {
            val sectionMap = currentFormData.wordSectionMap
            val section: WordSectionFormData? = sectionMap[noteContainerID]
            val meaningId: Long? = section?.meaningInput?.itemProperties?.getId()

            meaningId?.let {
                val result = _wordEntryFormItemFetcher.fetchSectionNoteItemList(meaningId, noteContainerID)
                _uiState.handleResultWithErrorCopy("updateWordEntryFormDataNotes", result) {
                    val updatedSection = section.copy(
                        noteInputMap = _wordEntryFormItemFetcher.persistentListToMap(it)
                    )
                    _wordEntryFormData = currentFormData.copy(
                        wordSectionMap = sectionMap.put(noteContainerID, updatedSection)
                    )
                }
            }
        } else if (dictionaryId != -1L) {
            val result = _wordEntryFormItemFetcher.fetchEntryNoteItemList(dictionaryId)
            _uiState.handleResultWithErrorCopy("updateWordEntryFormDataNotes", result) {
                _wordEntryFormData = currentFormData.copy(
                    noteInputMap = _wordEntryFormItemFetcher.persistentListToMap(it)
                )
            }
        }
    }

    fun getOrGenerateNewNoteItem(noteId: Long? = null, noteContainerID: Int = -1): TextItem {
        val entryData: WordEntryFormData = currentFormData
        return if (noteContainerID == WordDefinitionTabFragment.GENERAL_NOTE_CONTAINER_ID) {
            // Entry-level note
            val existingItem: TextItem? = entryData.getNoteInputMapAsList().firstOrNull {
                it.itemProperties.getId() == noteId
            }
            existingItem ?: _formItemManager.createNewTextItem(
                InputTextType.DICTIONARY_NOTE_DESCRIPTION,
                genericItemProperties = _formItemManager.createItemProperties()
            )
        } else {
            // Section-level note
            val sectionNotes: List<TextItem> = entryData.wordSectionMap[noteContainerID]
                ?.getNoteInputMapAsList()
                ?: emptyList()

            val existingItem: TextItem? = sectionNotes.firstOrNull {
                it.itemProperties.getId() == noteId
            }

            existingItem ?: _formItemManager.createNewTextItem(
                InputTextType.SECTION_NOTE_DESCRIPTION,
                genericItemProperties = _formItemManager.createItemSectionProperties(sectionId = noteContainerID)
            )
        }
    }

    fun upsertValidateNote(textItem: TextItem, newText: String) {
        _uiState.update { it.copy(isLoading = true) }
        val entryData = currentFormData
        // get container Id the notes links to and get list of other notes belonging to it
        val (parentId, noteList) = when (val props = textItem.itemProperties) {
            is ItemProperties -> {
                if (dictionaryId != -1L) {
                    Pair(dictionaryId, entryData.getNoteInputMapAsList())
                } else {
                    sendNoteError("Missing dictionary ID")
                    return
                }
            }
            is ItemSectionProperties -> {
                val wordSection: WordSectionFormData? = entryData.wordSectionMap[props.getSectionIndex()]
                if(wordSection != null){
                    val meaningId = wordSection.meaningInput.itemProperties.getId()
                    Pair(meaningId, wordSection.getNoteInputMapAsList())
                }else {
                    sendNoteError("Missing dictionary section ID")
                    return
                }
            }
            else -> {
                sendNoteError("Invalid item properties")
                return
            }
        }
        val updatedItem = textItem.copy(inputTextValue = newText)
        viewModelScope.launch {
            val result: ValidUpsertResult<Long> =
                if (textItem.inputTextType == InputTextType.DICTIONARY_NOTE_DESCRIPTION) {
                    _wordEntryFormUpsertValidation.dictionaryNote(parentId, updatedItem, noteList)
                } else {
                    _wordEntryFormUpsertValidation.dictionarySectionNote(parentId, updatedItem, noteList)
                }
            val finalResult = when (result) {
                is ValidUpsertResult.SingleItemOperationFailed -> NoteUpsertResult.ValidationError(result.error)
                is ValidUpsertResult.Success<Long> -> NoteUpsertResult.Success(result.value)
                is ValidUpsertResult.UnknownError -> NoteUpsertResult.UnknownError(result.exception)
                else -> { val message = "Should not be possible to access ${result::class} at upsertValidateNote in ${DictionaryDetailPageViewModel::class}"
                    NoteUpsertResult.UnknownError(IllegalStateException(message), message)
                }
            }
            _noteUpsertEvents.send(finalResult)
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun sendNoteError(message: String){
        viewModelScope.launch {
            _noteUpsertEvents.send(NoteUpsertResult.UnknownError(IllegalStateException(message), message))
        }
    }

    companion object {
        private const val EXPANDED_STATES_KEY = "EXPANDED_STATES_KEY"
        private const val IS_BOOKMARKED_KEY = "IS_BOOKMARKED_KEY"
    }
}

sealed class NoteUpsertResult {
    data class Success(val updatedNoteId: Long) : NoteUpsertResult()
    data class ValidationError(val message: String) : NoteUpsertResult()
    data class UnknownError(val exception: Throwable, val message: String? = null) : NoteUpsertResult()
}