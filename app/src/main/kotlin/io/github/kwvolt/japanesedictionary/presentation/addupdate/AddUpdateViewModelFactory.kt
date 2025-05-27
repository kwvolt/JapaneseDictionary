package io.github.kwvolt.japanesedictionary.presentation.addupdate

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormServiceFactory

class AddUpdateViewModelFactory(private val application: Application, private val dbProvider: DatabaseProviderInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AddUpdateViewModel::class.java)) {
            val dbHandler = dbProvider.databaseHandler
            val wordFormService = WordFormServiceFactory(dbHandler).create()
            AddUpdateViewModel(wordFormService) as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}