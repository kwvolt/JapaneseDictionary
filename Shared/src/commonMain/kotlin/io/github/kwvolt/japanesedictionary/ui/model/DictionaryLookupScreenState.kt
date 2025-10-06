package io.github.kwvolt.japanesedictionary.ui.model

import io.github.kwvolt.japanesedictionary.domain.model.SimplifiedWordEntryFormData

data class DictionaryLookupScreenState(
    val items: List<SimplifiedWordEntryFormData> = emptyList(),
    override val isLoading: Boolean = false,
    override val screenStateUnknownError: ScreenStateUnknownError? = null,
): ScreenState(isLoading, screenStateUnknownError), ScreenStateErrorCopyable<DictionaryLookupScreenState>{
    override fun copyWithError(error: ScreenStateUnknownError?): DictionaryLookupScreenState {
        return copy(screenStateUnknownError = error)
    }
}