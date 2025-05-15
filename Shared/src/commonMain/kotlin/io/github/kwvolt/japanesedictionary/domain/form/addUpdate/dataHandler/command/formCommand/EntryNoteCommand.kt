package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

class AddEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: InputTextItem
) : FormCommand {

    private var noteId: String = ""

    override fun execute(): WordEntryFormData {
        noteId = newEntryNoteItem.itemProperties.getIdentifier()
        val updatedMap = wordEntryFormData.entryNoteInputMap.put(noteId, newEntryNoteItem)
        return wordEntryFormData.copy(entryNoteInputMap = updatedMap)
    }

    override fun undo(): WordEntryFormData {
        return wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.remove(noteId))
    }
}

class UpdateEntryNoteItemCommand(
    private val wordEntryFormData: WordEntryFormData,
    private val newEntryNoteItem: InputTextItem,
) : FormCommand {

    private val noteId: String = newEntryNoteItem.itemProperties.getIdentifier()
    private val oldNote: InputTextItem? = wordEntryFormData.entryNoteInputMap[noteId]

    override fun execute(): WordEntryFormData {
        val updatedMap = wordEntryFormData.entryNoteInputMap.put(noteId, newEntryNoteItem)
        return wordEntryFormData.copy(entryNoteInputMap = updatedMap)
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
) : FormCommand {

    private val oldNote: InputTextItem? = wordEntryFormData.entryNoteInputMap[noteId]

    override fun execute(): WordEntryFormData {
        return wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.remove(noteId))
    }

    override fun undo(): WordEntryFormData {
        return if (oldNote != null) {
            wordEntryFormData.copy(entryNoteInputMap = wordEntryFormData.entryNoteInputMap.put(noteId, oldNote))
        } else {
            wordEntryFormData
        }
    }
}