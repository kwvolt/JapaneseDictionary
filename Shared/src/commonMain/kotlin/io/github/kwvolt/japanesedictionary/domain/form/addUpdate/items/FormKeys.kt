package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

object FormKeys {
    // Static keys (can also be an enum)
    const val WORD_CLASS_LABEL = "Word_Class_Label"
    const val PRIMARY_TEXT_LABEL = "Primary_Text_Label"
    const val ENTRY_DESCRIPTION_LABEL = "Entry_Description_Label"
    const val ENTRY_DESCRIPTION_ADD_BUTTON = "Entry_Description_AddButton"
    const val SECTION_ADD_BUTTON = "Section_AddButton"

    // Dynamic key generators
    fun kanaLabel(sectionId: Int): String = "Kana_${sectionId}_Label"
    fun kanaAddButton(sectionId: Int): String = "Kana_${sectionId}_AddButton"
    fun meaningLabel(sectionId: Int): String = "Meaning_${sectionId}_Label"
    fun sectionDescriptionLabel(sectionId: Int): String = "Section_Description_${sectionId}_Label"
    fun sectionDescriptionAddButton(sectionId: Int): String = "Section_Description_${sectionId}_AddButton"
    fun entrySectionLabel(sectionId: Int): String = "Entry_Section_${sectionId}_Label"
}