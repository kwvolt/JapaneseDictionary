package io.github.kwvolt.japanesedictionary.ui.model

data class ActivityMainScreenState(
    val isLoading: Boolean,
    val screenStateUnknownError: ScreenStateUnknownError? = null
)