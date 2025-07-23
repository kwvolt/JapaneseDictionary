package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class WordEntryFormHandler(
    private var _wordEntryFormData: WordEntryFormData,
    private val _formItemManager: FormItemManager
) {
    val wordEntryFormData: WordEntryFormData get() = _wordEntryFormData.copy()

    fun replacePrimaryText(primaryItem: TextItem){
        _wordEntryFormData = wordEntryFormData.copy(primaryTextInput = primaryItem)
    }

    fun replaceEntryNote(entryNoteItem: TextItem){
        _wordEntryFormData = wordEntryFormData.copy(entryNoteInputMap = ) .entryNoteInputMap[entryNoteItem.itemProperties.getIdentifier()] = entryNoteItem
    }

    fun replacePrimaryText(primaryItem: TextItem){
        _wordEntryFormData = wordEntryFormData.copy(primaryTextInput = primaryItem)
    }




    companion object {
        fun buildDefault(_formItemManager: FormItemManager){

        }
    }

}