package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class AddEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: TextItem
) : FormCommand<Unit> {

    private val noteId: String = newEntryNoteItem.itemProperties.getIdentifier()

    override fun execute(): CommandReturn<Unit>{
        val updatedMap = wordEntryFormData.entryNoteInputMap.put(noteId, newEntryNoteItem)
        return CommandReturn(wordEntryFormData.copy(entryNoteInputMap = updatedMap), Unit)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.remove(noteId))
    }
}

class UpdateEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: TextItem,
) : FormCommand<Unit> {

    private val noteId: String = newEntryNoteItem.itemProperties.getIdentifier()
    private val oldNote: TextItem? = wordEntryFormData.entryNoteInputMap[noteId]

    override fun execute():  CommandReturn<Unit> {
        val updatedMap = wordEntryFormData.entryNoteInputMap.put(noteId, newEntryNoteItem)
        return CommandReturn(wordEntryFormData.copy(entryNoteInputMap = updatedMap), Unit)
    }

    override fun undo(): WordEntryFormData {
        val revertedMap = oldNote?.let {
            wordEntryFormData.entryNoteInputMap.put(noteId, it)
        } ?: wordEntryFormData.entryNoteInputMap
        return wordEntryFormData.copy(entryNoteInputMap = revertedMap)
    }
}

class RemoveEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val noteId: String
) : FormCommand<Unit> {

    private val oldNote: TextItem? = wordEntryFormData.entryNoteInputMap[noteId]

    override fun execute():  CommandReturn<Unit> {
        return CommandReturn(wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.remove(noteId)), Unit)
    }

    override fun undo(): WordEntryFormData {
        return if (oldNote != null) {
            wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.put(noteId, oldNote))
        } else {
            wordEntryFormData
        }
    }
}