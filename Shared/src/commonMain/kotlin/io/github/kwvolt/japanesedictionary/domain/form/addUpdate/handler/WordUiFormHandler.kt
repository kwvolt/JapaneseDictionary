package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem

class WordUiFormHandler() : UiFormHandlerInterface {
    private val uiIdCache = mutableMapOf<String, GenericItemProperties>()

    private fun getOrCreateUiItemProperties(key: String): ItemProperties {
        return uiIdCache.getOrPut(key) { ItemProperties() } as ItemProperties
    }

    private fun getOrCreateUiSectionItemProperties(key: String, sectionId: Int): ItemSectionProperties {
        return uiIdCache.getOrPut(key) { ItemSectionProperties(sectionId = sectionId) } as ItemSectionProperties
    }

    override fun createUIList(wordEntryFormData: WordEntryFormData, formSectionManager: FormSectionManager, errors: Map<ValidationKey, ErrorMessage>): List<BaseItem> {
        val list: MutableList<BaseItem> = mutableListOf()

        // Add WordClass, Primary Text Input, and Entry Notes
        list.add(StaticLabelItem("Word Class",
            itemProperties = getOrCreateUiItemProperties(FormKeys.WORD_CLASS_LABEL)))
        list.add(applyDataItemErrorMessage(wordEntryFormData.wordClassInput, errors) { error ->
            WordClassFormUIItem(wordEntryFormData.wordClassInput, error)
        })

        list.add(StaticLabelItem("Word",
            itemProperties = getOrCreateUiItemProperties(FormKeys.PRIMARY_TEXT_LABEL)))
        list.add(applyDataItemErrorMessage(wordEntryFormData.primaryTextInput, errors){ error ->
            InputTextFormUIItem(wordEntryFormData.primaryTextInput, error)
        })

        list.add(StaticLabelItem("Entry Description",
            itemProperties = getOrCreateUiItemProperties(FormKeys.ENTRY_DESCRIPTION_LABEL)))
        list.addAll(wordEntryFormData.getEntryNoteMapAsList().map{ applyDataItemErrorMessage(it, errors){ error->
            InputTextFormUIItem(it, error) }})

        list.add(
            ItemButtonItem(
                "Create more entry note",
                ButtonAction.AddItem(InputTextType.ENTRY_NOTE_DESCRIPTION),
                itemProperties = getOrCreateUiItemProperties(FormKeys.ENTRY_DESCRIPTION_ADD_BUTTON)
            )
        )

        // Add the sections
        wordEntryFormData.wordSectionMap.forEach { section ->
            val sectionItems = createSectionItems(section.key, section.value, formSectionManager)
            list.addAll(sectionItems)
        }

        list.add(ItemButtonItem("Add Section", ButtonAction.AddSection,
            itemProperties = getOrCreateUiItemProperties(FormKeys.SECTION_ADD_BUTTON)))

        return list
    }

    override fun createSectionItems(
        sectionKey: Int,
        section: WordSectionFormData,
        formSectionManager: FormSectionManager,
        errors: Map<ValidationKey, ErrorMessage>
    ): List<BaseItem> {
        val sectionList: MutableList<BaseItem> = mutableListOf()

        // Section Header
        val entryLabelItem = EntryLabelItem(
            sectionCount = formSectionManager.getCurrentSectionCount(),
            itemProperties = getOrCreateUiSectionItemProperties(
                key = FormKeys.entrySectionLabel(sectionKey),
                sectionId = sectionKey
            )
        )
        sectionList.add(entryLabelItem)

        // English and Kana Inputs
        sectionList.add(
            StaticLabelItem(
                "Meaning",
                LabelType.SUB_HEADER,
                itemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.meaningLabel(sectionKey),
                    sectionId = sectionKey
                )
            )
        )
        sectionList.add(applyDataItemErrorMessage(section.meaningInput, errors){ error->
            InputTextFormUIItem(section.meaningInput, error)
        })


        val kanaLabel =  StaticLabelItem(
            "Kana",
            LabelType.SUB_HEADER,
            itemProperties = getOrCreateUiSectionItemProperties(
                key = FormKeys.kanaLabel(sectionKey),
                sectionId = sectionKey
            )
        )
        sectionList.add(applyFormItemErrorMessage(ValidationKey.FormItem(FormKeys.kanaLabel(sectionKey)), errors){ error ->
            StaticLabelFormUIItem(kanaLabel, error)
        })
        sectionList.addAll(section.getKanaInputMapAsList().map{ applyDataItemErrorMessage(it, errors){ error-> InputTextFormUIItem(it, error) }})
        sectionList.add(
            ItemButtonItem(
                "Kana",
                ButtonAction.AddChild(InputTextType.KANA, entryLabelItem),
                itemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.kanaAddButton(sectionKey),
                    sectionId = sectionKey
                )
            )
        )

        // Component Note Inputs
        sectionList.add(
            StaticLabelItem(
                "Specific Description",
                LabelType.SUB_HEADER,
                itemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.sectionDescriptionLabel(sectionKey),
                    sectionId = sectionKey
                )
            )
        )
        sectionList.addAll(section.getComponentNoteInputMapAsList().map{ applyDataItemErrorMessage(it, errors){ error-> InputTextFormUIItem(it, error) }})
        sectionList.add(
            ItemButtonItem(
                "Description",
                ButtonAction.AddChild(InputTextType.SECTION_NOTE_DESCRIPTION, entryLabelItem),
                itemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.sectionDescriptionAddButton(sectionKey),
                    sectionId = sectionKey
                )
            )
        )

        // Update entryChildrenCountMap
        formSectionManager.incrementCurrentSectionCount()
        formSectionManager.addSectionToMap(entryLabelItem.itemProperties.getSectionIndex(),  sectionList)

        return sectionList
    }

    private fun applyDataItemErrorMessage(item: BaseItem, errors: Map<ValidationKey, ErrorMessage>, block: (ErrorMessage)->FormUIItem): BaseItem{
        val itemId: String = item.itemProperties.getIdentifier()
        val error = errors[ValidationKey.DataItem(itemId)] ?: ErrorMessage()
        return block(error)
    }

    private fun applyFormItemErrorMessage(form: ValidationKey.FormItem, errors: Map<ValidationKey, ErrorMessage>, block: (ErrorMessage)->FormUIItem): BaseItem{
        val error = errors[form] ?: ErrorMessage()
        return block(error)
    }
}