package io.github.kwvolt.japanesedictionary.domain.model.items.item

import io.github.kwvolt.japanesedictionary.domain.model.items.TableId
import io.github.kwvolt.japanesedictionary.domain.model.items.WordEntryTable

sealed class BaseItem(open val itemProperties: GenericItemProperties)

data class ItemProperties(
    private val tableId: TableId = WordEntryTable.UI,
    private val id: Long
) : AbstractItemProperties(tableId, id) {
    override fun getIdentifier(): String {
        return "${getTableId()}-${getId()}"
    }
}

data class ItemSectionProperties(
    private val tableId: TableId = WordEntryTable.UI,
    private val id: Long,
    private val sectionId: Int
) : AbstractItemProperties(tableId, id) {
    override fun getIdentifier(): String {
        return "${getTableId()}-${getId()}-$sectionId"
    }
    fun getSectionIndex(): Int = sectionId
}

abstract class AbstractItemProperties(
    private val tableId: TableId,
    private val id: Long
) : GenericItemProperties {
    override fun getId(): Long = id
    override fun getTableId(): String = tableId.asString()
}

interface GenericItemProperties {
    fun getIdentifier(): String
    fun getId(): Long
    fun getTableId(): String
}