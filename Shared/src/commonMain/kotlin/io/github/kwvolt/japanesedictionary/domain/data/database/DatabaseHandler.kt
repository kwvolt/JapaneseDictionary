package io.github.kwvolt.japanesedictionary.domain.data.database
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.Closeable
import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.adapter.ConjugationOverridePropertyAdapter
import io.github.kwvolt.japanesedictionary.domain.data.database.adapter.StemRuleAdapter
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.Conjugation_override_property
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.Conjugation_preprocess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext


class DatabaseHandler(private val driver: SqlDriver): DatabaseHandlerBase(), Closeable {
    private val database: DictionaryDB
    private val transactionMutex = Mutex()

    val queries: DictionaryDB get() = database

    init {
        this.driver.execute(null, "PRAGMA foreign_keys = ON;", 0)
        this.database = DictionaryDB(
            driver = driver,
            conjugation_preprocessAdapter = Conjugation_preprocess.Adapter(StemRuleAdapter()),
            conjugation_override_propertyAdapter = Conjugation_override_property.Adapter(ConjugationOverridePropertyAdapter())
        )
    }

    override suspend fun <T> performTransaction(block: suspend () -> DatabaseResult<T>): DatabaseResult<T> {
        // Check for active transaction in coroutine context
        if (coroutineContext[TransactionContext]?.inTransaction == true) {
            return DatabaseResult.UnknownError(
                IllegalStateException("Nested transactions are not allowed!").fillInStackTrace(),
                "Nested transactions are not allowed!"
            )
        }
        return transactionMutex.withLock {
            try {
                withContext(dispatcher + TransactionContext.Active) {
                    database.transactionWithResult {
                        block()
                    }
                }
            } catch (e: Exception) {
                DatabaseResult.UnknownError(e, "Error within performTransaction")
            }
        }
    }

    override fun close(){
        driver.close()
    }
}