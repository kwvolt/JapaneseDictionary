package io.github.kwvolt.japanesedictionary.ui.model

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

data class FormScreenState(
    val items: List<BaseItem> = emptyList(),
    val errors: Map<ItemKey, ErrorMessage> = emptyMap(),
    val isLoading: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val screenStateUnknownError: ScreenStateUnknownError? = null
)

data class DisplayScreenState(
    val entry: WordEntryFormData? = null,
    val isLoading: Boolean = false,
    val screenStateUnknownError: ScreenStateUnknownError? = null,
    val hasUpdated: Boolean = false
)

data class ScreenStateUnknownError(val throwable: Throwable, val message: String?)