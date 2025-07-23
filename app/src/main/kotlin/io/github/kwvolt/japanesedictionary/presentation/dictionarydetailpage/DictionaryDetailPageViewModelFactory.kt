package io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistryOwner
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormServiceFactory
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModel

class DictionaryDetailPageViewModelFactory(private val dbProvider: DatabaseProviderInterface, private val dictionaryId: Long?): ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if (modelClass.isAssignableFrom(DictionaryDetailPageViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()

            val dbHandler = dbProvider.databaseHandler
            val wordFormService = WordFormServiceFactory(dbHandler).create()
            val formItemManager = FormItemManager()

            DictionaryDetailPageViewModel(dictionaryId, wordFormService, formItemManager, savedStateHandle) as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}