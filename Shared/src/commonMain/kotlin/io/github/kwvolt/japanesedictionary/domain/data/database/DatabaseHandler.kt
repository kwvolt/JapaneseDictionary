package io.github.kwvolt.japanesedictionary.domain.data.database
import app.cash.sqldelight.db.Closeable
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class DatabaseHandler(private val driver: SqlDriver): DatabaseHandlerBase(), Closeable {
    private val database: DictionaryDB
    private val transactionMutex = Mutex()
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
        return transactionMutex.withLock {
            isInTransaction = true
            try {
                database.transactionWithResult {
                    block()
                }
            } catch (e: Exception) {
                DatabaseResult.UnknownError(e, "Error within performTransaction")
            } finally {
                isInTransaction = false
            }
        }
    }

    override fun close(){
        driver.close()
    }
}