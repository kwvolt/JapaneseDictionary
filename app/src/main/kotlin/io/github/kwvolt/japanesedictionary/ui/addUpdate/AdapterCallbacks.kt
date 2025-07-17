package io.github.kwvolt.japanesedictionary.ui.addUpdate

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModel
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.WordClassCallBack


class InitWordClassCallBack(private val addUpdateViewModel: AddUpdateViewModel): WordClassCallBack {

    override fun updateMainClassId(
        wordClassFormUIItem: WordClassFormUIItem,
        selectionPosition: Int,
        position: Int
    ): Boolean {
        return addUpdateViewModel.updateMainClassId(wordClassFormUIItem, selectionPosition, position)
    }

    override fun updateSubClassId(
        wordClassFormUIItem: WordClassFormUIItem,
        selectionPosition: Int,
        position: Int
    ) {
        addUpdateViewModel.updateSubClassId(wordClassFormUIItem, selectionPosition, position)
    }

    override fun getMainClassListIndex(wordClassItem: WordClassItem): Int {
        return addUpdateViewModel.getMainClassListIndex(wordClassItem)
    }

    override fun getSubClassListIndex(wordClassItem: WordClassItem): Int {
        return addUpdateViewModel.getSubClassListIndex(wordClassItem)
    }

    override fun getMainClasList(): List<MainClassContainer> {
        return addUpdateViewModel.getMainClassList()
    }

    override fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer> {
        return addUpdateViewModel.getSubClassList(wordClassItem)
    }

    override fun getErrorMessage(wordClassFormUIItem: WordClassFormUIItem): String? {
        val errorMessage: ErrorMessage = wordClassFormUIItem.errorMessage
        if(errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }
}

class InitEditTextCallBack(private val addUpdateViewModel: AddUpdateViewModel): EditTextCallBack {
    override fun updateInputTextValue(
        inputTextFormUIItem: InputTextFormUIItem,
        inputText: String,
        position: Int
    ) {
        addUpdateViewModel.updateInputTextValue(inputTextFormUIItem, inputText, position)
    }

    override fun removeItemAtPosition(inputTextFormUIItem: InputTextFormUIItem, position: Int) {
        addUpdateViewModel.removeTextItemClicked(inputTextFormUIItem, position)
    }

    override fun getInputTextValue(inputTextItem: TextItem): String {
        return  inputTextItem.inputTextValue
    }

    override fun getInputTextType(inputTextItem: TextItem): InputTextType {
        return inputTextItem.inputTextType
    }


    override fun getErrorMessage(inputTextFormUIItem: InputTextFormUIItem): String? {
        val errorMessage: ErrorMessage = inputTextFormUIItem.errorMessage
        if(errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }
}

class InitLabelTextCallBack(private val addUpdateViewModel: AddUpdateViewModel): LabelTextCallBack {
    override fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int) {
        addUpdateViewModel.removeSectionClicked(entryLabelItem, position)
    }

    override fun getLabelType(labelItem: LabelItem): LabelHeaderType {
        return labelItem.labelHeaderType
    }

    override fun getWidgetName(namedItem: NamedItem): String {
        return namedItem.getDisplayText()
    }

    override fun getErrorMessage(staticLabelFormUIItem: StaticLabelFormUIItem): String? {
        val errorMessage: ErrorMessage = staticLabelFormUIItem.errorMessage
        if (errorMessage.isDirty){
            return errorMessage.errorMessage
        }
        return null
    }

}

class InitButtonCallBack(private val addUpdateViewModel: AddUpdateViewModel): ButtonCallBack {
    override fun buttonClickedHandler(button: ButtonAction, position: Int) {
        when (button){
            is ButtonAction.AddChild -> addUpdateViewModel.addChildTextItemClicked(button, position)
            is ButtonAction.AddItem -> addUpdateViewModel.addTextItemClicked(button, position)
            is ButtonAction.AddSection -> addUpdateViewModel.addSectionClicked(position)
            is ButtonAction.ValidateItem -> {
                val item = button.baseItem
                buttonClickedHandler(button.action, position)
                if(item.errorMessage.isDirty) {
                    // check validation for labels (used to indicate errors for the whole section)
                    if(item is StaticLabelFormUIItem){
                        addUpdateViewModel.validateStaticLabelFormUIItem(item)
                    }
                }
            }
        }
    }

    override fun getWidgetName(namedItem: NamedItem): String {
        return namedItem.getDisplayText()
    }
}