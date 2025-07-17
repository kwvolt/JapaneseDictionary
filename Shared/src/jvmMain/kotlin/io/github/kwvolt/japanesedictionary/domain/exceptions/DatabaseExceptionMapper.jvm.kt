package io.github.kwvolt.japanesedictionary.domain.exceptions

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseError
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseErrorType
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

actual fun <T> mapToDatabaseException(itemKey: ItemKey, e: Throwable): DatabaseResult<T> {
    val msg = e.message?.lowercase() ?: return DatabaseResult.UnknownError(e, "UnknownError")

    return when {
        "unique constraint failed" in msg ->
            DatabaseResult.InvalidInput(itemKey, DatabaseError(DatabaseErrorType.UNIQUE))

        "not null constraint failed" in msg ->
            DatabaseResult.InvalidInput(itemKey, DatabaseError(DatabaseErrorType.NOT_NULL))

        "check constraint failed" in msg ->
            DatabaseResult.InvalidInput(itemKey, DatabaseError(DatabaseErrorType.CHECK))

        "foreign key constraint failed" in msg ->
            DatabaseResult.InvalidInput(itemKey, DatabaseError(DatabaseErrorType.FOREIGN_KEY))

        else ->
            DatabaseResult.UnknownError(e, msg)
    }
}