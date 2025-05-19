package io.github.kwvolt.japanesedictionary.domain.data.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createTestDriver(): SqlDriver
    fun createDriver(): SqlDriver
}