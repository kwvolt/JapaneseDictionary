package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult.NotFound
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult.Success
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult.UnknownError

sealed class ValidationResult<out T> {
    data class Success<T>(val value: T) : ValidationResult<T>()
    data class InvalidInput(val key: ValidationKey, val error: List<ValidationError>): ValidationResult<Nothing>()
    data class InvalidInputMap(val errors: Map<ValidationKey, List<ValidationError>>) : ValidationResult<Nothing>()
    data object NotFound : ValidationResult<Nothing>()
    data class UnknownError(val exception: Throwable?, val message: String?) : ValidationResult<Nothing>()

    inline fun <R> flatMap(transform: (T) -> ValidationResult<R>): ValidationResult<R> = when (this) {
        is ValidationResult.Success -> transform(value)
        is ValidationResult.NotFound -> this
        is ValidationResult.UnknownError -> this
        is ValidationResult.InvalidInput -> this
        is ValidationResult.InvalidInputMap -> this
    }
}

data class ValidationError(val type: ValidationType)

sealed class ValidationKey {
    data class DataItem(val id: String) : ValidationKey()
    data class FormItem(val key: String) : ValidationKey()
}