package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.AddSectionCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.FormCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateSectionNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateEntryNoteItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateKanaItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdateMeaningItemCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.dataHandler.command.formCommand.UpdatePrimaryTextCommand
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties

class WordFormHandler (
    private val dataHandler: FormCommandManager,
    private val formStateManager: FormStateManager,
    private val wordUiFormHandler: UiFormHandlerInterface
){
    fun createInitialForm(): List<BaseItem> {
        return wordUiFormHandler.createUIList(dataHandler.wordEntryFormData, formStateManager)
    }

    fun createNewSection(): List<BaseItem> {
        formStateManager.incrementEntryCount()
        val sectionIndex = formStateManager.getCurrentEntryCount()
        val command = AddSectionCommand(dataHandler.wordEntryFormData, sectionIndex)
        dataHandler.executeCommand(command)

        val sectionData = dataHandler.wordEntryFormData.wordSectionMap[sectionIndex]
        return sectionData?.let {
            wordUiFormHandler.createSectionItems(sectionIndex, it, formStateManager)
        } ?: emptyList()
    }

    fun handleInputChange(item: InputTextItem, value: String): FormCommand {
        val updated = item.copy(inputTextValue = value)
        return createUpdateCommand(item, updated)
    }

    private fun createUpdateCommand(original: InputTextItem, updated: InputTextItem): FormCommand {
        val section = (original.itemProperties as? ItemSectionProperties)?.section() ?: -1
        val data = dataHandler.wordEntryFormData
        return when (original.inputTextType) {
            InputTextType.PRIMARY_TEXT -> UpdatePrimaryTextCommand(data, updated)
            InputTextType.MEANING -> UpdateMeaningItemCommand(data, section, updated)
            InputTextType.KANA -> UpdateKanaItemCommand(data, section, updated)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> UpdateEntryNoteItemCommand(data, updated)
            InputTextType.SECTION_NOTE_DESCRIPTION -> UpdateSectionNoteItemCommand(data, section, updated)
        }
    }

    private fun createAddFormCommand(textItem: InputTextItem): FormCommand {
        val section = (textItem.itemProperties as? ItemSectionProperties)?.section() ?: -1
        val data = dataHandler.wordEntryFormData

        return when (textItem.inputTextType) {
            InputTextType.KANA -> AddKanaItemCommand(data, section, textItem)
            InputTextType.ENTRY_NOTE_DESCRIPTION -> AddEntryNoteItemCommand(data, textItem)
            InputTextType.SECTION_NOTE_DESCRIPTION -> AddSectionNoteItemCommand(data, section, textItem)
            else -> throw IllegalStateException("Illegal InputTextType ${textItem::class.java} was passed")
        }
    }
}
