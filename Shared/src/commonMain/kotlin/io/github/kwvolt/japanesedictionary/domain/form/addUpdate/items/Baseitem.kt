package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer

sealed class BaseItem(open val itemProperties: GenericItemProperties)

interface DisplayLabelText{
    fun getDisplayText(): String
}

abstract class NamedItem(
    private val name: String,
    override val itemProperties: GenericItemProperties
): BaseItem(itemProperties), DisplayLabelText {
    override fun getDisplayText(): String = name
}

sealed class LabelItem(
    open val name: String,
    open val labelHeaderType: LabelHeaderType,
    override val itemProperties: GenericItemProperties
) : NamedItem(name, itemProperties)

data class StaticLabelItem(
    override val name: String,
    override val labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
    override val itemProperties: GenericItemProperties
) : LabelItem(name, labelHeaderType, itemProperties)

data class EntryLabelItem(
    override val name: String = "Section",
    val sectionCount: Int,
    override val labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
    override val itemProperties: ItemSectionProperties,
) : LabelItem(name, labelHeaderType, itemProperties) {
    override fun getDisplayText(): String {
        return "$name $sectionCount"
    }
}

interface ClickableItem {
    val action: ButtonAction
}

sealed class ButtonAction {
    data class AddItem(val inputType: InputTextType) : ButtonAction()
    data class AddChild(val inputTextType: InputTextType, val entryLabelItem: EntryLabelItem) : ButtonAction()
    data object AddSection : ButtonAction()
    data class ValidateItem(val baseItem: FormUIItem, val action: ButtonAction) : ButtonAction()
}

data class ButtonItem(
    private val name: String,
    override val action: ButtonAction,
    override val itemProperties: GenericItemProperties
): NamedItem(name, itemProperties), ClickableItem

// User inputs
data class WordClassItem(
    val chosenMainClass: MainClassContainer,
    val chosenSubClass: SubClassContainer,
    override val itemProperties: GenericItemProperties,
) : BaseItem(itemProperties){
}

data class TextItem(
    val inputTextType: InputTextType,
    val inputTextValue: String = "",
    override val itemProperties: GenericItemProperties
) : BaseItem(itemProperties)

sealed class FormUIItem(itemProperties: GenericItemProperties) : BaseItem(itemProperties) {
    abstract val errorMessage: ErrorMessage
    abstract fun withErrorMessage(errorMessage: ErrorMessage): FormUIItem
}

data class InputTextFormUIItem(
    val textItem: TextItem,
    override val errorMessage: ErrorMessage,
) : FormUIItem(textItem.itemProperties){
    override fun withErrorMessage(errorMessage: ErrorMessage) = copy(errorMessage = errorMessage)
}

data class WordClassFormUIItem(
    val wordClassItem: WordClassItem,
    override val errorMessage: ErrorMessage
) : FormUIItem(wordClassItem.itemProperties){
    override fun withErrorMessage(errorMessage: ErrorMessage) = copy(errorMessage = errorMessage)
}

data class StaticLabelFormUIItem(
    val staticLabelItem: StaticLabelItem,
    override val errorMessage: ErrorMessage
) : FormUIItem(staticLabelItem.itemProperties){
    override fun withErrorMessage(errorMessage: ErrorMessage) = copy(errorMessage = errorMessage)
}

// Enums to indicate type of input item
enum class InputTextType {
    PRIMARY_TEXT,
    MEANING,
    KANA,
    ENTRY_NOTE_DESCRIPTION,
    SECTION_NOTE_DESCRIPTION
}

enum class LabelHeaderType{
    HEADER,
    SUB_HEADER
}

data class ErrorMessage(val errorMessage: String? = null, val isDirty: Boolean = false)
