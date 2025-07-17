package io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items

data class ItemProperties(private val tableId: TableId = WordEntryTable.UI, private val id: Long):
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

data class ItemSectionProperties(private val tableId: TableId = WordEntryTable.UI, private val id: Long, private val sectionId: Int):
    GenericItemProperties, GenericSectionProperties {
    override fun getIdentifier(): String {
        return "${tableId.asString()}-$id-$sectionId"
    }
    override fun getId(): Long {
        return id
    }

    override fun getTableId(): String {
        return tableId.asString()
    }

    override fun getSectionIndex(): Int {
        return sectionId
    }
}

interface GenericItemProperties {
    fun getIdentifier(): String
    fun getId(): Long
    fun getTableId(): String
}

interface GenericSectionProperties {
    fun getSectionIndex(): Int
}