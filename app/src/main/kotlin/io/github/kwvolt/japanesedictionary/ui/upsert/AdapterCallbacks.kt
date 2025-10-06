package io.github.kwvolt.japanesedictionary.ui.upsert

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.LabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.presentation.upsert.UpsertViewModel
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.WordClassCallBack


class InitWordClassCallBack(private val upsertViewModel: UpsertViewModel): WordClassCallBack {

    override fun updateMainClassId(
        displayItem: DisplayItem.DisplayWordClassItem,
        selectionPosition: Int,
        position: Int
    ): Boolean {
        return upsertViewModel.updateMainOrSubClassIdForWordClass(displayItem, selectionPosition, position, true)
    }

    override fun updateSubClassId(
        displayItem: DisplayItem.DisplayWordClassItem,
        selectionPosition: Int,
        position: Int
    ) {
        upsertViewModel.updateMainOrSubClassIdForWordClass(displayItem, selectionPosition, position, false)
    }

    override fun getMainClassListIndex(wordClassItem: WordClassItem): Int {
        return upsertViewModel.getMainClassListIndex(wordClassItem)
    }

    override fun getSubClassListIndex(wordClassItem: WordClassItem): Int {
        return upsertViewModel.getSubClassListIndex(wordClassItem)
    }

    override fun getMainClasList(): List<MainClassContainer> {
        return upsertViewModel.getMainClassList()
    }

    override fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer> {
        return upsertViewModel.getSubClassList(wordClassItem)
    }

    override fun getErrorMessage(errorMessage: ErrorMessage): String? {
        if(errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }

    override fun onUnexpectedError(exception: Throwable) {
        upsertViewModel.reportUnknownError(exception)
    }
}

class InitEditTextCallBack(private val upsertViewModel: UpsertViewModel): EditTextCallBack {
    override fun updateInputTextValue(
        displayItem: DisplayItem.DisplayTextItem,
        inputText: String,
        position: Int
    ) {
        upsertViewModel.updateInputTextValue(displayItem, inputText, position)
    }

    override fun removeItemAtPosition(textItem: TextItem, position: Int) {
        upsertViewModel.removeTextItemClicked(textItem, position)
    }

    override fun getInputTextValue(inputTextItem: TextItem): String {
        return  inputTextItem.inputTextValue
    }

    override fun getInputTextType(inputTextItem: TextItem): InputTextType {
        return inputTextItem.inputTextType
    }


    override fun getErrorMessage(errorMessage: ErrorMessage): String? {
        if(errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }

    override fun onUnexpectedError(exception: Throwable) {
        upsertViewModel.reportUnknownError(exception)
    }
}

class InitLabelTextCallBack(private val upsertViewModel: UpsertViewModel): LabelTextCallBack {
    override fun removeSection(sectionLabelItem: SectionLabelItem, position: Int) {
        upsertViewModel.removeSectionClicked(sectionLabelItem, position)
    }

    override fun getLabelType(labelItem: LabelItem): LabelHeaderType {
        return labelItem.labelHeaderType
    }

    override fun getLabelKey(labelItem: StaticLabelItem): FormKeys {
        return labelItem.formKeys
    }

    override fun getSectionCount(sectionLabelItem: SectionLabelItem): Int {
        return sectionLabelItem.sectionCount
    }

    override fun getErrorMessage(errorMessage: ErrorMessage): String? {
        if (errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }

    override fun onUnexpectedError(exception: Throwable) {
        upsertViewModel.reportUnknownError(exception)
    }

}

class InitButtonCallBack(private val upsertViewModel: UpsertViewModel): ButtonCallBack {
    override fun buttonClickedHandler(button: ButtonAction, position: Int) {
        when (button){
            is ButtonAction.AddTextChild -> {
                upsertViewModel.addTextItemClicked(
                    button.inputTextType,
                    button.sectionId,
                    position
                )
            }
            is ButtonAction.AddTextItem -> upsertViewModel.addTextItemClicked(button.inputTextType, position = position)
            is ButtonAction.AddSection -> upsertViewModel.addSectionClicked(position)
            is ButtonAction.ValidateItem -> {
                if (button.action is ButtonAction.ValidateItem) {
                    throw IllegalArgumentException("Nested ValidateItem is not supported")
                }

                TODO("Implement validation if needed")

                // validation function here if needed
                val item = button.baseItem
                buttonClickedHandler(button.action, position)
                }
            }
        }

    override fun getButtonKey(buttonItem: ButtonItem): FormKeys {
        return buttonItem.formKeys
    }

    override fun onUnexpectedError(exception: Throwable) {
        upsertViewModel.reportUnknownError(exception)
    }
}