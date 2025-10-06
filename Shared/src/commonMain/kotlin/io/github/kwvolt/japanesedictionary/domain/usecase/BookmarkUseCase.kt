package io.github.kwvolt.japanesedictionary.domain.usecase

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert


class ToggleBookmarkUseCase(
    private val wordEntryFormUpsert: WordEntryFormUpsert,
) {
    suspend operator fun invoke(dictionaryId: Long, currentState: Boolean, optimisticUpdate: (Boolean) -> Unit): DatabaseResult<Boolean> {
        val newBookmarkState = !currentState
        optimisticUpdate(newBookmarkState)
        return wordEntryFormUpsert.updateIsBookmark(dictionaryId, newBookmarkState).map {
            newBookmarkState
        }
    }
}

class GetBookmarkStateUseCase(private val userFetcher: UserFetcher) {
    suspend operator fun invoke(dictionaryId: Long): DatabaseResult<Boolean> {
        return userFetcher.fetchIsBookmarked(dictionaryId)
    }
}