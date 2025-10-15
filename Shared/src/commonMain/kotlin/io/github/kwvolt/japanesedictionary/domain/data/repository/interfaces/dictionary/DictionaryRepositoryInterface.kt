package io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

interface DictionaryRepositoryInterface {
    suspend fun insert(wordClassId: Long, primaryText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun delete(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun update(id: Long, wordClassId: Long?=null, primaryText: String?=null, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectIdByPrimaryText(primaryText: String, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Long>
    suspend fun selectRow(id: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionaryEntryContainer>
    suspend fun insertLinkDictionaryEntryToConjugationTemplate(dictionaryId: Long, conjugationTemplateId: Long, kana: String? = null, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun deleteLinkDictionaryEntryToConjugationTemplate(dictionaryId: Long, conjugationTemplateId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun updateLinkDictionaryEntryToConjugationTemplate(kana: String? = null, dictionaryId: Long, conjugationTemplateId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<Unit>
    suspend fun selectConjugationTemplate(dictionaryId: Long, itemId: String? = null, returnNotFoundOnNull: Boolean = false): DatabaseResult<DictionaryEntryConjugationTemplateContainer>
}
data class DictionaryEntryContainer(val id: Long, val wordClassId: Long, val primaryText: String)
data class DictionaryEntryConjugationTemplateContainer(val conjugationTemplateId: Long, val kana: String? = null)