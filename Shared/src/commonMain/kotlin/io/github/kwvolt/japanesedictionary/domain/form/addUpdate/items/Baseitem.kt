package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer

sealed class BaseItem(open val itemProperties: GenericItemProperties) {
}

interface DisplayLabelText{
    fun getDisplayText(): String
}

abstract class NamedItem(
    private val name: String = "",
    override val itemProperties: GenericItemProperties
): BaseItem(itemProperties), DisplayLabelText {
    override fun getDisplayText(): String = name
}

sealed class LabelItem(
    open val name: String = "",
    open val labelType: LabelType,
    override val itemProperties: GenericItemProperties
) : NamedItem(name, itemProperties)

data class StaticLabelItem(
    override val name: String = "",
    override val labelType: LabelType = LabelType.HEADER,
    override val itemProperties: GenericItemProperties
) : LabelItem(name, labelType, itemProperties)

data class EntryLabelItem(
    override val name: String = "",
    val sectionCount: Int = 0,
    override val labelType: LabelType = LabelType.HEADER,
    override val itemProperties: ItemSectionProperties
) : LabelItem(name, labelType, itemProperties) {
    override fun getDisplayText(): String {
        return "$name: $sectionCount"
    }
}

interface ClickableItem {
    val action: ButtonAction
}

sealed class ButtonAction {
    data class AddItem(val inputType: InputTextType) : ButtonAction()
    data class AddChild(val inputTextType: InputTextType, val entryLabelItem: EntryLabelItem) : ButtonAction()
    data object AddSection : ButtonAction()
}

data class ItemButtonItem(
    private val name: String,
    override val action: ButtonAction,
    override val itemProperties: GenericItemProperties
): NamedItem(name, itemProperties), ClickableItem

// User inputs
data class WordClassItem(
    val chosenMainClassId: Long = -1,
    val chosenSubClassId: Long = -1,
    val currentSubClassData: List<SubClassContainer> = listOf(),
    override val itemProperties: GenericItemProperties,
) : BaseItem(itemProperties){
}

data class InputTextItem(
    val inputTextType: InputTextType,
    val inputTextValue: String = "",
    override val itemProperties: GenericItemProperties,
) : BaseItem(itemProperties){
}

// Enums to indicate type of input item
enum class InputTextType {
    PRIMARY_TEXT,
    MEANING,
    KANA,
    ENTRY_NOTE_DESCRIPTION,
    SECTION_NOTE_DESCRIPTION
}

enum class LabelType{
    HEADER,
    SUB_HEADER
}
