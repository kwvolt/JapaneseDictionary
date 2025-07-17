package io.github.kwvolt.japanesedictionary.domain.exceptions

import io.github.kwvolt.japanesedictionary.domain.data.ItemKey
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

expect fun <T> mapToDatabaseException (itemKey: ItemKey, e: Throwable): DatabaseResult<T>