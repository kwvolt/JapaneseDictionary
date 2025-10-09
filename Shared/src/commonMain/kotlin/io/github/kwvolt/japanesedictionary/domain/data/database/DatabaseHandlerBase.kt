package io.github.kwvolt.japanesedictionary.domain.data.database

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.exceptions.mapToDatabaseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


abstract class DatabaseHandlerBase {
    open val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private fun <K, V> Map<K, V>.chunkedMap(batchSize: Int): List<List<Map.Entry<K, V>>> {
        return this.entries.chunked(batchSize)
    }

    suspend fun <T> processBatchRead(
        items: Collection<T>,
        batchSize: Int = 10,
        maxConcurrent: Int = 5,
        task: suspend (T) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit>  {
        val batches = items.chunked(batchSize)
        val semaphore = Semaphore(maxConcurrent)
        return coroutineScope {
            batches.forEach { batch ->
                val jobs = batch.map { item ->
                    async {
                        semaphore.withPermit {
                            task(item)
                        }
                    }
                }
                jobs.awaitAll()
            }
            DatabaseResult.Success(Unit)
        }
    }

    suspend fun <T> processBatchWrite(
        items: Collection<T>,
        task: suspend (T) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        for (item in items) {
            val result = task(item)
            if (result.isFailure) return result
        }
        return DatabaseResult.Success(Unit)
    }

    suspend fun <T, R> processBatchRead(
        items: Map<T, R>,
        batchSize: Int = 10,
        maxConcurrent: Int = 5,
        task: suspend (Map.Entry<T, R>) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        val batches = items.chunkedMap(batchSize)
        val semaphore = Semaphore(maxConcurrent)
        return coroutineScope {
            batches.forEach { batch ->
                val jobs = batch.map { item ->
                    async {
                        semaphore.withPermit {
                            task(item)
                        }
                    }
                }
                jobs.awaitAll()
            }
            DatabaseResult.Success(Unit)
        }
    }

    suspend fun <T, R> processBatchWrite(
        items: Map<T, R>,
        task: suspend (Map.Entry<T, R>) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        for (item in items) {
            val result = task(item)
            if (result.isFailure) return result
        }
        return DatabaseResult.Success(Unit)
    }

    suspend inline fun <T> wrapQuery(
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false,
        crossinline block: suspend () -> T?
    ): DatabaseResult<T> {
        val inTransaction = coroutineContext[TransactionContext]?.inTransaction ?: false
        return try {
            val result = if (inTransaction) block() else withContext(dispatcher) { block() }
            when {
                result != null -> DatabaseResult.Success(result)
                returnNotFoundOnNull -> itemId?.let {
                    DatabaseResult.InvalidInput(ItemKey.DataItem(it), DatabaseError(DatabaseErrorType.NOT_FOUND))
                } ?: DatabaseResult.NotFound
                else -> DatabaseResult.UnknownError(
                    NullPointerException("Expected value but was null").fillInStackTrace(),
                    "Expected value but was null"
                )
            }
        } catch (e: Exception) {
            itemId?.let {
                mapToDatabaseException(ItemKey.DataItem(it), e)
            } ?: DatabaseResult.UnknownError(e, "Unexpected DB error")
        }
    }

    suspend inline fun wrapRowCountQuery(
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false,
        crossinline block: suspend () -> Long
    ): DatabaseResult<Unit> {
        val inTransaction = coroutineContext[TransactionContext]?.inTransaction ?: false
        return try {
            val affectedRows = if (inTransaction) block() else withContext(dispatcher) { block() }
            when {
                affectedRows > 0 -> DatabaseResult.Success(Unit)
                returnNotFoundOnNull -> itemId?.let {
                    DatabaseResult.InvalidInput(ItemKey.DataItem(it), DatabaseError(DatabaseErrorType.NOT_FOUND))
                } ?: DatabaseResult.NotFound
                else -> DatabaseResult.UnknownError(
                    NullPointerException("Expected value but was null").fillInStackTrace(),
                    "Expected value but was null"
                )

            }
        } catch (e: Exception) {
            itemId?.let {
                mapToDatabaseException(ItemKey.DataItem(it), e)
            } ?: DatabaseResult.UnknownError(e, "Unexpected DB error")
        }
    }

    suspend inline fun <T, R> selectAll(
        itemId: String? = null,
        returnNotFoundOnNull: Boolean = false,
        crossinline queryBlock: suspend () -> List<T>,
        crossinline mapper: (T) -> R
    ): DatabaseResult<List<R>> {
        return wrapQuery(itemId, returnNotFoundOnNull) {
            val results = queryBlock()
            if (results.isNotEmpty()) results.map { mapper(it) } else emptyList()
        }
    }

    // Method to perform a transaction
    abstract suspend fun <T> performTransaction(block: suspend () -> DatabaseResult<T>): DatabaseResult<T>

    suspend fun <T> requireTransaction(block: suspend () -> DatabaseResult<T>): DatabaseResult<T>{
        if (coroutineContext[TransactionContext]?.inTransaction != true) {
            return DatabaseResult.UnknownError(
                IllegalStateException("This function must be called within a transaction"),
                "This function must be called within a transaction"
            )
        }
        return block()
    }

    // Method to close the database connection
    abstract fun close()
}

class TransactionContext private constructor(val inTransaction: Boolean) : AbstractCoroutineContextElement(TransactionContext) {
    companion object : CoroutineContext.Key<TransactionContext> {
        val Active = TransactionContext(true)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiresTransaction