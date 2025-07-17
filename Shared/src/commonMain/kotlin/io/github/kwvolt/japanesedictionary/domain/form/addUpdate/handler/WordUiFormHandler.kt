package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormKeys
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.FormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem

class WordUiFormHandler() : UiFormHandlerInterface {
    private val uiIdCache = mutableMapOf<String, ItemProperties>()
    private val uiIdCacheSection = mutableMapOf<String, ItemSectionProperties>()

    private fun getOrCreateUiItemProperties(key: String, formItemManager: FormItemManager): ItemProperties {
        return uiIdCache.getOrPut(key) { formItemManager.createItemProperties() }
    }

    private fun getOrCreateUiSectionItemProperties(key: String, sectionId: Int, formItemManager: FormItemManager): ItemSectionProperties {
        return uiIdCacheSection.getOrPut(key) { formItemManager.createItemSectionProperties(sectionId = sectionId) }
    }

    override fun createUIList(wordEntryFormData: WordEntryFormData, formItemManager: FormItemManager, errors: Map<ItemKey, ErrorMessage>): List<BaseItem> {
        val list: MutableList<BaseItem> = mutableListOf()

        // Add WordClass, Primary Text Input, and Entry Notes
        list.add(formItemManager.createStaticLabelItem("Word Class", genericItemProperties = getOrCreateUiItemProperties(FormKeys.WORD_CLASS_LABEL, formItemManager)))
        list.add(applyDataItemErrorMessage(wordEntryFormData.wordClassInput, errors) { error ->
            WordClassFormUIItem(wordEntryFormData.wordClassInput, error)
        })

        list.add(formItemManager.createStaticLabelItem("Word", genericItemProperties = getOrCreateUiItemProperties(FormKeys.PRIMARY_TEXT_LABEL, formItemManager)))
        list.add(applyDataItemErrorMessage(wordEntryFormData.primaryTextInput, errors){ error ->
            InputTextFormUIItem(wordEntryFormData.primaryTextInput, error)
        })

        list.add(formItemManager.createStaticLabelItem("Entry Description", genericItemProperties = getOrCreateUiItemProperties(FormKeys.ENTRY_DESCRIPTION_LABEL, formItemManager)))
        list.addAll(wordEntryFormData.getEntryNoteMapAsList().map{ applyDataItemErrorMessage(it, errors){ error->
            InputTextFormUIItem(it, error) }})

        list.add(formItemManager.createButtonItem("Create more entry note", ButtonAction.AddItem(InputTextType.ENTRY_NOTE_DESCRIPTION), getOrCreateUiItemProperties(FormKeys.ENTRY_DESCRIPTION_ADD_BUTTON, formItemManager)))

        // Add the sections
        wordEntryFormData.wordSectionMap.forEach { section ->
            val sectionItems = createSectionItems(section.key, section.value, formItemManager, errors)
            list.addAll(sectionItems)
        }

        list.add(formItemManager.createButtonItem("Add Section", ButtonAction.AddSection, genericItemProperties= getOrCreateUiItemProperties(FormKeys.SECTION_ADD_BUTTON, formItemManager)))

        return list
    }

    override fun createSectionItems(
        sectionKey: Int,
        section: WordSectionFormData,
        formItemManager: FormItemManager,
        errors: Map<ItemKey, ErrorMessage>
    ): List<BaseItem> {
        val sectionList: MutableList<BaseItem> = mutableListOf()

        // Section Header
        val entryLabelItem = formItemManager.createEntryLabelItem(sectionCount = formItemManager.getCurrentSectionCount(),
            itemSectionProperties = getOrCreateUiSectionItemProperties(
                key = FormKeys.entrySectionLabel(sectionKey),
                sectionId = sectionKey,
                formItemManager
            ))
        sectionList.add(entryLabelItem)

        // English and Kana Inputs
        sectionList.add(formItemManager.createStaticLabelItem("Meaning",
            LabelHeaderType.SUB_HEADER,  genericItemProperties = getOrCreateUiSectionItemProperties(FormKeys.meaningLabel(sectionKey), sectionKey, formItemManager)))
        sectionList.add(applyDataItemErrorMessage(section.meaningInput, errors){ error->
            InputTextFormUIItem(section.meaningInput, error)
        })

        val kanaLabel = formItemManager.createStaticLabelItem("Kana", LabelHeaderType.SUB_HEADER,genericItemProperties = getOrCreateUiSectionItemProperties(FormKeys.kanaLabel(sectionKey), sectionKey, formItemManager))
        val kanaLabelWithValidation = applyFormItemErrorMessage(ItemKey.FormItem(FormKeys.kanaLabel(sectionKey)), errors){ error ->
            StaticLabelFormUIItem(kanaLabel, error)
        }
        sectionList.add(kanaLabelWithValidation)
        sectionList.addAll(section.getKanaInputMapAsList().map{ applyDataItemErrorMessage(it, errors){ error-> InputTextFormUIItem(it, error) }})
        sectionList.add(formItemManager.createButtonItem("Kana",
            ButtonAction.ValidateItem(
                kanaLabelWithValidation,
                ButtonAction.AddChild(InputTextType.KANA, entryLabelItem
                )
        ), genericItemProperties = getOrCreateUiSectionItemProperties(
            key = FormKeys.kanaAddButton(sectionKey),
            sectionId = sectionKey,
            formItemManager
        )))

        // Component Note Inputs
        sectionList.add(
            formItemManager.createStaticLabelItem(
                "Specific Description",
                LabelHeaderType.SUB_HEADER,
                genericItemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.sectionDescriptionLabel(sectionKey),
                    sectionId = sectionKey,
                    formItemManager
                )
            )
        )
        sectionList.addAll(section.getComponentNoteInputMapAsList().map{ applyDataItemErrorMessage(it, errors){ error-> InputTextFormUIItem(it, error) }})
        sectionList.add(formItemManager.createButtonItem(
                "Description",
                ButtonAction.AddChild(InputTextType.SECTION_NOTE_DESCRIPTION, entryLabelItem),
                genericItemProperties = getOrCreateUiSectionItemProperties(
                    key = FormKeys.sectionDescriptionAddButton(sectionKey),
                    sectionId = sectionKey,
                    formItemManager
                )
            )
        )

        // Update entryChildrenCountMap
        formItemManager.incrementCurrentSectionCount()
        formItemManager.addSectionToMap(entryLabelItem.itemProperties.getSectionIndex(),  sectionList)

        return sectionList
    }

    private fun applyDataItemErrorMessage(item: BaseItem, errors: Map<ItemKey, ErrorMessage>, block: (ErrorMessage)->FormUIItem): BaseItem{
        val itemId: String = item.itemProperties.getIdentifier()
        val error = errors[ItemKey.DataItem(itemId)] ?: ErrorMessage()
        return block(error)
    }

    private fun applyFormItemErrorMessage(form: ItemKey.FormItem, errors: Map<ItemKey, ErrorMessage>, block: (ErrorMessage)->FormUIItem): FormUIItem{
        val error = errors[form] ?: ErrorMessage()
        return block(error)
    }
}