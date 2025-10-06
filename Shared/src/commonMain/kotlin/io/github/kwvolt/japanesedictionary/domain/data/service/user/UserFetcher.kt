package io.github.kwvolt.japanesedictionary.domain.data.service.user

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryUserRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryUserRepository

class UserFetcher(private val dictionaryUserRepository: DictionaryUserRepositoryInterface) {
    suspend fun fetchIsBookmarked(dictionaryEntryId: Long): DatabaseResult<Boolean>{
        return dictionaryUserRepository.selectIsBookmarked(dictionaryEntryId)
    }
}