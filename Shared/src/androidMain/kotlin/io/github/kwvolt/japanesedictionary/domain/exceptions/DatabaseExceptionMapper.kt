package io.github.kwvolt.japanesedictionary.domain.exceptions

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseError
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseErrorType
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

actual fun <T> mapToDatabaseException(itemKey: ItemKey, e: Throwable): DatabaseResult<T> {
    if (e is SQLiteConstraintException || e is SQLiteException) {
        val msg = e.message?.lowercase() ?: return DatabaseResult.UnknownError(e, "Unknown SQLite error")

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

    // Unknown or non-SQLite error
    return DatabaseResult.UnknownError(e, e.message)
}