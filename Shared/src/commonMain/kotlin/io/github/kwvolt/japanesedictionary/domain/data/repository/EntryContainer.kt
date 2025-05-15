package io.github.kwvolt.japanesedictionary.domain.data.repository

/*
data class EntryBuildingBlock(val componentList: List<ComponentBuildingBlock>? = null, val entryNoteList: List<String>? = null)

class EntryContainer(val dictionaryEntryId: Long){
    companion object {
        suspend fun EntryBuilder(dictionaryId: Long, wordClassId: WordClassId, componentList: List<ComponentBuildingBlock>? = null, entryNoteList: List<String>? = null): EntryContainer {
            val id: Long =
                DatabaseHandler.DictionaryEntry.insertDictionaryEntry(dictionaryId, wordClassId())
            if (componentList != null) {
                for(component in componentList){
                    ComponentContainer.componentBuilder(id, component)
                }
            }
            if (entryNoteList != null) {
                for(entryNote in entryNoteList){
                    DatabaseHandler.DictionaryEntry.insertDictionaryEntryNote(id, entryNote)
                }
            }
            return EntryContainer(id)
        }

        suspend fun getDictionaryEntry(dictionaryId: Long, wordClassId: WordClassId): EntryContainer {
            val id: Long =
                DatabaseHandler.DictionaryEntry.getDictionaryEntry(dictionaryId, wordClassId())
            return  EntryContainer(id)
        }

    }
}

data class WordClassId(val wordClassId: Long){

    operator fun invoke(): Long = wordClassId

    companion object {
       suspend  fun wordClassBuilder(mainClassIdName: String, subClassIdName: String): WordClassId {
           val wordClassId: Long =
               DatabaseHandler.WordClass.insertWordClass(mainClassIdName, subClassIdName)
           return WordClassId(wordClassId)
       }
        suspend  fun wordClassBuilder(mainClassId: Long, subClassId: Long): WordClassId {
            val wordClassId: Long =
                DatabaseHandler.WordClass.insertWordClass(mainClassId, subClassId)
            return WordClassId(wordClassId)
        }

        suspend fun getWordClassId(mainClassIdName: String, subClassIdName: String): WordClassId {
            val wordClassId: Long =
                DatabaseHandler.WordClass.insertWordClass(mainClassIdName, subClassIdName)
            return WordClassId(wordClassId)

        }

        suspend fun getWordClassId(mainClassId: Long, subClassId: Long): WordClassId {
            val wordClassId: Long =
                DatabaseHandler.WordClass.insertWordClass(mainClassId, subClassId)
            return WordClassId(wordClassId)

        }
    }
}

 */