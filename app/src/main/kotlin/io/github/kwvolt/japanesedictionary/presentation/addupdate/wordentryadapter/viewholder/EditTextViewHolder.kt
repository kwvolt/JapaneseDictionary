package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.text.InputType
import android.view.View.GONE
import android.view.View.VISIBLE
import io.github.kwvolt.japanesedictionary.databinding.EditTextItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder

interface EditTextCallBack{
    fun updateInputTextValue(inputTextItem: InputTextItem, inputText: String, position: Int)
    fun removeItemAtPosition(inputTextItem: InputTextItem, position: Int)
    fun getInputTextValue(inputTextItem: InputTextItem): String
    fun getInputTextType(inputTextItem: InputTextItem): InputTextType
}

class EditTextViewHolder(private val binding: EditTextItemBinding, private val callBack: EditTextCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        val inputTextItem: InputTextItem = baseItem as? InputTextItem ?: return
        val inputTextType: InputTextType = callBack.getInputTextType(inputTextItem)

        // Configure EditText based on inputTextType
        configureEditText(inputTextType)

        // Set up EditText listener for focus changes
        setupFocusListener(inputTextItem)

        // Update EditText text only when necessary
        updateEditTextText(inputTextItem)

        // Configure delete button visibility and click behavior
        configureDeleteButton(inputTextItem, inputTextType)
    }

    private fun setupFocusListener(inputTextItem: InputTextItem) {
        binding.addUpdateEditText.setOnFocusChangeListener { _, hasFocus ->
            val inputText = binding.addUpdateEditText.text.toString()
            if (!hasFocus) {
                // Update the value when focus is lost
                callBack.updateInputTextValue(inputTextItem, inputText, adapterPosition)
            }
        }
    }

    private fun updateEditTextText(inputTextItem: InputTextItem) {
        val textValue = callBack.getInputTextValue(inputTextItem)
        val currentText = binding.addUpdateEditText.text.toString()
        if (currentText != textValue) {
            binding.addUpdateEditText.setText(textValue)  // Update text if different
        }
    }

    private fun configureEditText(inputTextType: InputTextType) {
        val hint = getHintForInputType(inputTextType)
        binding.addUpdateEditText.apply {
            this.hint = hint
            when (inputTextType) {
                InputTextType.ENTRY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> {
                    this.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    this.isVerticalScrollBarEnabled = true
                    this.maxLines = 3
                }
                else -> {
                    this.inputType = InputType.TYPE_CLASS_TEXT
                    this.isVerticalScrollBarEnabled = false
                    this.maxLines = 1
                }
            }
        }
    }

    private fun configureDeleteButton(inputTextItem: InputTextItem, inputTextType: InputTextType) {
        val deleteButtonVisibility = when (inputTextType) {
            InputTextType.KANA, InputTextType.ENTRY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> VISIBLE
            else -> GONE
        }

        binding.addUpdateEditTextDelete.apply {
            visibility = deleteButtonVisibility
            setOnClickListener { callBack.removeItemAtPosition(inputTextItem, adapterPosition) }
        }
    }

    private fun getHintForInputType(inputTextType: InputTextType): String {
        return when (inputTextType) {
            InputTextType.PRIMARY_TEXT -> "Enter word"
            InputTextType.MEANING -> "Enter meaning"
            InputTextType.KANA -> "Enter hiragana/katakana spelling"
            InputTextType.ENTRY_NOTE_DESCRIPTION -> "Enter additional information on the specific details"
            InputTextType.SECTION_NOTE_DESCRIPTION -> "Enter additional information on the general details"
        }
    }
}