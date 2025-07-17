package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormService
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.FormItemValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.InputTextFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.StaticLabelFormValidator
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.itemValidation.WordClassFormValidator
import io.github.kwvolt.japanesedictionary.ui.model.FormScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError

class FormListValidatorManager {

    suspend fun <T : FormUIItem> validateAndUpdateItem(
        item: T,
        position: Int,
        formData: WordEntryFormData,
        validator: FormItemValidator<T>,
        currentState: FormScreenState,
        listManager: WordFormItemListManager
    ): FormScreenState {
        val (key, result) = validator.validate(item, formData)
        val updatedErrors = currentState.errors.toMutableMap()
        val updatedList = currentState.items.toMutableList()

        return handleValidationResult(key, result, item, position, currentState, updatedErrors, updatedList, listManager)
    }

    suspend fun revalidateEntireList(
        handler: WordFormHandler,
        currentItems: List<BaseItem>,
        errorMap: MutableMap<ItemKey, ErrorMessage>,
        wordFormService: WordFormService,
        currentState: FormScreenState,
        listManager: WordFormItemListManager
    ): FormScreenState {
        val formData = handler.getWordEntryFormData()
        val newList = currentItems.toMutableList()
        var exceptionResult: ScreenStateUnknownError? = null

        val dirtyItemsWithIndices = getDirtyItemsAsMap(currentItems, errorMap)

        for ((index, uiItem) in dirtyItemsWithIndices) {
            val (key, result) = when (uiItem) {
                is InputTextFormUIItem -> InputTextFormValidator(wordFormService).validate(uiItem, formData)
                is StaticLabelFormUIItem -> StaticLabelFormValidator(wordFormService).validate(uiItem, formData)
                is WordClassFormUIItem -> WordClassFormValidator(wordFormService).validate(uiItem, formData)
                else -> continue
            }

            val updatedErrors = errorMap.toMutableMap()
            val updatedList = newList.toMutableList()

            val updatedState = handleValidationResult(
                key, result, uiItem, index, currentState, updatedErrors, updatedList, listManager
            )

            if (updatedState.screenStateUnknownError != null) {
                exceptionResult = updatedState.screenStateUnknownError
                break
            }
        }

        return if (exceptionResult != null) {
            currentState.copy(screenStateUnknownError = exceptionResult)
        } else {
            currentState.copy(items = newList, errors = errorMap)
        }
    }

    private fun <T : FormUIItem> handleValidationResult(
        key: ItemKey,
        result: ValidationResult<Unit>,
        item: T,
        position: Int,
        currentState: FormScreenState,
        updatedErrors: MutableMap<ItemKey, ErrorMessage>,
        updatedList: MutableList<BaseItem>,
        listManager: WordFormItemListManager
    ): FormScreenState {
        return when (result) {
            is ValidationResult.InvalidInput -> {
                val newError = ErrorMessage(
                    errorMessage = formatValidationTypeToMessage(result.error),
                    isDirty = true
                )
                updatedErrors[key] = newError
                val updatedItem = item.withErrorMessage(newError)
                currentState.copy(items = listManager.updateItemAt(updatedList, updatedItem, position), errors = updatedErrors)
            }

            is ValidationResult.Success -> {
                updatedErrors.remove(key)
                val updatedItem = item.withErrorMessage(ErrorMessage())
                currentState.copy(items = listManager.updateItemAt(updatedList, updatedItem, position), errors = updatedErrors)
            }

            is ValidationResult.UnknownError -> {
                currentState.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message))
            }

            else -> currentState.copy(
                screenStateUnknownError = ScreenStateUnknownError(
                    IllegalStateException("Invalid validation result"),
                    "Invalid validation result"
                )
            )
        }
    }

    private fun getDirtyItemsAsMap(
        currentItems: List<BaseItem>,
        updatedErrors: Map<ItemKey, ErrorMessage>
    ): Map<Int, FormUIItem> {
        return currentItems.asSequence()
            .mapIndexedNotNull { index, item ->
                val key = getItemKey(item)
                if (key != null && updatedErrors.containsKey(key) && item is FormUIItem) {
                    index to item
                } else {
                    null
                }
            }.toMap()
    }

    private fun getItemKey(item: BaseItem): ItemKey? = when (item) {
        is InputTextFormUIItem -> ItemKey.DataItem(item.textItem.itemProperties.getIdentifier())
        is StaticLabelFormUIItem -> (item.itemProperties as? ItemSectionProperties)?.let {
            ItemKey.FormItem(FormKeys.kanaLabel(it.getSectionIndex()))
        }
        is WordClassFormUIItem -> ItemKey.DataItem(item.wordClassItem.itemProperties.getIdentifier())
        else -> null
    }
}