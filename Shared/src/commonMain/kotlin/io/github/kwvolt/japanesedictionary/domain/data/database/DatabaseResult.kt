package io.github.kwvolt.japanesedictionary.domain.data.database

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidationKey
import io.github.kwvolt.japanesedictionary.domain.data.validation.ValidationType

/**
 * Represents the result of a database operation.
 *
 * This sealed class is used to express different possible outcomes of interacting with the database.
 *
 * @param T The type of value returned in case of a successful result.
 */
sealed class DatabaseResult<out T> {

    /**
     * Represents a successful database operation.
     *
     * @param T The type of the successful result.
     * @property value The value returned from the database.
     */
    data class Success<T>(val value: T) : DatabaseResult<T>()

    /**
     * Indicates that the requested item was not found in the database.
     */
    data object NotFound : DatabaseResult<Nothing>()

    /**
     * Indicates that the input provided to the database operation was invalid.
     *
     * @property invalidType The type of validation that failed.
     */
    data class InvalidInput(val invalidType: ValidationType) : DatabaseResult<Nothing>()

    data class InvalidInputMap(val errors: Map<ValidationKey, List<InvalidInput>>) : DatabaseResult<Nothing>()

    /**
     * Represents an unknown error that occurred during the database operation.
     *
     * @property exception The exception that was thrown.
     * @property message A human-readable message describing the error.
     */
    data class UnknownError(val exception: Throwable, val message: String) : DatabaseResult<Nothing>()

    // Whether the result is a success
    val isSuccess: Boolean get() = this is Success<T>

    // Whether the result is a failure (NotFound, InvalidInput, or UnknownError)
    val isFailure: Boolean get() = !isSuccess

    /**
     * Chains another operation that returns a [DatabaseResult].
     */
    inline fun <R> flatMap(transform: (T) -> DatabaseResult<R>): DatabaseResult<R> = when (this) {
        is Success -> transform(value)
        is NotFound -> NotFound
        is InvalidInput -> this
        is UnknownError -> this
        is InvalidInputMap -> this
    }

    fun <A, B> mapErrorTo(): DatabaseResult<B> = when (this) {
        is Success -> throw IllegalStateException("mapErrorTo called on Success")
        is NotFound -> NotFound
        is InvalidInput -> this
        is UnknownError -> this
        is InvalidInputMap -> this
    }
}