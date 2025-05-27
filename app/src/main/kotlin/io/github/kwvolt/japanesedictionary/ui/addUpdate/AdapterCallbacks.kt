package io.github.kwvolt.japanesedictionary.ui.addUpdate

import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModel
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.WordClassCallBack


class InitWordClassCallBack(private val addUpdateViewModel: AddUpdateViewModel): WordClassCallBack {

    override fun updateMainClassId(
        wordClassItem: WordClassItem,
        selectionPosition: Int,
        position: Int
    ): Boolean {
        return addUpdateViewModel.updateMainClassId(wordClassItem, selectionPosition, position)
    }

    override fun updateSubClassId(
        wordClassItem: WordClassItem,
        selectionPosition: Int,
        position: Int
    ) {
        addUpdateViewModel.updateSubClassId(wordClassItem, selectionPosition, position)
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

    override fun getHasError(identifier: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getErrorMessage(identifier: String): String {
        TODO("Not yet implemented")
    }
}

class InitEditTextCallBack(private val addUpdateViewModel: AddUpdateViewModel): EditTextCallBack {
    override fun updateInputTextValue(
        inputTextItem: InputTextItem,
        inputText: String,
        position: Int
    ) {
        addUpdateViewModel.updateInputTextValue(inputTextItem, inputText, position)
    }

    override fun removeItemAtPosition(inputTextItem: InputTextItem, position: Int) {
        addUpdateViewModel.removeTextItemClicked(inputTextItem, position)
    }

    override fun getInputTextValue(inputTextItem: InputTextItem): String {
        return  inputTextItem.inputTextValue
    }

    override fun getInputTextType(inputTextItem: InputTextItem): InputTextType {
        return inputTextItem.inputTextType
    }

    override fun getHasError(identifier: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getErrorMessage(identifier: String): String {
        TODO("Not yet implemented")
    }

}

class InitLabelTextCallBack(private val addUpdateViewModel: AddUpdateViewModel): LabelTextCallBack {
    override fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int) {
        addUpdateViewModel.removeSectionClicked(entryLabelItem, position)
    }

    override fun getLabelType(labelItem: LabelItem): LabelType {
        return labelItem.labelType
    }

    override fun getWidgetName(namedItem: NamedItem): String {
        return namedItem.getDisplayText()
    }

    override fun getHasError(identifier: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getErrorMessage(identifier: String): String {
        TODO("Not yet implemented")
    }

}

class InitButtonCallBack(private val addUpdateViewModel: AddUpdateViewModel): ButtonCallBack {
    override fun buttonClickedHandler(button: ButtonAction, position: Int) {
        when (button){
            is ButtonAction.AddChild -> addUpdateViewModel.addChildTextItemClicked(button, position)
            is ButtonAction.AddItem -> addUpdateViewModel.addTextItemClicked(button, position)
            is ButtonAction.AddSection -> addUpdateViewModel.addSectionClicked(position)
        }
    }

    override fun getWidgetName(namedItem: NamedItem): String {
        return namedItem.getDisplayText()
    }
}