package io.github.kwvolt.japanesedictionary.domain.data.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    suspend fun createTestDriver(): SqlDriver
    suspend fun createDriver(): SqlDriver
}