package io.github.kwvolt.japanesedictionary.presentation.addupdate

import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordChildClassContainer
import io.github.kwvolt.japanesedictionary.databinding.ButtonItemBinding
import io.github.kwvolt.japanesedictionary.databinding.EditTextItemBinding
import io.github.kwvolt.japanesedictionary.databinding.LabelItemBinding
import io.github.kwvolt.japanesedictionary.databinding.WordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem


interface AddUpdateViewModelCallBack {

    // button click
    fun buttonClickedHandler(button: ButtonAction, position: Int)

    // remove items
    fun removeItemAtPosition(position: Int)
    fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int)

    // update items
    //word class
    fun updateMainClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int): Boolean
    fun updateSubClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int)
    //Text input
    fun updateInputTextValue(inputTextItem: InputTextItem, inputText: String, position: Int)

    // entry
    fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int)

    // get item values
    // word class
    fun getMainClassListIndex(wordClassItem: WordClassItem): Int
    fun getSubClassListIndex(wordClassItem: WordClassItem): Int
    fun getMainClasList(): List<MainClassContainer>
    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>

    // Text input
    fun getInputTextValue(inputTextItem: InputTextItem): String
    fun getInputTextType(inputTextItem: InputTextItem): InputTextType

    // label
    fun getLabelType(labelItem: LabelItem): LabelType

    // label, button
    fun getWidgetName(namedItem: NamedItem): String
}


