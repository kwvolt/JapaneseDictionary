package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.inputData

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import kotlinx.collections.immutable.PersistentMap

data class CharacterFormData (
    val primaryTextInput: InputTextItem,
    val entryNoteInputMap: PersistentMap<Long, InputTextItem>,
    val componentSectionMap: PersistentMap<Int, CharacterComponentSection>
): FormData {

}

data class CharacterComponentSection(
    val englishInput: InputTextItem,
    val kanaInputMap: PersistentMap<Long, InputTextItem>,
    val componentNoteInputMap: PersistentMap<Long, InputTextItem>,
): FormSectionInterface {
    fun getKanaInputMapAsList(): List<BaseItem> {
        return kanaInputMap.values.toList()
    }

    fun getComponentNoteInputMapAsList(): List<BaseItem> {
        return componentNoteInputMap.values.toList()
    }
}
