package io.github.kwvolt.japanesedictionary.domain.data.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun createTestDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        DictionaryDB.Schema.create(driver)
        return driver
    }

    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = AndroidSqliteDriver(
            DictionaryDB.Schema.synchronous(),
            context.applicationContext,
            "JapaneseDictionary.db"
        )
        return driver
    }
}