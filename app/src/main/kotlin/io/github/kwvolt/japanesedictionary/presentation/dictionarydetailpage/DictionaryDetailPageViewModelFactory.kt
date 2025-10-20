package io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager

class DictionaryDetailPageViewModelFactory(private val dbProvider: DatabaseProviderInterface, private val dictionaryId: Long): ViewModelProvider.Factory  {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if (modelClass.isAssignableFrom(DictionaryDetailPageViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            val dbHandler = dbProvider.databaseHandler
            val formItemManager = FormItemManager()
            ServiceContainer(dbHandler).getServices {
                DictionaryDetailPageViewModel(
                    dictionaryId,
                    formItemManager,
                    wordEntryFormUpsertValidation,
                    wordEntryFormItemFetcher,
                    wordEntryFormBuilder,
                    wordEntryFormDelete,
                    wordEntryFormUpsert,
                    userFetcher,
                    savedStateHandle)
            }as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}