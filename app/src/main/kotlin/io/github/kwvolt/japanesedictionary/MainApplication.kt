package io.github.kwvolt.japanesedictionary

import android.app.Application
import android.content.Context
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory


class AppContainer(context: Context) {
    val databaseHandler: DatabaseHandler by lazy {
        val driver = DriverFactory(context).createDriver()
        DatabaseHandler(driver)
    }
}

class MyApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}