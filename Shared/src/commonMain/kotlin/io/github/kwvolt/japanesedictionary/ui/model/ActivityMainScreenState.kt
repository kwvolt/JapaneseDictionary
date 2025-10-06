package io.github.kwvolt.japanesedictionary.ui.model

data class ActivityMainScreenState(
    override val isLoading: Boolean,
    override val screenStateUnknownError: ScreenStateUnknownError? = null
): ScreenState(isLoading, screenStateUnknownError), ScreenStateErrorCopyable<ActivityMainScreenState> {
    override fun copyWithError(error: ScreenStateUnknownError?): ActivityMainScreenState {
        return copy(screenStateUnknownError = error)
    }
}