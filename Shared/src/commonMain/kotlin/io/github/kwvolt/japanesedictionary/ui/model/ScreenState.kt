package io.github.kwvolt.japanesedictionary.ui.model

sealed class ScreenState(
    open val isLoading: Boolean,
    open val screenStateUnknownError: ScreenStateUnknownError? = null) {
}

data class ScreenStateUnknownError(val throwable: Throwable, val message: String? = null)

interface ScreenStateErrorCopyable<T> {
    fun copyWithError(error: ScreenStateUnknownError?): T
}