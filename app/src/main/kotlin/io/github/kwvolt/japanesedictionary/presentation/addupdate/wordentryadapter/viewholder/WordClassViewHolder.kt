package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.databinding.WordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordChildClassContainer
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder


interface WordClassCallBack{
    fun updateMainClassId(wordClassFormUIItem: WordClassFormUIItem, selectionPosition: Int, position: Int): Boolean
    fun updateSubClassId(wordClassFormUIItem: WordClassFormUIItem, selectionPosition: Int, position: Int)
    fun getMainClassListIndex(wordClassItem: WordClassItem): Int
    fun getSubClassListIndex(wordClassItem: WordClassItem): Int
    fun getMainClasList(): List<MainClassContainer>
    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>
    fun getErrorMessage(wordClassFormUIItem: WordClassFormUIItem): String?
}

class WordClassViewHolder(private val binding: WordClassItemBinding, private val callBack: WordClassCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        if (baseItem !is WordClassFormUIItem) return
        bindWordClassItem(baseItem)
    }


    private fun bindWordClassItem(item: WordClassFormUIItem) {
        val wordClassItem = item.wordClassItem

        // Bind main class spinner with data and listener
        bindClassSpinner(binding.mainClassDrop, callBack.getMainClasList(), callBack.getMainClassListIndex(wordClassItem), item)
        { position ->
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val hasUpdated = callBack.updateMainClassId(item, position, adapterPosition)
                bindSubClassSpinnerIfNeeded(
                    item,
                    hasUpdated
                )  // Update sub-class spinner if main class changes
            }
        }

        // Bind sub-class spinner with data and listener
        bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassItem), callBack.getSubClassListIndex(wordClassItem), item)
        { position ->
            if (adapterPosition != RecyclerView.NO_POSITION) {
                callBack.updateSubClassId(item, position, adapterPosition)
            }
        }

        checkAndSetError(item)
    }

    // General function to bind a spinner with data and set the listener for updates
    private fun bindClassSpinner(
        spinner: Spinner,
        classList: List<WordChildClassContainer>, // Assuming ClassType is the type of your class objects
        selectedIndex: Int,
        wordClassFormUIItem: WordClassFormUIItem,
        onItemSelected: (Int) -> Unit
    ) {
        val spinnerAdapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_spinner_item,
            classList.map { it.displayText }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner.adapter = spinnerAdapter
        spinner.safeSetSelection(selectedIndex)

        var isSpinnerInitialized = false

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return  // Skip first call triggered by initialization
                }

                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemSelected(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Bind sub-class spinner only if the class data has changed
    private fun bindSubClassSpinnerIfNeeded(wordClassFormUIItem: WordClassFormUIItem, hasUpdated: Boolean) {
        if (binding.subClassDrop.adapter == null || hasUpdated) {
            bindClassSpinner(binding.subClassDrop, callBack.getSubClassList(wordClassFormUIItem.wordClassItem), 0, wordClassFormUIItem) // Default selection, could be adjusted based on your logic
            { position ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    callBack.updateSubClassId(wordClassFormUIItem, position, adapterPosition)
                }
            }
        }
    }

    private fun checkAndSetError(wordClassFormUIItem: WordClassFormUIItem){
        val message = callBack.getErrorMessage(wordClassFormUIItem)
        binding.addUpdateWordClassError.apply {
            text = message.orEmpty()
            visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun Spinner.safeSetSelection(position: Int) {
        if (this.selectedItemPosition != position) {
            setSelection(position)
        }
    }
}