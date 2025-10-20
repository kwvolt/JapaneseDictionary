package io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item

import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassContainer

data class TextItem(
    val inputTextType: InputTextType,
    val inputTextValue: String,
    override val itemProperties: GenericItemProperties
) : BaseItem(itemProperties)

data class WordClassItem(
    val chosenMainClass: MainClassContainer,
    val chosenSubClass: SubClassContainer,
    override val itemProperties: GenericItemProperties,
) : BaseItem(itemProperties)

data class ConjugationTemplateItem(
    val chosenConjugationTemplateId: Long,
    val kanaId: Long? = null,
    override val itemProperties: GenericItemProperties,
) : BaseItem(itemProperties)


enum class InputTextType {
    PRIMARY_TEXT,
    MEANING,
    KANA,
    DICTIONARY_NOTE_DESCRIPTION,
    SECTION_NOTE_DESCRIPTION
}