package io.github.kwvolt.japanesedictionary.domain.model.items.item

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey

sealed class DisplayItem{
    abstract val item: BaseItem
    abstract val itemKey: ItemKey
    abstract val viewType: Int

    data class DisplayTextItem(val errorMessage: ErrorMessage, override val item: TextItem, override val itemKey: ItemKey.DataItem): DisplayItem() {
        override val viewType: Int = RecyclerViewType.TEXT.id
    }
    data class DisplayWordClassItem(val errorMessage: ErrorMessage, override val item: WordClassItem, override val itemKey: ItemKey.DataItem): DisplayItem(){
        override val viewType: Int = RecyclerViewType.WORD_CLASS.id
    }
    data class DisplayLabelItem(val errorMessage: ErrorMessage? = null, override val item: LabelItem, override val itemKey: ItemKey.FormItem): DisplayItem(){
        override val viewType: Int = RecyclerViewType.LABEL.id
    }
    data class DisplayButtonItem(override val item: ButtonItem, override val itemKey: ItemKey.FormItem): DisplayItem(){
        override val viewType: Int = RecyclerViewType.BUTTON.id
    }

    fun copyError(newErrorMessage: ErrorMessage?=null): DisplayItem {
        return when (this) {
            is DisplayTextItem -> this.copy(errorMessage = newErrorMessage ?: this.errorMessage)
            is DisplayWordClassItem -> this.copy(errorMessage = newErrorMessage ?: this.errorMessage)
            is DisplayLabelItem -> this.copy(errorMessage = newErrorMessage)
            is DisplayButtonItem -> this
        }
    }

    enum class RecyclerViewType(val id: Int){
        TEXT(0),
        WORD_CLASS(1),
        LABEL(2),
        BUTTON(3)
    }
}

data class ErrorMessage(val errorMessage: String? = null, val isDirty: Boolean = false)