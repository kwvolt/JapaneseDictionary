package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.ItemKey

/**
 * Represents the result of a validation operation.
 *
 * This sealed class is used to express different possible outcomes of validating the values.
 *
 * @param T The type of value returned in case of a successful result.
 */
sealed class ValidationResult<out T> {
    data class Success<T>(val value: T) : ValidationResult<T>()
    data class InvalidInput(val key: ItemKey, val error: List<ValidationError>): ValidationResult<Nothing>()
    data class InvalidInputMap(val errors: Map<ItemKey, List<ValidationError>>) : ValidationResult<Nothing>()
    data object NotFound : ValidationResult<Nothing>()
    data class UnknownError(val exception: Throwable, val message: String?) : ValidationResult<Nothing>()

    inline fun <R> flatMap(transform: (T) -> ValidationResult<R>): ValidationResult<R> = when (this) {
        is ValidationResult.Success -> transform(value)
        is ValidationResult.NotFound -> this
        is ValidationResult.UnknownError -> this
        is ValidationResult.InvalidInput -> this
        is ValidationResult.InvalidInputMap -> this
    }
}

data class ValidationError(val type: ValidationType)