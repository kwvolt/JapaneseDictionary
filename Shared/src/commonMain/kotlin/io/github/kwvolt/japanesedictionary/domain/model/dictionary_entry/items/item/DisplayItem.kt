package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey

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
    data class DisplayConjugationTemplateItem(val errorMessage: ErrorMessage, override val item: ConjugationTemplateItem, override val itemKey: ItemKey.DataItem): DisplayItem(){
        override val viewType: Int = RecyclerViewType.CONJUGATION_TEMPLATE.id
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
            is DisplayConjugationTemplateItem -> this.copy(errorMessage = newErrorMessage ?: this.errorMessage)
        }
    }

    enum class RecyclerViewType(val id: Int){
        TEXT(0),
        WORD_CLASS(1),
        CONJUGATION_TEMPLATE(2),
        LABEL(3),
        BUTTON(4)
    }
}

data class ErrorMessage(val errorMessage: String? = null, val isDirty: Boolean = false)