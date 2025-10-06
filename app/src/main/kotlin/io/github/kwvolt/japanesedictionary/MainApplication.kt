package io.github.kwvolt.japanesedictionary

import android.app.Application
import android.content.Context
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.database.initialize.InitialWordClass
import io.github.kwvolt.japanesedictionary.domain.data.database.initialize.InitializeWordEntries
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class AppContainer(context: Context) {
    val databaseHandler: DatabaseHandler by lazy {
        val driver = DriverFactory(context).createTestDriver()
        DatabaseHandler(driver)
    }
}

class MainApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val container: AppContainer  by lazy {
        AppContainer(this)
    }
    override fun onCreate() {
        super.onCreate()
        val handler: DatabaseHandler = container.databaseHandler

        val serviceContainer = ServiceContainer(handler)
        serviceContainer.getServices {
            applicationScope.launch{
                InitialWordClass(handler, wordClassUpsert).createWordClass()
                InitializeWordEntries(wordEntryFormUpsertValidation, wordClassFetcher).generateWordEntries()
            }
        }
    }
}