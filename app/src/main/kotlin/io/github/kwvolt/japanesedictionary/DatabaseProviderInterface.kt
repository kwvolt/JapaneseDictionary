package io.github.kwvolt.japanesedictionary

import android.app.Application
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler

interface DatabaseProviderInterface {
    val databaseHandler: DatabaseHandler
}