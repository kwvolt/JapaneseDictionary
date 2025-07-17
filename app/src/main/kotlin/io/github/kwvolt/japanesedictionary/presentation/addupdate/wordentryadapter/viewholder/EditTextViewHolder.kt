package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.databinding.EditTextItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextHints
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder

interface EditTextCallBack{
    fun updateInputTextValue(inputTextFormUIItem: InputTextFormUIItem, inputText: String, position: Int)
    fun removeItemAtPosition(inputTextFormUIItem: InputTextFormUIItem, position: Int)
    fun getInputTextValue(inputTextItem: TextItem): String
    fun getInputTextType(inputTextItem: TextItem): InputTextType
    fun getErrorMessage(inputTextFormUIItem: InputTextFormUIItem): String?
}

class EditTextViewHolder(private val binding: EditTextItemBinding, private val callBack: EditTextCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        if (baseItem !is InputTextFormUIItem) return
        bindInputTextItem(baseItem)
    }

    private fun bindInputTextItem(item: InputTextFormUIItem) {
        val textItem = item.textItem
        val inputTextType = callBack.getInputTextType(textItem)

        configureEditText(inputTextType)
        setupFocusListener(item)
        updateEditTextText(textItem)
        configureDeleteButton(item, inputTextType)
        checkAndSetError(item)
    }

    private fun setupFocusListener(inputTextFormUIItem: InputTextFormUIItem) {
        binding.addUpdateEditText.setOnFocusChangeListener { _, hasFocus ->
            val inputText = binding.addUpdateEditText.text.toString()
            if (!hasFocus) {
                // Update the value when focus is lost
                println(inputTextFormUIItem.textItem.inputTextValue)
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    callBack.updateInputTextValue(inputTextFormUIItem, inputText, adapterPosition)
                }
            }
        }
    }

    private fun updateEditTextText(textItem: TextItem) {
        val newText = callBack.getInputTextValue(textItem)
        if (binding.addUpdateEditText.text.toString() != newText) {
            binding.addUpdateEditText.setText(newText)
        }
    }

    private fun configureEditText(inputTextType: InputTextType) {
        with(binding.addUpdateEditText) {
            hint = InputTextHints.forType(inputTextType)

            when (inputTextType) {
                InputTextType.ENTRY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                    movementMethod = ScrollingMovementMethod.getInstance()
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP -> {
                                v.parent.requestDisallowInterceptTouchEvent(false)
                                v.performClick()
                            }
                        }
                        false
                    }
                }
                else -> {
                    inputType = InputType.TYPE_CLASS_TEXT
                    imeOptions = EditorInfo.IME_ACTION_DONE
                    isSingleLine = true
                    maxLines = 1
                }
            }
        }
    }

    private fun configureDeleteButton(inputTextFormUIItem: InputTextFormUIItem, inputTextType: InputTextType) {
        val deleteButtonVisibility = when (inputTextType) {
            InputTextType.KANA, InputTextType.ENTRY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> VISIBLE
            else -> GONE
        }

        binding.addUpdateEditTextDelete.apply {
            visibility = deleteButtonVisibility
            setOnClickListener { callBack.removeItemAtPosition(inputTextFormUIItem, adapterPosition) }
        }
    }

    private fun checkAndSetError(inputTextFormUIItem: InputTextFormUIItem){
        val errorMessage = callBack.getErrorMessage(inputTextFormUIItem)
        if(!errorMessage.isNullOrEmpty()){
            binding.addUpdateEditTextLayout.isErrorEnabled = true
        }
        else{
            binding.addUpdateEditTextLayout.isErrorEnabled = false
        }
        binding.addUpdateEditTextLayout.error = errorMessage
    }
}