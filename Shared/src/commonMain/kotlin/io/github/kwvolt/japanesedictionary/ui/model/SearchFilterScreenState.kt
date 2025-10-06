package io.github.kwvolt.japanesedictionary.ui.model

data class SearchFilterScreenState(
    val idList: List<Long> = emptyList(),
    override val isLoading: Boolean = false,
    override val screenStateUnknownError: ScreenStateUnknownError? = null,
): ScreenState(isLoading, screenStateUnknownError), ScreenStateErrorCopyable<SearchFilterScreenState>{
    override fun copyWithError(error: ScreenStateUnknownError?): SearchFilterScreenState {
        return copy(screenStateUnknownError = error)
    }
}