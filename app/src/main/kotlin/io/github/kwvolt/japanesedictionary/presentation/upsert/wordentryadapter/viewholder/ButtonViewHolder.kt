package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewButtonItemBinding
import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.BaseItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.ui.StringResourceFromFormKey

interface ButtonCallBack{
    fun buttonClickedHandler(button: ButtonAction, position: Int)
    fun getButtonKey(buttonItem: ButtonItem): FormKeys
    fun onUnexpectedError(exception: Throwable)
}

class ButtonViewHolder(private val binding: UpsertRecyclerviewButtonItemBinding, private val callBack: ButtonCallBack, private val upsertRecyclerCallBack: UpsertRecyclerCallBack) : UpsertViewHolder(binding.root, upsertRecyclerCallBack) {
    override fun bind(item: DisplayItem) {
        val baseItem: BaseItem = item.item
        if (baseItem is ButtonItem) {
            val buttonKey: FormKeys = callBack.getButtonKey(baseItem)
            setupButtonText(buttonKey)
            setupButtonClickListener(baseItem)
        }
        else {
            callBack.onUnexpectedError(IllegalStateException("Unknown item type: ${baseItem::class.java} in ButtonViewHolder").fillInStackTrace())
        }
    }

    // Setup the button's text using a resource string.
    private fun setupButtonText(buttonKey: FormKeys) {
        val buttonStringId: Int = StringResourceFromFormKey.getStringResource(buttonKey)
        binding.upsertButton.text = itemView.context.getString(buttonStringId)
    }

    // Set up the button's click listener.
    private fun setupButtonClickListener(button: ButtonItem) {
        binding.upsertButton.setOnClickListener {
            if(bindingAdapterPosition != RecyclerView.NO_POSITION){
                callBack.buttonClickedHandler(button.action, bindingAdapterPosition)
            }
        }
    }
}