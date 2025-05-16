package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

import io.github.kwvolt.japanesedictionary.domain.service.TableId
import io.github.kwvolt.japanesedictionary.domain.service.WordEntryTable

private var nextAvailableId: Long = 0 // Start from -1, -2, -3, etc. for new items

fun generateNewItemId(): Long {
    return nextAvailableId++
}

data class ItemProperties(private val tableId: TableId = WordEntryTable.UI, private val id: Long = generateNewItemId()):
    GenericItemProperties {
    override fun getIdentifier(): String {
        return "${tableId.asString()}-$id"
    }

    override fun getId(): Long {
        return id
    }

    override fun getTableId(): String {
        return tableId.asString()
    }
}

data class ItemSectionProperties(private val tableId: TableId = WordEntryTable.UI, private val id: Long = generateNewItemId(), val section: Int = -1):
    GenericItemProperties, GenericSectionProperties {
    override fun getIdentifier(): String {
        return "${tableId.asString()}-$id-$section"
    }
    override fun getId(): Long {
        return id
    }

    override fun getTableId(): String {
        return tableId.asString()
    }

    override fun section(): Int {
        return section
    }
}

interface GenericItemProperties {
    fun getIdentifier(): String
    fun getId(): Long
    fun getTableId(): String
}

interface GenericSectionProperties {
    fun section(): Int
}