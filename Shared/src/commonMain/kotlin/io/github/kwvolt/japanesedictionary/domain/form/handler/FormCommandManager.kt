package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData

class FormCommandManager(private var _wordEntryFormData: WordEntryFormData) {

    // Use immutable stacks for better safety
    public val wordEntryFormData: WordEntryFormData get() = _wordEntryFormData.copy()

    private val undoStack: MutableList<FormCommand> = mutableListOf()
    private val redoStack: MutableList<FormCommand> = mutableListOf()

    private val MAX_HISTORY_SIZE = 50

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun executeCommand(formCommand: FormCommand) {
        if (undoStack.size >= MAX_HISTORY_SIZE) {
            undoStack.removeAt(0) // Remove the oldest command if we've hit the limit
        }

        // Execute the command and update the wordFormData
        _wordEntryFormData = formCommand.execute()
        undoStack.add(formCommand)

        // Clear the redo stack, as new actions invalidate redo history
        redoStack.clear()
    }

    fun undo(): Boolean {
        // Check if there's anything to undo
        val command = undoStack.removeLastOrNull()
        return if (command != null) {
            _wordEntryFormData = command.undo()
            redoStack.add(command)
            true
        } else {
            false // No command to undo
        }
    }

    fun redo(): Boolean {
        // Check if there's anything to redo
        val command = redoStack.removeLastOrNull()
        return if (command != null) {
            _wordEntryFormData = command.execute()
            undoStack.add(command)
            true
        } else {
            false // No command to redo
        }
    }
}