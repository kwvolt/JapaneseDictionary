package io.github.kwvolt.japanesedictionary.util

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.ui.model.ScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateErrorCopyable
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <T, S> MutableStateFlow<S>.handleDatabaseResult(
    functionName: String,
    result: DatabaseResult<T>,
    crossinline copyWithError: (ScreenStateUnknownError?) -> S,
    onSuccess: (T) -> Unit
) {
    when (result) {
        is DatabaseResult.Success -> onSuccess(result.value)

        is DatabaseResult.NotFound -> {
            this.update {
                copyWithError(
                    ScreenStateUnknownError(
                        NoSuchElementException("Not found at $functionName").fillInStackTrace()
                    )
                )
            }
        }

        is DatabaseResult.UnknownError -> {
            this.update {
                copyWithError(ScreenStateUnknownError(result.exception))
            }
        }

        else -> {
            this.update {
                copyWithError(
                    ScreenStateUnknownError(
                        IllegalStateException("Unexpected result: ${result::class} at $functionName").fillInStackTrace()
                    )
                )
            }
        }
    }
}

inline fun <T, S> MutableStateFlow<S>.handleResultWithErrorCopy(
    functionName: String,
    result: DatabaseResult<T>,
    onSuccess: (T) -> Unit
) where S : ScreenState, S : ScreenStateErrorCopyable<S> {
    handleDatabaseResult(
        functionName = functionName,
        result = result,
        copyWithError = value::copyWithError,
        onSuccess = onSuccess
    )
}