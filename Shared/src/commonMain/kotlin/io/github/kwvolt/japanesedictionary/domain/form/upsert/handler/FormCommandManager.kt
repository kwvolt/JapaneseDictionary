package io.github.kwvolt.japanesedictionary.domain.form.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.form.upsert.command.FormCommand
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class FormCommandManager(
    private var _wordEntryFormData: WordEntryFormData
) {

    val wordEntryFormData: WordEntryFormData get() = _wordEntryFormData.copy()

    private val undoStack = mutableListOf<FormCommand<*>>()
    private val redoStack = mutableListOf<FormCommand<*>>()

    private val MAX_HISTORY_SIZE = 50

    private val canUndo: Boolean get() = undoStack.isNotEmpty()
    private val canRedo: Boolean get() = redoStack.isNotEmpty()

    private var stateListener: UndoRedoStateListener? = null

    fun setUndoRedoListener(listener: UndoRedoStateListener) {
        this.stateListener = listener
        listener.onStateChanged(canUndo, canRedo)
    }

    fun <T> executeCommand(formCommand: FormCommand<T>): T {
        if (undoStack.size >= MAX_HISTORY_SIZE) {
            undoStack.removeAt(0)
        }
        val result = formCommand.execute()
        _wordEntryFormData = result.wordEntryFormData
        undoStack.add(formCommand)
        redoStack.clear()
        notifyListener()
        return result.value
    }

    fun undo(): Boolean {
        val command = undoStack.removeLastOrNull() ?: return false
        _wordEntryFormData = command.undo()
        redoStack.add(command)
        notifyListener()
        return true
    }

    fun redo(): Boolean {
        val command = redoStack.removeLastOrNull() ?: return false
        _wordEntryFormData = command.execute().wordEntryFormData
        undoStack.add(command)
        notifyListener()
        return true
    }

    private fun notifyListener() {
        stateListener?.onStateChanged(canUndo, canRedo)
    }
}

interface UndoRedoStateListener {
    fun onStateChanged(canUndo: Boolean, canRedo: Boolean)
}