package io.github.kwvolt.japanesedictionary.presentation.dictionarylookup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer

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