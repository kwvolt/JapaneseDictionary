package io.github.kwvolt.japanesedictionary.domain.data.repository

class DictionaryContainer {
    /*
    companion object {
        suspend fun buildDictionary(primaryText: String, entryIdList: List<EntryContainer>, conjugationTemplateId: Long? = null, radicalIdSet: RadicalIdSet? = null): Long{
            val id: Long = insertDictionary(primaryText)
            for (entryId in entryIdList){
                buildEntry(id, entryId)
            }
            if (conjugationTemplateId != null) {
                linkDictionaryToConjugationTemplate(id, conjugationTemplateId)
            }
            if (radicalIdSet != null){
                for(radicalId in radicalIdSet.idList) {
                    linkKanjiToRadical(id, radicalId)
                }
            }
            return id
        }
    }

     */
}