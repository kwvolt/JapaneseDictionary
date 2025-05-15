package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

private var nextAvailableId: Long = 0 // Start from -1, -2, -3, etc. for new items

fun generateNewItemId(): Long {
    return nextAvailableId++
}

data class ItemProperties(private val tableId: String = "UI", private val id: Long = generateNewItemId()):
    GenericItemProperties {
    override fun getIdentifier(): String {
        return "$tableId-$id"
    }

    override fun getId(): Long {
        return id
    }
}

data class ItemSectionProperties(private val tableId: String = "UI", private val id: Long = generateNewItemId(), val section: Int = -1):
    GenericItemProperties, GenericSectionProperties {
    override fun getIdentifier(): String {
        return "$tableId-$id-$section"
    }
    override fun getId(): Long {
        return id
    }

    fun getTableId(): String {
        return tableId
    }

    override fun section(): Int {
        return section
    }
}

interface GenericItemProperties {
    fun getIdentifier(): String
    fun getId(): Long
}

interface GenericSectionProperties {
    fun section(): Int
}