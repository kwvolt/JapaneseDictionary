package io.github.kwvolt.japanesedictionary.domain.data.repository

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import kotlinx.coroutines.coroutineScope
/*
data class ComponentBuildingBlock (val english: String, val kanaIdSet: KanaIdSet? = null, val componentNoteList: List<String>? = null)

class ComponentContainer(val entryComponentId: Long){
    companion object {
        suspend fun componentBuilder(entryId: Long, buildingBlock: ComponentBuildingBlock): ComponentContainer {
            val entryComponentId: Long = DatabaseHandler.performTransaction<Long> {
                val id: Long = EntryComponent.insertEntryComponent(entryId, buildingBlock.english)
                coroutineScope {
                    if (buildingBlock.kanaIdSet != null) {
                        DatabaseHandler.processBatch(buildingBlock.kanaIdSet.invoke()) { kanaId ->
                            EntryComponent.linkKanaToEntryComponent(kanaId(), id)
                        }
                    }
                    if (buildingBlock.componentNoteList != null) {
                        DatabaseHandler.processBatch(buildingBlock.componentNoteList) { componentNote ->
                            EntryComponent.insertEntryComponentNote(id, componentNote)
                        }
                    }
                }
                return@performTransaction id
            }
            return ComponentContainer(entryComponentId) // Return the created object
        }

        suspend fun getEntryComponentId(entryId: Long, english: String): ComponentContainer {
            val entryComponentId: Long = EntryComponent.getEntryComponentId(entryId, english)
            return ComponentContainer(entryComponentId)
        }
    }

    suspend fun updateEnglish(newEnglish: String) {
        EntryComponent.updateEnglish(newEnglish, entryComponentId)
    }

    suspend fun deleteComponent(){
        EntryComponent.deleteEntryComponent(entryComponentId)
    }

    suspend fun deleteConnectionToKana(kanaId: KanaId){
        EntryComponent.deleteKanaToEntryComponent(kanaId(), entryComponentId)
    }

    suspend fun deleteConnectionToKanaSet(kanaIdSet: KanaIdSet){
        DatabaseHandler.performTransaction {
            DatabaseHandler.processBatch(kanaIdSet()) { kanaId ->
                deleteConnectionToKana(kanaId)
            }
        }
    }

    suspend fun addConnectionToKana(kanaId: KanaId){
        EntryComponent.linkKanaToEntryComponent(kanaId(), entryComponentId)
    }

    suspend fun addConnectionToKanaSet(kanaIdSet: KanaIdSet) {
        DatabaseHandler.performTransaction {
            DatabaseHandler.processBatch(kanaIdSet()) { kanaId ->
                addConnectionToKana(kanaId)
            }
        }
    }

    suspend fun switchConnectionOfKana(oldKanaId: KanaId, newKanaId: KanaId){
        DatabaseHandler.performTransaction {
            deleteConnectionToKana(oldKanaId)
            addConnectionToKana(newKanaId)
        }
    }

    suspend fun addComponentNote(noteDescription: String){
        EntryComponent.insertEntryComponentNote(entryComponentId, noteDescription)
    }

    suspend fun addComponentNoteList(noteDescriptionList: List<String>){
        DatabaseHandler.performTransaction {
            DatabaseHandler.processBatch(noteDescriptionList) { noteDescription ->
                addComponentNote(noteDescription)
            }
        }
    }

    suspend fun updateComponentNote(entryComponentNoteId: Long, newNoteDescription: String){
        EntryComponent.updateEntryComponentNote(entryComponentNoteId, newNoteDescription)
    }

    suspend fun deleteComponentNote(entryComponentNoteId: Long){
        EntryComponent.deleteEntryComponentNote(entryComponentNoteId)
    }

    suspend fun deleteComponentNoteList(entryComponentNoteIdList: List<Long>){
        DatabaseHandler.performTransaction {
            DatabaseHandler.processBatch(entryComponentNoteIdList) { entryComponentNoteId ->
                deleteComponentNote(entryComponentNoteId)
            }
        }
    }
}

data class KanaIdSet(private val idList: MutableSet<KanaId>){

    operator fun invoke(): Set<KanaId> = idList

    companion object {
        suspend fun createKanaSet(kanaSet: Set<String>): KanaIdSet {
            val idList: MutableSet<KanaId> = mutableSetOf()
            DatabaseHandler.processBatch(kanaSet) { kanaText ->
                idList.add(KanaId.createKana(kanaText))
            }
            return KanaIdSet(idList)
        }

        suspend fun getKanaIdSet(kanaSet: Set<String>): KanaIdSet {
            val idList: MutableSet<KanaId> = mutableSetOf()
            DatabaseHandler.processBatch(kanaSet) { kanaText ->
                idList.add(KanaId.getKanaId(kanaText))
            }
            return KanaIdSet(idList)
        }

        suspend fun getKanaIdSet(kanaIdSet: Set<Long>): KanaIdSet {
            val idList: MutableSet<KanaId> = mutableSetOf()
            DatabaseHandler.performTransaction {
                DatabaseHandler.processBatch(kanaIdSet) { kanaId ->
                    if (EntryComponent.verifyKanaId(kanaId)) {
                        idList.add(KanaId(kanaId))
                    }
                }
            }
            return KanaIdSet(idList)
        }
    }

    suspend fun addCreateId(kanaText: String){
        idList.add(KanaId.createKana(kanaText))
    }

    suspend fun addGetId(kanaText: String){
        idList.add(KanaId.getKanaId(kanaText))
    }

    suspend fun extendCreateIdList(kanaSet: Set<String>){
        val tempKanaIdSet = createKanaSet(kanaSet)
        idList.addAll(tempKanaIdSet.idList)
    }

    suspend fun extendGetIdList(kanaSet: Set<String>){
        val tempKanaIdSet = getKanaIdSet(kanaSet)
        idList.addAll(tempKanaIdSet.idList)
    }

    suspend fun addId(id: Long){
        if(EntryComponent.verifyKanaId(id)){
            this.idList.add(KanaId(id))
        }
    }

    suspend fun extendIdList(idList: Set<Long>){
        DatabaseHandler.processBatch(idList) { id ->
            addId(id)
        }
    }
}

data class KanaId(val kanaId: Long){

    operator fun invoke(): Long = kanaId

    companion object {
        suspend fun createKana(kanaText: String): KanaId {
            val kanaId: Long = EntryComponent.insertKana(kanaText)
            return KanaId(kanaId)
        }
        suspend fun getKanaId(kanaText:String): KanaId {
            val kanaId: Long = EntryComponent.getKanaId(kanaText)
            return KanaId(kanaId)
        }
    }
    suspend fun updateKana(newWordText:String){
        EntryComponent.updateKana(newWordText, kanaId)
    }
    suspend fun deleteKana(){
        EntryComponent.deleteKana(kanaId)
    }
}

 */