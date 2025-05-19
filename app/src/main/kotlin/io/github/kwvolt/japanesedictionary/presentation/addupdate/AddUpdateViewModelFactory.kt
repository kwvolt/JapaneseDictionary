package io.github.kwvolt.japanesedictionary.presentation.addupdate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.database.FakeDatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.FakeWordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface

class AddUpdateViewModelFactory(private val repository: WordClassRepositoryInterface): ViewModelProvider.Factory {
    override suspend fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddUpdateViewModel::class.java)) {
            val databaseDriverFactory = DriverFactory()
            val driver = databaseDriverFactory.createDriver()
            val database = DatabaseHandler(driver)
            val repository = WordClassRepository(database)
            return AddUpdateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class FakeAddUpdateViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddUpdateViewModel::class.java)) {
            val database =
            val repository = FakeWordClassRepository(database)
            return AddUpdateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}