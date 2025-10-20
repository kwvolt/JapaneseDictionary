package io.github.kwvolt.japanesedictionary.domain.form.upsert.command

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData

class AddEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: TextItem
) : FormCommand {

    private val noteId: String = newEntryNoteItem.itemProperties.getIdentifier()

    override fun execute(): WordEntryFormData{
        val updatedMap = wordEntryFormData.noteInputMap.put(noteId, newEntryNoteItem)
        return wordEntryFormData.copy(noteInputMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(noteInputMap = wordEntryFormData.noteInputMap.remove(noteId))
    }
}

class UpdateEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: TextItem,
) : FormCommand {

    private val noteId: String = newEntryNoteItem.itemProperties.getIdentifier()
    private val oldNote: TextItem? = wordEntryFormData.noteInputMap[noteId]

    override fun execute(): WordEntryFormData {
        val updatedMap = wordEntryFormData.noteInputMap.put(noteId, newEntryNoteItem)
        return wordEntryFormData.copy(noteInputMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        val revertedMap = oldNote?.let {
            wordEntryFormData.noteInputMap.put(noteId, it)
        } ?: wordEntryFormData.noteInputMap
        return wordEntryFormData.copy(noteInputMap = revertedMap)
    }
}

class RemoveEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val noteId: String
) : FormCommand {

    private val oldNote: TextItem? = wordEntryFormData.noteInputMap[noteId]

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(noteInputMap = wordEntryFormData.noteInputMap.remove(noteId))
    }

    override fun undo(): WordEntryFormData {
        return if (oldNote != null) {
            wordEntryFormData.copy(noteInputMap = wordEntryFormData.noteInputMap.put(noteId, oldNote))
        } else {
            wordEntryFormData
        }
    }
}