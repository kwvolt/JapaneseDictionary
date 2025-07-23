package io.github.kwvolt.japanesedictionary.presentation.mainactivity

import androidx.lifecycle.ViewModel
import io.github.kwvolt.japanesedictionary.ui.model.ActivityMainScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LoadingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ActivityMainScreenState>(ActivityMainScreenState(false))
    val uiState: StateFlow<ActivityMainScreenState> get() = _uiState

    fun showLoading() {
        _uiState.update { it.copy(true) }
    }

    fun hideLoading() {
        _uiState.update { it.copy(false) }
    }

    fun isCurrentlyLoading(): Boolean {
        return _uiState.value.isLoading
    }

    fun showWarning(screenStateUnknownError: ScreenStateUnknownError) {
        _uiState.update { it.copy(true, screenStateUnknownError) }
    }
}