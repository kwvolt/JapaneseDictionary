package io.github.kwvolt.japanesedictionary.util

object RetrieveDictionaryIdAsNullable {
    fun getDictionaryId(dictionaryId: Long): Long?{
        return if(dictionaryId != -1L){
            dictionaryId
        }
        else {
            null
        }

    }
}