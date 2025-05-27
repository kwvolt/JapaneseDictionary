package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import io.github.kwvolt.japanesedictionary.databinding.WordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordChildClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder


interface WordClassCallBack{
    fun updateMainClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int): Boolean
    fun updateSubClassId(wordClassItem: WordClassItem, selectionPosition: Int, position: Int)
    fun getMainClassListIndex(wordClassItem: WordClassItem): Int
    fun getSubClassListIndex(wordClassItem: WordClassItem): Int
    fun getMainClasList(): List<MainClassContainer>
    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>
    fun getHasError(errorMessage: ErrorMessage): Boolean
    fun getErrorMessage(errorMessage: ErrorMessage): String
}

class WordClassViewHolder(private val binding: WordClassItemBinding, private val callBack: WordClassCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        val wordClassFormUiItem = baseItem as? WordClassFormUIItem ?: return
        val wordClassItem: WordClassItem = wordClassFormUiItem.wordClassItem

        // Bind main class spinner with data and listener
        bindClassSpinner(binding.mainClassDrop, callBack.getMainClasList(), callBack.getMainClassListIndex(wordClassItem))
        { position ->
            val hasUpdated = callBack.updateMainClassId(wordClassItem, position, adapterPosition)
            bindSubClassSpinnerIfNeeded(wordClassItem, hasUpdated)  // Update sub-class spinner if main class changes
        }

        // Bind sub-class spinner with data and listener
        bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassItem), callBack.getSubClassListIndex(wordClassItem))
        { position ->
            callBack.updateSubClassId(wordClassItem, position, adapterPosition)
        }

        checkAndSetError(wordClassFormUiItem.errorMessage)
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

        var isSpinnerInitialized = false

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return  // Skip first call triggered by initialization
                }
                onItemSelected(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Bind sub-class spinner only if the class data has changed
    private fun bindSubClassSpinnerIfNeeded(wordClassItem: WordClassItem, hasUpdated: Boolean) {
        if (binding.subClassDrop.adapter == null || hasUpdated) {
            bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassItem), 0) // Default selection, could be adjusted based on your logic
            { position ->
                callBack.updateSubClassId(wordClassItem, position, adapterPosition)
            }
        }
    }

    private fun checkAndSetError(errorMessage: ErrorMessage){
        if(callBack.getHasError(errorMessage)){
            val message: String = callBack.getErrorMessage(errorMessage)
            if(message.isNotBlank()){
                binding.addUpdateWordClassError.text = message
            }
        }
    }
}