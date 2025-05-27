package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

class FormCommandManager(
    private var _wordEntryFormData: WordEntryFormData
) {

    val wordEntryFormData: WordEntryFormData get() = _wordEntryFormData.copy()

    private val undoStack = mutableListOf<FormCommand>()
    private val redoStack = mutableListOf<FormCommand>()

    private val MAX_HISTORY_SIZE = 50

    private val canUndo: Boolean get() = undoStack.isNotEmpty()
    private val canRedo: Boolean get() = redoStack.isNotEmpty()

    private var stateListener: UndoRedoStateListener? = null

    fun setUndoRedoListener(listener: UndoRedoStateListener) {
        this.stateListener = listener
        listener.onStateChanged(canUndo, canRedo)
    }

    fun executeCommand(formCommand: FormCommand) {
        if (undoStack.size >= MAX_HISTORY_SIZE) {
            undoStack.removeAt(0)
        }
        _wordEntryFormData = formCommand.execute()
        undoStack.add(formCommand)
        redoStack.clear()
        notifyListener()
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
        _wordEntryFormData = command.execute()
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