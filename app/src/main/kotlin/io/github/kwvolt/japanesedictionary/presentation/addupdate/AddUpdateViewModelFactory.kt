package io.github.kwvolt.japanesedictionary.presentation.addupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordFormServiceFactory
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormListValidatorManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordFormItemListManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordUiFormHandler

class AddUpdateViewModelFactory(private val dbProvider: DatabaseProviderInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AddUpdateViewModel::class.java)) {

            val dbHandler = dbProvider.databaseHandler
            val wordFormService = WordFormServiceFactory(dbHandler).create()
            val wordFormItemListManager = WordFormItemListManager(WordUiFormHandler())
            val formItemManager = FormItemManager()
            val wordClassDataManager = WordClassDataManager()
            val formListValidatorManager =  FormListValidatorManager()

            AddUpdateViewModel(wordFormService, wordFormItemListManager, formItemManager, wordClassDataManager, formListValidatorManager) as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}