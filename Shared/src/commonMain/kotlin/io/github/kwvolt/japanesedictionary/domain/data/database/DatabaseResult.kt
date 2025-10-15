package io.github.kwvolt.japanesedictionary.domain.data.database

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey

sealed class DatabaseResult<out T> {
    data class Success<T>(val value: T) : DatabaseResult<T>()
    data object NotFound: DatabaseResult<Nothing>()
    data class InvalidInput(val key: ItemKey, val error: DatabaseError): DatabaseResult<Nothing>()
    data class UnknownError(val exception: Throwable, val message: String? = null) : DatabaseResult<Nothing>() {
        constructor(exception: Throwable) : this(exception, exception.message ?: "Unknown Error")
    }

    val isSuccess: Boolean get() = this is Success<T>

    val isFailure: Boolean get() = !isSuccess

    inline fun <R> flatMap(transform: (T) -> DatabaseResult<R>): DatabaseResult<R> = when (this) {
        is Success -> transform(value)
        is UnknownError -> this
        is InvalidInput -> this
        NotFound -> NotFound
    }

    inline fun <R> map(transform: (T) -> R): DatabaseResult<R> = when (this) {
        is Success -> Success(transform(value))
        is UnknownError -> this
        is InvalidInput -> this
        NotFound -> NotFound
    }

    inline fun toUnit(transform: (T) -> Unit = {}): DatabaseResult<Unit> = when (this) {
        is Success -> Success(transform(value))
        is UnknownError -> this
        is InvalidInput -> this
        NotFound -> NotFound
    }

    fun <B> mapErrorTo(): DatabaseResult<B> = when (this) {
        is Success -> UnknownError(IllegalStateException("mapErrorTo called on Success").fillInStackTrace())
        is UnknownError -> this
        is InvalidInput -> this
        NotFound -> NotFound
    }

    inline fun <R> returnOnFailure(
        errorTo : (DatabaseResult<R>) -> DatabaseResult<Nothing>
    ) : DatabaseResult<T>{
        return when (this) {
            is Success -> this
            else -> errorTo(this.mapErrorTo())
        }
    }

    inline fun <R> getOrReturn(
        errorTo : (DatabaseResult<R>) -> Nothing
    ) : T{
        return when (this) {
            is Success -> value
            else -> errorTo(this.mapErrorTo())
        }
    }
}

inline fun <T, R> DatabaseResult<T>.getOrDefaultOrReturn(
    onNotFound: () -> T,
    errorTo: (DatabaseResult<R>) -> Nothing
): T {
    return when (this) {
        is DatabaseResult.Success -> value
        is DatabaseResult.NotFound -> onNotFound()
        else -> errorTo(this.mapErrorTo())
    }
}

data class DatabaseError(val type: DatabaseErrorType)