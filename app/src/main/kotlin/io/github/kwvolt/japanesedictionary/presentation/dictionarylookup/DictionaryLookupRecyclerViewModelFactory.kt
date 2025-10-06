package io.github.kwvolt.japanesedictionary.presentation.dictionarylookup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionarySearchRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryUserRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormDelete
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormItemFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormSearchFilter

class DictionaryLookupRecyclerViewModelFactory(
    private val _dbProvider: DatabaseProviderInterface
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if (modelClass.isAssignableFrom(DictionaryLookupRecyclerViewModel::class.java)) {
            val savedStateHandle: SavedStateHandle = extras.createSavedStateHandle()
            val dbHandler = _dbProvider.databaseHandler
            ServiceContainer(dbHandler).getServices {
                DictionaryLookupRecyclerViewModel(
                    wordEntryFormBuilder,
                    wordEntryFormUpsert,
                    wordEntryFormDelete,
                    wordEntryFormSearchFilter,
                    userFetcher,
                    savedStateHandle
                ) as T
            }
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}