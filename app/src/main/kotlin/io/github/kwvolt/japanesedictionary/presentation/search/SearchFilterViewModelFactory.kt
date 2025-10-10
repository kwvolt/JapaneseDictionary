package io.github.kwvolt.japanesedictionary.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.model.SearchFilter
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModel.Companion.KEY_SEARCH_FILTER

class SearchFilterViewModelFactory(
    private val _dbProvider: DatabaseProviderInterface,
    private val _searchFilter: SearchFilter? = null
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        return if (modelClass.isAssignableFrom(SearchFilterViewModel::class.java)) {
            val dbHandler = _dbProvider.databaseHandler
            if (!savedStateHandle.contains(KEY_SEARCH_FILTER)) {
                savedStateHandle[KEY_SEARCH_FILTER] =  _searchFilter
            }

            ServiceContainer(dbHandler).getServices {
                SearchFilterViewModel(
                    WordClassDataManager(wordClassBuilder, true),
                    savedStateHandle
                )
            }as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}