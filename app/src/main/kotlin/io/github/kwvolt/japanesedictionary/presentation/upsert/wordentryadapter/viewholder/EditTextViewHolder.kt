package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder

import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewEditTextItemBinding
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem

interface EditTextCallBack{
    fun updateInputTextValue(displayItem: DisplayItem.DisplayTextItem, inputText: String, position: Int)
    fun removeItemAtPosition(textItem: TextItem, position: Int)
    fun getInputTextValue(inputTextItem: TextItem): String
    fun getInputTextType(inputTextItem: TextItem): InputTextType
    fun getErrorMessage(errorMessage: ErrorMessage): String?
    fun onUnexpectedError(exception: Throwable)
}

class EditTextViewHolder(private val binding: UpsertRecyclerviewEditTextItemBinding, private val callBack: EditTextCallBack, private val upsertRecyclerCallBack: UpsertRecyclerCallBack) : UpsertViewHolder(binding.root, upsertRecyclerCallBack) {

    override fun bind(item: DisplayItem) {
        when (item) {
            is DisplayItem.DisplayTextItem -> {
                bindInputTextItem(item)
            }
            else -> callBack.onUnexpectedError(
                IllegalStateException("Unknown display item type: ${DisplayItem::class.java} in EditTextViewHolder").fillInStackTrace()
            )
        }

    }

    private fun bindInputTextItem(displayItem: DisplayItem.DisplayTextItem) {
        val textItem: TextItem = displayItem.item
        val inputTextType = callBack.getInputTextType(textItem)
        configureEditText(inputTextType)
        setupFocusListener(displayItem)
        updateEditTextText(textItem)
        configureDeleteButton(textItem, inputTextType)
        checkAndSetError(displayItem.errorMessage)
    }

    private fun setupFocusListener(item: DisplayItem.DisplayTextItem) {
        binding.upsertEditText.setOnFocusChangeListener { _, hasFocus ->
            val inputText: String = binding.upsertEditText.text.toString()
            if (!hasFocus) {
                // Update the value when focus is lost and text value is different
                val itemTextValue: String = callBack.getInputTextValue(item.item)
                if (adapterPosition != RecyclerView.NO_POSITION && itemTextValue != inputText) {
                    callBack.updateInputTextValue(item, inputText, adapterPosition)
                    upsertRecyclerCallBack.setHasUpdated()
                }
            }
        }
    }

    private fun updateEditTextText(textItem: TextItem) {
        val newText = callBack.getInputTextValue(textItem)
        if (binding.upsertEditText.text.toString() != newText) {
            val selection = binding.upsertEditText.selectionStart
            val safeSelection = selection.coerceIn(0, newText.length)
            binding.upsertEditText.setText(newText)
            binding.upsertEditText.setSelection(safeSelection)
        }
    }

    private fun configureEditText(inputTextType: InputTextType) {
        with(binding.upsertEditText) {
            hint = textHint(inputTextType)
            when (inputTextType) {
                InputTextType.DICTIONARY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> {
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

    private fun configureDeleteButton(textItem: TextItem, inputTextType: InputTextType) {
        val shouldShowDelete: Boolean = when (inputTextType) {
            InputTextType.KANA,
            InputTextType.DICTIONARY_NOTE_DESCRIPTION,
            InputTextType.SECTION_NOTE_DESCRIPTION -> true
            else -> false
        }

        binding.upsertEditTextLayout.apply {
            endIconMode = if (shouldShowDelete) TextInputLayout.END_ICON_CUSTOM else TextInputLayout.END_ICON_NONE
            isEndIconVisible = shouldShowDelete
            if (shouldShowDelete) {
                setEndIconOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        callBack.removeItemAtPosition(textItem, position)
                    }
                }
            } else {
                setEndIconOnClickListener(null)
            }
        }
    }

    private fun checkAndSetError(errorMessage: ErrorMessage){
        val message: String? = callBack.getErrorMessage(errorMessage)
        binding.upsertEditTextLayout.isErrorEnabled = !message.isNullOrEmpty()
        if (binding.upsertEditTextLayout.error != message) {
            binding.upsertEditTextLayout.error = message
        }
    }

    private fun textHint(type: InputTextType): String {
        val stringId: Int = when (type) {
            InputTextType.PRIMARY_TEXT -> R.string.upsert_edit_vh_primary_text_hint
            InputTextType.MEANING -> R.string.upsert_edit_vh_meaning_hint
            InputTextType.KANA -> R.string.upsert_edit_vh_kana_hint
            InputTextType.DICTIONARY_NOTE_DESCRIPTION -> R.string.upsert_edit_vh_entry_note_hint
            InputTextType.SECTION_NOTE_DESCRIPTION -> R.string.upsert_edit_vh_section_note_hint

        }
        return itemView.context.getString(stringId)
    }
}