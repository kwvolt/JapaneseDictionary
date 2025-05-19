package io.github.kwvolt.japanesedictionary.domain.form.handler

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem

class WordUiFormHandler: UiFormHandlerInterface {
    override fun createUIList(wordEntryFormData: WordEntryFormData, formSectionManager: FormSectionManager): List<BaseItem> {
        val list: MutableList<BaseItem> = mutableListOf()

        // Add WordClass, Primary Text Input, and Entry Notes
        list.add(StaticLabelItem("Word Class", itemProperties = ItemProperties()))
        list.add(wordEntryFormData.wordClassInput)
        list.add(StaticLabelItem("Word", itemProperties = ItemProperties()))
        list.add(wordEntryFormData.primaryTextInput)
        list.add(StaticLabelItem("Entry Description", itemProperties = ItemProperties()))
        list.addAll(wordEntryFormData.getEntryNoteMapAsList())
        list.add(
            ItemButtonItem("Create more entry note",
                ButtonAction.AddItem(InputTextType.ENTRY_NOTE_DESCRIPTION), itemProperties = ItemProperties())
        )

        // Add the sections
        wordEntryFormData.wordSectionMap.forEach { section ->
            val sectionItems = createSectionItems(section.key, section.value, formSectionManager)
            list.addAll(sectionItems)
        }

        list.add(ItemButtonItem("Add Section", ButtonAction.AddSection, itemProperties = ItemProperties()))

        return list
    }

    override fun createSectionItems(sectionKey: Int, section: WordSectionFormData, formSectionManager: FormSectionManager): List<BaseItem> {
        val sectionList: MutableList<BaseItem> = mutableListOf()

        // Section Header
        val entryLabelItem = EntryLabelItem(itemProperties = ItemSectionProperties(sectionId = sectionKey))
        sectionList.add(entryLabelItem)

        // English and Kana Inputs
        sectionList.add(StaticLabelItem("English", LabelType.SUB_HEADER, itemProperties = ItemSectionProperties(sectionId = sectionKey)))
        sectionList.add(section.meaningInput)
        sectionList.add(StaticLabelItem("Kana", LabelType.SUB_HEADER, itemProperties = ItemSectionProperties(sectionId= sectionKey)))
        sectionList.addAll(section.getKanaInputMapAsList())
        sectionList.add(ItemButtonItem("Add Kana", ButtonAction.AddChild(InputTextType.KANA, entryLabelItem), itemProperties = ItemSectionProperties(sectionId= sectionKey)))

        // Component Note Inputs
        sectionList.add(StaticLabelItem("Specific Description", LabelType.SUB_HEADER, itemProperties = ItemSectionProperties()))
        sectionList.addAll(section.getComponentNoteInputMapAsList())
        sectionList.add(ItemButtonItem("Description", ButtonAction.AddChild(InputTextType.SECTION_NOTE_DESCRIPTION, entryLabelItem), itemProperties = ItemSectionProperties(sectionId = sectionKey)))

        // Update entryChildrenCountMap
        formSectionManager.addSectionToMap(entryLabelItem.itemProperties.getSectionIndex(),  sectionList)

        return sectionList
    }
}