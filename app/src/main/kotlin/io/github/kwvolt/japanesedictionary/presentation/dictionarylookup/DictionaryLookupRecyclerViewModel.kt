package io.github.kwvolt.japanesedictionary.presentation.dictionarylookup

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormDelete
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormSearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SimplifiedWordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.model.DictionaryLookupScreenState
import io.github.kwvolt.japanesedictionary.util.handleResultWithErrorCopy
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.parcelize.Parcelize

@OptIn(FlowPreview::class)
class DictionaryLookupRecyclerViewModel(
    private val _wordEntryFormBuilder: WordEntryFormBuilder,
    private val _wordEntryFormUpsert: WordEntryFormUpsert,
    private val _wordEntryFormDelete: WordEntryFormDelete,
    private val _wordEntryFormSearchFilter: WordEntryFormSearchFilter,
    private val _userFetcher: UserFetcher,
    private val _savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _uiState = MutableStateFlow(DictionaryLookupScreenState())
    val uiState: StateFlow<DictionaryLookupScreenState> = _uiState.asStateFlow()

    private var _paginationState: PaginationState
        get() = _savedStateHandle[PAGINATION_STATE_KEY] ?: PaginationState()
        set(value) = _savedStateHandle.set(PAGINATION_STATE_KEY, value)

    val paginationState: PaginationState get() = _paginationState
    private val _paginationMutex = Mutex()

    private val _scrollFlow = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    private val bookmarkToggleInProgress = mutableSetOf<Long>()
    private val glossToggleInProgress = mutableSetOf<Long>()

    init {
        viewModelScope.launch {
            _scrollFlow
                .debounce(REQUEST_NEW_PAGE_DEBOUNCE_MILLIS)
                .collect { pageSize ->
                    processAppendingNewEntriesFromId(pageSize)
                }
        }
    }

    fun toggleBookmark(dictionaryId: Long) {
        viewModelScope.launch {
            if (bookmarkToggleInProgress.contains(dictionaryId)) return@launch
            bookmarkToggleInProgress.add(dictionaryId)
            try {
                val formData = uiState.value.items.find { it.dictionaryId == dictionaryId } ?: return@launch
                val toggledBookmark = !formData.isBookmark
                val result = _wordEntryFormUpsert.updateIsBookmark(dictionaryId, toggledBookmark)
                _uiState.handleResultWithErrorCopy("toggleBookmark", result) {
                    val updatedList = uiState.value.items.map {
                        if (it.dictionaryId == dictionaryId) it.copy(isBookmark = toggledBookmark) else it
                    }
                    _uiState.update { it.copy(items = updatedList) }
                }
            } finally {
                bookmarkToggleInProgress.remove(dictionaryId)
            }
        }
    }

    fun autoComplete(){
        TODO("implement this later (potentially move to search filter)")
    }

    fun toggleGlossLayout(dictionaryId: Long) {
        if (glossToggleInProgress.contains(dictionaryId)) return
        glossToggleInProgress.add(dictionaryId)

        processToggleGlossLayout(dictionaryId)

        viewModelScope.launch {
            delay( TOGGLE_GLOSS_LAYOUT_DEBOUNCE_MILLIS) // block rapid repeat taps on same item
            glossToggleInProgress.remove(dictionaryId)
        }
    }

    private fun processToggleGlossLayout(dictionaryId: Long) {
        val formData = uiState.value.items.find { it.dictionaryId == dictionaryId } ?: return
        val isExpanded = !formData.isExpanded
        val updatedList = uiState.value.items.map {
            if (it.dictionaryId == dictionaryId) it.copy(isExpanded = isExpanded) else it
        }
        _uiState.update { it.copy(items = updatedList) }
    }

    suspend fun runSearch(searchTerm: String? = null, searchFilter: SearchFilter, pageSize: Int) {
        _uiState.update { it.copy(isLoading = true) }
        if (!searchTerm.isNullOrBlank()) {
            val result: DatabaseResult<List<Long>> = _wordEntryFormSearchFilter.searchBasedOnFilter(searchTerm, searchFilter)
            _uiState.handleResultWithErrorCopy("runSearch", result) { newIdList: List<Long> ->
                updatePaginationState { it.copy(
                    currentPage = START_POSITION,
                    ids = newIdList,
                    isLastPage = false,
                    isPaginating = false
                ) }
                if (newIdList.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, items = emptyList()) }
                } else {
                    requestNextPage(pageSize)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        } else {
            updatePaginationState { it.copy(START_POSITION, emptyList(), true) }
            _uiState.update { it.copy(isLoading = false, items = emptyList()) }
        }
    }


    suspend fun deleteEntry(dictionaryId: Long, pageSize: Int) {
        _uiState.update { it.copy(isLoading = true) }
        val result: DatabaseResult<Unit> =
            _wordEntryFormDelete.deleteWordEntryFormData(dictionaryId)
        _uiState.handleResultWithErrorCopy("deleteEntry", result) {
            val updatedIdList: List<Long> = paginationState.ids.filter { it != dictionaryId }
            val totalPages: Int = (updatedIdList.size + pageSize - 1) / pageSize
            val newCurrentPage = minOf(paginationState.currentPage, maxOf(totalPages - 1, 0))
            updatePaginationState { it.copy(currentPage = newCurrentPage, ids = updatedIdList) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun requestNextPage(pageSize: Int) {
        _scrollFlow.emit(pageSize)
    }

    private suspend fun processAppendingNewEntriesFromId(pageSize: Int){
        if (paginationState.isPaginating || paginationState.isLastPage) return
        val idList: List<Long> = paginationState.ids
        val currentPage = paginationState.currentPage
        val totalItems = idList.size
        val start = currentPage * pageSize
        val end = minOf(totalItems, start + pageSize)

        if (start >= totalItems) {
            updatePaginationState { it.copy(isLastPage = true, isPaginating = false) }
            return
        }
        updatePaginationState { it.copy(isLastPage = false, isPaginating = true) }

        // retrieve new items
        val subIdList: List<Long> = idList.subList(start, end)
        val appendItems: List<SimplifiedWordEntryFormData> = coroutineScope {
            subIdList.map { id ->
                async {
                    _wordEntryFormBuilder.buildSimplifiedWordFormData(_userFetcher, id).let { result ->
                        var data: SimplifiedWordEntryFormData? = null
                        _uiState.handleResultWithErrorCopy("appendNewEntriesFromIds", result) {
                            data = it
                        }
                        data
                    }
                }
            }.awaitAll().filterNotNull()
        }
        updatePaginationState { it.copy(isPaginating = false) }
        val currentItems = uiState.value.items
        val updatedItems: List<SimplifiedWordEntryFormData> = (currentItems + appendItems)
            .associateBy { it.dictionaryId } // Deduplicate
            .let { dedupedMap -> idList.mapNotNull { dedupedMap[it] } } // Keep original order

        val nextPage = currentPage + 1
        val isLastPage = (nextPage * pageSize) >= totalItems

        updatePaginationState {
            it.copy(
                currentPage = nextPage,
                isLastPage = isLastPage,
                isPaginating = false
            )
        }
        _uiState.update { it.copy(items = updatedItems) }
    }

    suspend fun clearState() {
        _uiState.value = DictionaryLookupScreenState()
        updatePaginationState {  PaginationState() }
    }

    companion object {
        private const val START_POSITION: Int = 0
        private const val PAGINATION_STATE_KEY = "PAGINATION_STATE_KEY"
        private const val TOGGLE_GLOSS_LAYOUT_DEBOUNCE_MILLIS: Long = 150
        private const val REQUEST_NEW_PAGE_DEBOUNCE_MILLIS: Long = 300
    }

    @Parcelize
    data class PaginationState(
        val currentPage: Int = START_POSITION,
        val ids: List<Long> = emptyList(),
        val isLastPage: Boolean = false,
        val isPaginating: Boolean = false
    ): Parcelable

    private suspend fun updatePaginationState(block: (PaginationState) -> PaginationState) {
        _paginationMutex.withLock {
            _paginationState = block(_paginationState)
        }
    }
}
