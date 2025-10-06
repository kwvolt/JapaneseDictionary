package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormValidation
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationResult
import io.github.kwvolt.japanesedictionary.domain.data.validation.formatValidationTypeToMessage
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordFormHandler
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.model.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.ui.model.FormScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError

class FormListValidatorManager(private val _wordEntryFormValidation: WordEntryFormValidation) {
    fun validateAndUpdateItem(
        item: DisplayItem,
        position: Int,
        formData: WordEntryFormData,
        currentState: FormScreenState,
        listManager: WordFormItemListManager
    ): FormScreenState {
        val updatedErrors = currentState.errors.toMutableMap()
        val updatedList = currentState.items.toMutableList()
        return handleValidationResult(item, formData, position, currentState, updatedErrors, updatedList, listManager)
    }

    fun revalidateEntireList(
        handler: WordFormHandler,
        currentItems: List<DisplayItem>,
        errorMap: MutableMap<ItemKey, ErrorMessage>,
        currentState: FormScreenState,
        listManager: WordFormItemListManager
    ): FormScreenState {
        val formData = handler.getWordEntryFormData()
        val newList = currentItems.toMutableList()
        var exceptionResult: ScreenStateUnknownError? = null

        val dirtyItemsWithIndices = getDirtyItemsAsMap(currentItems, errorMap)

        val updatedErrors = errorMap.toMutableMap()
        val updatedList = newList.toMutableList()
        for ((index: Int, uiItem: DisplayItem) in dirtyItemsWithIndices) {

            val updatedState = handleValidationResult(
                uiItem, formData, index, currentState, updatedErrors, updatedList, listManager
            )

            if (updatedState.screenStateUnknownError != null) {
                exceptionResult = updatedState.screenStateUnknownError
                break
            }
        }

        return if (exceptionResult != null) {
            currentState.copy(screenStateUnknownError = exceptionResult)
        } else {
            currentState.copy(items = updatedList, errors = updatedErrors)
        }
    }

    private fun getDirtyItemsAsMap(
        currentItems: List<DisplayItem>,
        updatedErrors: Map<ItemKey, ErrorMessage>
    ): Map<Int, DisplayItem> {
        return currentItems.asSequence()
            .mapIndexedNotNull { index, item ->
                val key = item.itemKey
                if (updatedErrors.containsKey(key)) {
                    index to item
                } else {
                    null
                }
            }.toMap()
    }

    private fun handleValidationResult(
        item: DisplayItem,
        wordEntryFormData: WordEntryFormData,
        position: Int,
        currentState: FormScreenState,
        updatedErrors: MutableMap<ItemKey, ErrorMessage>,
        updatedList: MutableList<DisplayItem>,
        listManager: WordFormItemListManager
    ): FormScreenState {
        return when (val result: ValidationResult<Unit> = validateItem(displayItem = item, wordEntryFormData = wordEntryFormData)) {
            is ValidationResult.InvalidInput -> {
                val newError = ErrorMessage(
                    errorMessage = formatValidationTypeToMessage(result.error),
                    isDirty = true
                )
                updatedErrors[item.itemKey] = newError
                val updatedItem = item.copyError(newError)
                currentState.copy(items = listManager.updateItemAt(updatedList, updatedItem, position), errors = updatedErrors)
            }

            is ValidationResult.Success -> {
                updatedErrors.remove(item.itemKey)
                val updatedItem = item.copyError(ErrorMessage())
                currentState.copy(items = listManager.updateItemAt(updatedList, updatedItem, position), errors = updatedErrors)
            }

            is ValidationResult.UnknownError -> {
                currentState.copy(screenStateUnknownError = ScreenStateUnknownError(result.exception))
            }

            else -> currentState.copy(
                screenStateUnknownError = ScreenStateUnknownError(
                    IllegalStateException("Invalid validation result").fillInStackTrace()
                )
            )
        }
    }

    private fun validateItem(displayItem: DisplayItem, wordEntryFormData: WordEntryFormData): ValidationResult<Unit> {
        return when(val baseItem = displayItem.item) {
            is SectionLabelItem -> ValidationResult.Success(Unit)
            is StaticLabelItem -> ValidationResult.Success(Unit)
            is TextItem -> validateTextItem(baseItem, wordEntryFormData).flatMap { ValidationResult.Success(Unit) }
            is WordClassItem -> ValidationResult.Success(Unit)
            else -> ValidationResult.Success(Unit)
        }
    }

    private fun validateTextItem(textItem: TextItem, wordEntryFormData: WordEntryFormData): ValidationResult<TextItem>{
        return when(textItem.inputTextType){
            InputTextType.PRIMARY_TEXT -> _wordEntryFormValidation.validatePrimaryText(textItem)
            InputTextType.MEANING -> _wordEntryFormValidation.validateMeaningText(textItem)
            InputTextType.KANA -> {
                val props =textItem.itemProperties as ItemSectionProperties
                val kanaList = wordEntryFormData.wordSectionMap[props.getSectionIndex()]?.getKanaInputMapAsList()
                if (kanaList != null) {
                    _wordEntryFormValidation.validateKana(textItem, kanaList)
                }
                else{
                    ValidationResult.NotFound
                }
            }
            InputTextType.DICTIONARY_NOTE_DESCRIPTION -> {
                _wordEntryFormValidation.validateNotes(textItem, wordEntryFormData.getEntryNoteMapAsList())
            }
            InputTextType.SECTION_NOTE_DESCRIPTION -> {
                val props = textItem.itemProperties as ItemSectionProperties
                val noteList = wordEntryFormData.wordSectionMap[props.getSectionIndex()]?.getComponentNoteInputMapAsList()
                if (noteList != null) {
                    _wordEntryFormValidation.validateNotes(textItem, noteList)
                }
                else {
                    ValidationResult.NotFound
                }
            }
        }
    }
}