class AddUpdateAdapter(
    val addUpdateViewModelCallBack: AddUpdateViewModelCallBack
) :
    ListAdapter<BaseItem, AddUpdateAdapter.AddUpdateViewHolder>(AddUpdateDiffUtilCallback()) {

    enum class ViewType {
        LABEL,
        WORD_CLASS,
        EDITTEXT,
        BUTTON,
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ItemButtonItem -> ViewType.BUTTON.ordinal
            is InputTextItem -> ViewType.EDITTEXT.ordinal
            is StaticLabelItem, is EntryLabelItem -> ViewType.LABEL.ordinal
            is WordClassItem -> ViewType.WORD_CLASS.ordinal
            else -> {
                throw IllegalStateException("Illegal BaseItem type ${getItem(position)::class.java} in AddUpdateAdapter->getItemViewType()")
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): AddUpdateViewHolder {
        return when (viewType){
            ViewType.LABEL.ordinal -> {
                val binding = LabelItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                LabelTextViewHolder(binding)
            }
            ViewType.EDITTEXT.ordinal -> {
                val binding = EditTextItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                EditTextViewHolder(binding)}
            ViewType.BUTTON.ordinal -> {
                val binding = ButtonItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                ButtonViewHolder(binding)
            }
            ViewType.WORD_CLASS.ordinal -> {
                val binding = WordClassItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                WordClassViewHolder(binding)
            }

            else -> {
                throw IllegalStateException("Unknown ViewType Int encountered: $viewType in AddUpdateAdapter->onCreateViewHolder()")
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: AddUpdateViewHolder, position: Int) {
        val item: BaseItem = getItem(position)
        viewHolder.bind(item, addUpdateViewModelCallBack)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = currentList.size

    abstract class AddUpdateViewHolder(view: View): RecyclerView.ViewHolder(view){
        abstract fun bind(baseItem: BaseItem, callBack: AddUpdateViewModelCallBack)
    }

    class WordClassViewHolder(private val binding: WordClassItemBinding) : AddUpdateViewHolder(binding.root) {

        override fun bind(baseItem: BaseItem, callBack: AddUpdateViewModelCallBack) {
            val wordClassItem = baseItem as? WordClassItem ?: return

            // Bind main class spinner with data and listener
            bindClassSpinner(binding.mainClassDrop, callBack.getMainClasList(), callBack.getMainClassListIndex(wordClassItem))
            { position ->
                val hasUpdated = callBack.updateMainClassId(wordClassItem, position, adapterPosition)
                bindSubClassSpinnerIfNeeded(wordClassItem, callBack, hasUpdated)  // Update sub-class spinner if main class changes
            }

            // Bind sub-class spinner with data and listener
            bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassItem), callBack.getSubClassListIndex(wordClassItem))
            { position ->
                callBack.updateSubClassId(wordClassItem, position, adapterPosition)
            }
        }

        // General function to bind a spinner with data and set the listener for updates
        private fun bindClassSpinner(
            spinner: Spinner,
            classList: List<WordChildClassContainer>, // Assuming ClassType is the type of your class objects
            selectedIndex: Int,
            onItemSelected: (Int) -> Unit
        ) {
            val spinnerAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item,
                classList.map { it.displayText }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

            spinner.adapter = spinnerAdapter
            spinner.setSelection(selectedIndex)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onItemSelected(position) // Trigger the appropriate update action
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Bind sub-class spinner only if the class data has changed
        private fun bindSubClassSpinnerIfNeeded(wordClassItem: WordClassItem, callBack: AddUpdateViewModelCallBack, hasUpdated: Boolean) {
            if (binding.subClassDrop.adapter == null || hasUpdated) {
                bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassItem), 0) // Default selection, could be adjusted based on your logic
                { position ->
                    callBack.updateSubClassId(wordClassItem, position, adapterPosition)
                }
            }
        }
    }

    class EditTextViewHolder(private val binding: EditTextItemBinding) : AddUpdateViewHolder(binding.root) {

        override fun bind(baseItem: BaseItem, callBack: AddUpdateViewModelCallBack) {
            val inputTextItem: InputTextItem = baseItem as? InputTextItem ?: return
            val inputTextType: InputTextType = callBack.getInputTextType(inputTextItem)

            // Configure EditText based on inputTextType
            configureEditText(inputTextType)

            // Set up EditText listener for focus changes
            setupFocusListener(inputTextItem, callBack)

            // Update EditText text only when necessary
            updateEditTextText(inputTextItem, callBack)

            // Configure delete button visibility and click behavior
            configureDeleteButton(inputTextType, callBack)
        }

        private fun setupFocusListener(inputTextItem: InputTextItem, callBack: AddUpdateViewModelCallBack) {
            binding.addUpdateEditText.setOnFocusChangeListener { _, hasFocus ->
                val inputText = binding.addUpdateEditText.text.toString()
                if (!hasFocus) {
                    // Update the value when focus is lost
                    callBack.updateInputTextValue(inputTextItem, inputText, adapterPosition)
                }
            }
        }

        private fun updateEditTextText(inputTextItem: InputTextItem, callBack: AddUpdateViewModelCallBack) {
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

        private fun configureDeleteButton(inputTextType: InputTextType, callBack: AddUpdateViewModelCallBack) {
            val deleteButtonVisibility = when (inputTextType) {
                InputTextType.KANA, InputTextType.ENTRY_NOTE_DESCRIPTION, InputTextType.SECTION_NOTE_DESCRIPTION -> VISIBLE
                else -> GONE
            }

            binding.addUpdateEditTextDelete.apply {
                visibility = deleteButtonVisibility
                setOnClickListener { callBack.removeItemAtPosition(adapterPosition) }
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

    class LabelTextViewHolder(private val binding: LabelItemBinding) : AddUpdateViewHolder(binding.root) {

        override fun bind(baseItem: BaseItem, callBack: AddUpdateViewModelCallBack) {
            if (baseItem is LabelItem) {
                // Process based on specific item type
                when (baseItem) {
                    is EntryLabelItem -> handleEntryLabelItem(baseItem, callBack)
                    is StaticLabelItem -> handleLabelItem(baseItem, callBack)
                }

                // Set the label text
                val labelName: String = callBack.getWidgetName(baseItem)
                binding.addUpdateLabel.text = getResourceString(binding, labelName, R.string.generic_label)
            } else {
                throw IllegalStateException("Unknown item type encountered: ${baseItem::class.java} in LabelTextViewHolder->bind()")
            }
        }

        private fun handleEntryLabelItem(item: EntryLabelItem, callBack: AddUpdateViewModelCallBack) {
            callBack.updateEntryIndexIfNeeded(item, adapterPosition)
            setupEntryLabelAppearance()
            setupEntryLabelDeleteAction(item, callBack)
        }

        private fun setupEntryLabelAppearance() {
            binding.apply {
                addUpdateLabel.setTextAppearance(R.style.TextAppearance_AppCompat_Headline1)
                addUpdateEditTextDelete.visibility = VISIBLE
            }
        }

        private fun setupEntryLabelDeleteAction(item: EntryLabelItem, callBack: AddUpdateViewModelCallBack) {
            binding.addUpdateEditTextDelete.setOnClickListener {
                callBack.entryRemoveItems(item, adapterPosition)
            }
        }

        private fun handleLabelItem(item: LabelItem, callBack: AddUpdateViewModelCallBack) {
            val labelType = callBack.getLabelType(item)
            val textAppearance = getTextAppearanceForLabelType(labelType)
            binding.addUpdateLabel.setTextAppearance(textAppearance)
            binding.addUpdateEditTextDelete.visibility = GONE
        }

        private fun getTextAppearanceForLabelType(labelType: LabelType): Int {
            return when (labelType) {
                LabelType.HEADER -> R.style.TextAppearance_AppCompat_Headline1
                LabelType.SUB_HEADER -> R.style.TextAppearance_AppCompat_Subhead
            }
        }
    }

    class ButtonViewHolder(private val binding: ButtonItemBinding) : AddUpdateViewHolder(binding.root) {

        override fun bind(baseItem: BaseItem, callBack: AddUpdateViewModelCallBack) {
            if (baseItem is ItemButtonItem) {
                val buttonName = callBack.getWidgetName(baseItem)
                setupButtonText(buttonName)
                setupButtonClickListener(baseItem, callBack)
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
        private fun setupButtonClickListener(button: ItemButtonItem, callBack: AddUpdateViewModelCallBack) {
            binding.addUpdateButton.setOnClickListener {
                callBack.buttonClickedHandler(button.action, adapterPosition)
            }
        }

        // Handle invalid item type gracefully.
        private fun handleInvalidType(baseItem: BaseItem) {
            throw IllegalStateException("Unknown item type encountered: ${baseItem::class.java.simpleName} in ButtonViewHolder->bind()")
        }
    }

}

private fun getResourceString(binding: ViewBinding, name: String, resource: Int): String {
    return try {
        binding.root.context.getString(resource, name)
    } catch (e: Exception) {
        Log.e("ResourceUtils", "Error fetching string resource $resource with name $name", e)
        name // Fallback to name if resource fetching fails
    }
}

class AddUpdateDiffUtilCallback: DiffUtil.ItemCallback<BaseItem>() {

    override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem.itemProperties.getIdentifier() == newItem.itemProperties.getIdentifier()
    }

    override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem == newItem
    }
}

