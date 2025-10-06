package io.github.kwvolt.japanesedictionary.util

object CoroutineEnvironment {
    var isTestEnvironment: Boolean = false
    var semaphoreCount: Int = 5
    var dbTimeOutMillis: Long = 5000
}