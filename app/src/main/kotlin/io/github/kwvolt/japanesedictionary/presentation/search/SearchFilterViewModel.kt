package io.github.kwvolt.japanesedictionary.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchType
import io.github.kwvolt.japanesedictionary.ui.model.SearchFilterScreenState
import io.github.kwvolt.japanesedictionary.util.handleResultWithErrorCopy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

class SearchFilterViewModel(
    private val _wordClassDataManager: WordClassDataManager,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private var _searchFilter: SearchFilter
        get()  = savedStateHandle.get<SearchFilter>(KEY_SEARCH_FILTER) ?: SearchFilter(SearchType.ALL, false)
        set(value) = savedStateHandle.set(KEY_SEARCH_FILTER, value)
    val searchFilter: SearchFilter get() = _searchFilter

    private var _isExpanded: Boolean
        get()=savedStateHandle.get<Boolean>(KEY_TOGGLE_LAYOUT) ?: false
        set(value) = savedStateHandle.set(KEY_TOGGLE_LAYOUT, value)
    val isExpanded: Boolean get() = _isExpanded

    private val _uiState = MutableStateFlow(SearchFilterScreenState())
    val uiState: StateFlow<SearchFilterScreenState> get() = _uiState

    private val _onDropdownReady = MutableSharedFlow<Unit>(replay = 1)
    val onDropdownReady = _onDropdownReady.asSharedFlow()

    fun toggleIsExpanded(): Boolean{
        _isExpanded = !_isExpanded
        return _isExpanded
    }

    // retrieves Word Class values from database
    suspend fun loadWordClassSpinner(){
        _uiState.update { it.copy(isLoading = true) }
        val result = _wordClassDataManager.loadWordClassData()
        _uiState.handleResultWithErrorCopy("loadWordClassSpinner", result){
            _uiState.update { it.copy(isLoading = false) }
            _onDropdownReady.emit(Unit)
        }
    }

    fun getMainClassList(): List<MainClassContainer>{
        return _wordClassDataManager.getMainClassList()
    }

    fun getSubClassList(): List<SubClassContainer>{
        return _wordClassDataManager.getSubClassList(_searchFilter.mainClassId)
    }

    fun getMainClassIndex(): Int{
        return _wordClassDataManager.getMainClassListIndex(_searchFilter.mainClassId)
    }

    fun getSubClassIndex(): Int{
        return _wordClassDataManager.getSubClassListIndex(_searchFilter.mainClassId, _searchFilter.subClassId)
    }

    fun setMainClass(selectedPosition: Int): Boolean{
        val mainClassId = _wordClassDataManager.getMainClassId(selectedPosition)
        if(mainClassId != _searchFilter.mainClassId){
            _searchFilter = _searchFilter.copy(mainClassId = mainClassId)
            return true
        }
        return false
    }

    fun setSubClass(selectedPosition: Int){
        val subClassId: Long = _wordClassDataManager.getSubClassId(_searchFilter.mainClassId, selectedPosition)
        _searchFilter = _searchFilter.copy(subClassId = subClassId)
    }

    fun setSearchType(searchType: SearchType){
        _searchFilter = _searchFilter.copy(searchType = searchType)
    }

    fun setIsBookmark(isBookmark: Boolean){
        _searchFilter = _searchFilter.copy(isBookmark = isBookmark)
    }

    companion object {
        internal const val KEY_SEARCH_FILTER = "SEARCH_FILTER_KEY"
        private const val KEY_TOGGLE_LAYOUT = "TOGGLE_LAYOUT_KEY"
    }
}