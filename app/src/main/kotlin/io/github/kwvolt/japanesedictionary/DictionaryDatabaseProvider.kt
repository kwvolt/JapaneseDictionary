package io.github.kwvolt.japanesedictionary

import android.app.Application
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler

class DictionaryDatabaseProvider(private val application: Application) : DatabaseProviderInterface {
    override val databaseHandler: DatabaseHandler
        get() = (application as MainApplication).container.databaseHandler
}