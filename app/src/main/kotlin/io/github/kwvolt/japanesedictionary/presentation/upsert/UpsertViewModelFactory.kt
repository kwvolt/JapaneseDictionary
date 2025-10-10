package io.github.kwvolt.japanesedictionary.presentation.upsert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.kwvolt.japanesedictionary.DatabaseProviderInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.FormListValidatorManager
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.upsert.handler.WordClassDataManager
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.WordFormItemListManager
import io.github.kwvolt.japanesedictionary.ui.upsert.handler.WordUiFormHandler

class UpsertViewModelFactory(private val dbProvider: DatabaseProviderInterface): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UpsertViewModel::class.java)) {
            val dbHandler = dbProvider.databaseHandler
            val formItemManager = FormItemManager()
            val wordFormItemListManager = WordFormItemListManager(WordUiFormHandler(), FormSectionManager())
            ServiceContainer(dbHandler).getServices {
                UpsertViewModel(
                    wordFormItemListManager,
                    formItemManager,
                    WordClassDataManager(wordClassBuilder, false),
                    FormListValidatorManager(wordEntryFormValidation),
                    wordEntryFormUpsertValidation,
                    wordEntryFormItemFetcher,
                    wordEntryFormBuilder

                )
            } as T
        }else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}