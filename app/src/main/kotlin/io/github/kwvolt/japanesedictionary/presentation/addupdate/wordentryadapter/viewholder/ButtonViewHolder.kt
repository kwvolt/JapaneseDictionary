package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.ButtonItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.getResourceString

interface ButtonCallBack{
    fun buttonClickedHandler(button: ButtonAction, position: Int)
    fun getWidgetName(namedItem: NamedItem): String
}

class ButtonViewHolder(private val binding: ButtonItemBinding, private val callBack: ButtonCallBack) : AddUpdateViewHolder(binding.root) {
    override fun bind(baseItem: BaseItem) {
        if (baseItem is ButtonItem) {
            val buttonName = callBack.getWidgetName(baseItem)
            setupButtonText(buttonName)
            setupButtonClickListener(baseItem)
        } else {
            handleInvalidType(baseItem)
        }
    }

    // Setup the button's text using a resource string.
    private fun setupButtonText(buttonName: String) {
        val buttonText = getResourceString(binding, buttonName, R.string.generic_add_button)
        binding.addUpdateButton.text = buttonText
    }

    // Set up the button's click listener.
    private fun setupButtonClickListener(button: ButtonItem) {
        binding.addUpdateButton.setOnClickListener {
            callBack.buttonClickedHandler(button.action, adapterPosition)
        }
    }

    // Handle invalid item type gracefully.
    private fun handleInvalidType(baseItem: BaseItem) {
        throw IllegalStateException("Unknown item type encountered: ${baseItem::class.java.simpleName} in ButtonViewHolder->bind()")
    }
}