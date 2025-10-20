package io.github.kwvolt.japanesedictionary.ui.model

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ErrorMessage

data class FormScreenState(
    val items: List<DisplayItem> = emptyList(),
    val errors: Map<ItemKey, ErrorMessage> = emptyMap(),
    override val isLoading: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    override val screenStateUnknownError: ScreenStateUnknownError? = null,
    val confirmed: Long? = null
): ScreenState(isLoading, screenStateUnknownError), ScreenStateErrorCopyable<FormScreenState>{
    override fun copyWithError(error: ScreenStateUnknownError?): FormScreenState {
        return copy(screenStateUnknownError = error)
    }
}

