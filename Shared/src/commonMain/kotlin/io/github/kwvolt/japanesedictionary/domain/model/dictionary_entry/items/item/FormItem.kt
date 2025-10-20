package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item

import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys

sealed class LabelItem(
    open val labelHeaderType: LabelHeaderType,
    override val itemProperties: GenericItemProperties
) : BaseItem(itemProperties)

data class StaticLabelItem(
    val formKeys: FormKeys,
    override val labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
    override val itemProperties: GenericItemProperties
) : LabelItem(labelHeaderType, itemProperties)

data class SectionLabelItem(
    val sectionCount: Int,
    override val labelHeaderType: LabelHeaderType = LabelHeaderType.HEADER,
    override val itemProperties: ItemSectionProperties,
) : LabelItem(labelHeaderType, itemProperties)

enum class LabelHeaderType{
    HEADER,
    SUB_HEADER
}

interface ClickableItem {
    val action: ButtonAction
}

data class ButtonItem(
    val formKeys: FormKeys,
    override val action: ButtonAction,
    override val itemProperties: GenericItemProperties
): BaseItem(itemProperties), ClickableItem

sealed class ButtonAction {
    data class AddTextItem(val inputTextType: InputTextType) : ButtonAction()
    data class AddTextChild(val inputTextType: InputTextType, val sectionId: Int) : ButtonAction()
    data object AddSection : ButtonAction()
    data class ValidateItem(val baseItem: DisplayItem, val action: ButtonAction) : ButtonAction()
}
