package io.github.kwvolt.japanesedictionary.util

import android.content.Context
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordChildClassContainer

object WordClassBindUtil {
    fun bindWordClassItem(
        context: Context,
        mainClassDrop: MaterialAutoCompleteTextView,
        subClassDrop: MaterialAutoCompleteTextView,
        onItemSelectedInMainClass: (selectedIndex: Int, bindSubClassSpinner: (Boolean) -> Unit) -> Unit,
        onItemSelectedInSubClass: (Int) -> Unit,
        getMainClassList: () -> List<WordChildClassContainer>,
        getSubClassList: () -> List<WordChildClassContainer>,
        getMainClassIndex: () -> Int,
        getSubClassIndex: () -> Int
    ) {
        val bindSubClassSpinner: (Boolean) -> Unit = { hasUpdated ->
            bindSubClassSpinnerIfNeeded(
                context = context,
                subClassDropDown = subClassDrop,
                hasUpdated = hasUpdated,
                onItemSelected = onItemSelectedInSubClass,
                getSubClassList = getSubClassList,
                getSubClassListIndex = getSubClassIndex
            )
        }

        val wrappedMainClassSelection: (Int) -> Unit = { selectedIndex ->
            onItemSelectedInMainClass(selectedIndex, bindSubClassSpinner)
        }

        // Bind main class spinner
        bindSpinner(
            context = context,
            spinner = mainClassDrop,
            itemList = getMainClassList(),
            initialSelectedIndex = getMainClassIndex(),
            onItemSelected = wrappedMainClassSelection
        )

        // Bind sub class spinner
        bindSpinner(
            context = context,
            spinner = subClassDrop,
            itemList = getSubClassList(),
            initialSelectedIndex = getSubClassIndex(),
            onItemSelected = onItemSelectedInSubClass
        )
    }

    fun bindSpinner(
        context: Context,
        spinner: MaterialAutoCompleteTextView,
        itemList: List<WordChildClassContainer>,
        initialSelectedIndex: Int,
        onItemSelected: (Int) -> Unit
    ) {
        val displayTextList = itemList.map { it.displayText }

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            displayTextList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner.setAdapter(adapter)

        var lastSelectedIndex = initialSelectedIndex
        spinner.safeSetSelection(initialSelectedIndex, itemList)

        spinner.setOnItemClickListener { _, _, position, _ ->
            if (position != lastSelectedIndex) {
                lastSelectedIndex = position
                onItemSelected(position)
                spinner.clearFocus()
            }
        }

        spinner.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputText = spinner.text.toString()
                val matchIndex = displayTextList.indexOf(inputText)
                if (matchIndex != -1 && matchIndex != lastSelectedIndex) {
                    lastSelectedIndex = matchIndex
                    spinner.safeSetSelection(matchIndex, itemList)
                    onItemSelected(matchIndex)
                } else {
                    if (displayTextList.isNotEmpty()) {
                        val fallbackIndex = if (lastSelectedIndex in displayTextList.indices) lastSelectedIndex else 0
                        spinner.safeSetSelection(fallbackIndex, itemList)
                    } else {
                        spinner.setText("", false)
                    }
                }
            }
        }
    }

    fun bindSubClassSpinnerIfNeeded(
        context: Context,
        subClassDropDown: MaterialAutoCompleteTextView,
        hasUpdated: Boolean,
        onItemSelected: (Int) -> Unit,
        getSubClassList: () -> List<WordChildClassContainer>,
        getSubClassListIndex: () -> Int
    ) {
        if (subClassDropDown.adapter == null || hasUpdated) {
            val subClassList = getSubClassList()
            val subClassIndex = getSubClassListIndex()

            bindSpinner(
                context = context,
                spinner = subClassDropDown,
                itemList = subClassList,
                initialSelectedIndex = subClassIndex,
                onItemSelected = onItemSelected
            )
        }
    }

    private fun MaterialAutoCompleteTextView.safeSetSelection(
        position: Int,
        items: List<WordChildClassContainer>
    ) {
        val newText = items.getOrNull(position)?.displayText.orEmpty()
        if (this.text.toString() != newText) {
            setText(newText, false) // Avoid triggering filters
        }
    }
}