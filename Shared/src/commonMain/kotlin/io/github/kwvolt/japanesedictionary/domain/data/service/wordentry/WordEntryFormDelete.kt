package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepositoryInterface

class WordEntryFormDelete (
    private val dbHandler: DatabaseHandlerBase,
    private val entryRepository: EntryRepositoryInterface,
){
    suspend fun deleteWordEntryFormData(dictionaryEntryId: Long): DatabaseResult<Unit> {
        return dbHandler.performTransaction {
            entryRepository.deleteDictionaryEntry(dictionaryEntryId)
        }
    }
}