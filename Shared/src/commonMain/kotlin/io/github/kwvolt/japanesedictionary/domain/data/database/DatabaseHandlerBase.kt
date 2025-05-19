package io.github.kwvolt.japanesedictionary.domain.data.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

abstract class DatabaseHandlerBase {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private fun <K, V> Map<K, V>.chunkedMap(batchSize: Int): List<List<Map.Entry<K, V>>> {
        return this.entries.chunked(batchSize)
    }

    suspend fun <T> processBatch(
        items: Collection<T>,
        batchSize: Int = 10,
        maxConcurrent: Int = 5,
        task: suspend (T) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit>  {
        val batches = items.chunked(batchSize)
        val semaphore = Semaphore(maxConcurrent)
        return coroutineScope {
            batches.forEach { batch ->
                batch.map {
                    semaphore.withPermit {
                        async { task(it) }
                    }
                }.also {
                    // Wait for all tasks in this batch to complete
                    it.awaitAll()
                }
            }
            return@coroutineScope DatabaseResult.Success(Unit)
        }
    }

    suspend fun <T, R> processBatch(
        items: Map<T, R>,
        batchSize: Int = 10,
        maxConcurrent: Int = 5,
        task: suspend (Map.Entry<T, R>) -> DatabaseResult<Unit>
    ): DatabaseResult<Unit> {
        val batches = items.chunkedMap(batchSize)
        val semaphore = Semaphore(maxConcurrent)
        return coroutineScope {
            batches.forEach { batch ->
                batch.map {
                    semaphore.withPermit {
                        async { task(it) }
                    }
                }.also {
                    // Wait for all tasks in this batch to complete
                    it.awaitAll()
                }
            }
            return@coroutineScope DatabaseResult.Success(Unit)
        }
    }

    fun <T> wrapResult(value: T?): DatabaseResult<T> {
        return if (value != null) {
            DatabaseResult.Success(value)
        } else {
            DatabaseResult.NotFound
        }
    }

    suspend fun <T> withContextDispatcher(block: suspend() -> T?): DatabaseResult<T>{
        return  wrapResult(withContext(dispatcher) {
            block()
        })
    }

    suspend fun <T> withContextDispatcherWithException(errorMessage: String, block: suspend () -> T?): DatabaseResult<T> {
        return try {
            wrapResult(withContext(dispatcher) {
                block()
            })
        } catch (e: Exception) {
            DatabaseResult.UnknownError(e, errorMessage)
        }
    }

    suspend inline fun <T, R> selectAll(
        errorMessage: String,
        crossinline queryBlock: suspend () -> List<T>,
        crossinline mapper: (T) -> R
    ): DatabaseResult<List<R>> {
        return withContextDispatcherWithException(errorMessage) {
            val results = queryBlock()
            if (results.isNotEmpty()) results.map { mapper(it) } else null
        }
    }

    // Method to perform a transaction
    abstract suspend fun <T> performTransaction(block: suspend () -> DatabaseResult<T>): DatabaseResult<T>

    // Method to close the database connection
    abstract fun close()
}