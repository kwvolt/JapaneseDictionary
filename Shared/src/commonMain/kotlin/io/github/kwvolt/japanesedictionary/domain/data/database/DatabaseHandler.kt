package io.github.kwvolt.japanesedictionary.domain.data.database
import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DictionaryDB
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepositoryInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


class DatabaseHandler(private val driver: SqlDriver): DatabaseHandlerBase() {
    private val database: DictionaryDB
    private var isInTransaction = false

    val queries get() = database

    init {
        this.driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        this.database = DictionaryDB(driver)
    }

    override suspend fun <T> performTransaction(block: suspend () -> DatabaseResult<T>): DatabaseResult<T> {
        if (isInTransaction) {
            return DatabaseResult.UnknownError(
                IllegalStateException("Nested transactions are not allowed!"), "Nested transactions are not allowed!"
            )
        }
        isInTransaction = true
        return try {
            database.transactionWithResult {
                block()
            }
        } catch (e: Exception) {
            DatabaseResult.UnknownError(e, "Error within performTransaction")
        } finally {
            isInTransaction = false
        }
    }

    override fun close(){
        driver.close()
    }
}