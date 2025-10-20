package io.github.kwvolt.japanesedictionary.ui.upsert.handler

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.GenericItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.ItemSectionProperties
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.LabelItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem

class WordUiFormHandler() : UiFormHandlerInterface {
    private val uiIdCache = mutableMapOf<String, ItemProperties>()
    private val uiIdCacheSection = mutableMapOf<String, ItemSectionProperties>()

    private fun getOrCreateUiItemProperties(key: String, formItemManager: FormItemManager): ItemProperties {
        return uiIdCache.getOrPut(key) { formItemManager.createItemProperties() }
    }

    private fun getOrCreateUiSectionItemProperties(key: String, sectionId: Int, formItemManager: FormItemManager): ItemSectionProperties {
        return uiIdCacheSection.getOrPut(key) { formItemManager.createItemSectionProperties(sectionId = sectionId) }
    }

    override fun createUIList(
        wordEntryFormData: WordEntryFormData,
        formItemManager: FormItemManager,
        formSectionManager: FormSectionManager,
        errors: Map<ItemKey, ErrorMessage>
    ): List<DisplayItem> {
        val list: MutableList<DisplayItem> = mutableListOf()

        // word Class
        list.addLabelItem(FormKeys.WordClassLabel, formItemManager)
        list.add(wordEntryFormData.wordClassInput.getDisplayItem(errors))

        // primary text
        list.addLabelItem(FormKeys.PrimaryTextLabel, formItemManager)
        list.add(wordEntryFormData.primaryTextInput.getDisplayItem(errors))

        // dictionary Notes
        list.addLabelItem(FormKeys.DictionaryNoteLabel, formItemManager)
        list.addAll(wordEntryFormData.getNoteInputMapAsList().map{ it.getDisplayItem(errors)})
        list.addButtonItem(FormKeys.DictionaryNoteAddButton, ButtonAction.AddTextItem(InputTextType.DICTIONARY_NOTE_DESCRIPTION), formItemManager)

        // Add the sections
        wordEntryFormData.wordSectionMap.forEach { section ->
            val sectionItems = createSectionItems(section.key, section.value, formItemManager, formSectionManager, errors)
            list.addAll(sectionItems)
        }
        list.addButtonItem(FormKeys.SectionAddButton, ButtonAction.AddSection, formItemManager)

        return list
    }

    override fun createSectionItems(
        sectionId: Int,
        section: WordSectionFormData,
        formItemManager: FormItemManager,
        formSectionManager: FormSectionManager,
        errors: Map<ItemKey, ErrorMessage>
    ): List<DisplayItem> {
        val sectionList: MutableList<DisplayItem> = mutableListOf()

        // Section Header
        val sectionKey: FormKeys = FormKeys.SectionLabel(sectionId)
        val sectionLabelItem = formItemManager.createSectionLabelItem(sectionCount = formSectionManager.getThenIncrementCurrentSectionCount(),
            itemSectionProperties = getOrCreateUiSectionItemProperties(
                key = sectionKey.key,
                sectionId = sectionId,
                formItemManager
            ))
        sectionList.add(sectionLabelItem.getDisplayItem(sectionKey.key, errors))

        // Meaning
        sectionList.addLabelItem(FormKeys.MeaningLabel(sectionId), formItemManager, sectionId)
        sectionList.add(section.meaningInput.getDisplayItem(errors))

        // Kana Inputs
        sectionList.addLabelItem(FormKeys.KanaLabel(sectionId), formItemManager, sectionId)
        sectionList.addAll(section.getKanaInputMapAsList().map{ it.getDisplayItem(errors)})
        sectionList.addButtonItem(
            FormKeys.KanaAddButton(sectionId), ButtonAction.AddTextChild(
                InputTextType.KANA, sectionId), formItemManager, sectionId)

        // Section Note Inputs
        sectionList.addLabelItem(FormKeys.SectionNoteLabel(sectionId), formItemManager, sectionId)
        sectionList.addAll(section.getNoteInputMapAsList().map{ it.getDisplayItem(errors)})
        sectionList.addButtonItem(
            FormKeys.SectionNoteAddButton(sectionId), ButtonAction.AddTextChild(
                InputTextType.SECTION_NOTE_DESCRIPTION, sectionId), formItemManager, sectionId)

        // Update entryChildrenCountMap
        formSectionManager.addSectionToMap(sectionLabelItem.itemProperties.getSectionIndex(),  sectionList)

        return sectionList
    }

    private fun MutableList<DisplayItem>.addLabelItem(formKey: FormKeys, formItemManager: FormItemManager, sectionId: Int? = null){
        val (labelHeaderType: LabelHeaderType, itemProperties: GenericItemProperties) = if(sectionId == null){
            LabelHeaderType.HEADER to getOrCreateUiItemProperties(formKey.key, formItemManager)
        }
        else{
            LabelHeaderType.SUB_HEADER to getOrCreateUiSectionItemProperties(formKey.key, sectionId, formItemManager)
        }
        add(
            formItemManager.createStaticLabelItem(
                formKey,
                labelHeaderType,
                genericItemProperties = itemProperties
            ).getDisplayItem(formKey.key)
        )
    }

    private fun MutableList<DisplayItem>.addButtonItem(formKey: FormKeys, buttonAction: ButtonAction, formItemManager: FormItemManager, sectionId: Int? = null){
        add(
            formItemManager.createButtonItem(
                formKey,
                buttonAction,
                genericItemProperties = if(sectionId == null){
                    getOrCreateUiItemProperties(formKey.key, formItemManager)
                }
                else{
                    getOrCreateUiSectionItemProperties(formKey.key, sectionId, formItemManager)
                }
            ).getDisplayItem(formKey.key)
        )
    }

    private fun LabelItem.getDisplayItem(key: String, errors: Map<ItemKey, ErrorMessage>? = null): DisplayItem.DisplayLabelItem{
        val formKey: ItemKey.FormItem = ItemKey.FormItem(key)
        return DisplayItem.DisplayLabelItem(errors?.get(formKey), this, formKey)

    }

    private fun ButtonItem.getDisplayItem(key: String): DisplayItem.DisplayButtonItem{
        val formKey: ItemKey.FormItem = ItemKey.FormItem(key)
        return DisplayItem.DisplayButtonItem(this, formKey)
    }

    private fun TextItem.getDisplayItem(errors: Map<ItemKey, ErrorMessage>): DisplayItem {
        val itemKey: ItemKey.DataItem = ItemKey.DataItem(this.itemProperties.getIdentifier())
        val error = errors[itemKey] ?: ErrorMessage()
        return DisplayItem.DisplayTextItem(error, this, itemKey)
    }

    private fun WordClassItem.getDisplayItem(errors: Map<ItemKey, ErrorMessage>): DisplayItem {
        val itemKey: ItemKey.DataItem = ItemKey.DataItem(this.itemProperties.getIdentifier())
        val error = errors[itemKey] ?: ErrorMessage()
        return DisplayItem.DisplayWordClassItem(error, this, itemKey)
    }
}