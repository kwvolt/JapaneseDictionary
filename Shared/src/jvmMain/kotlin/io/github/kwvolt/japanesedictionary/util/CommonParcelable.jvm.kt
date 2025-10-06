package io.github.kwvolt.japanesedictionary.util
// jvmMain

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual annotation class CommonParcelize

actual interface CommonParcelable

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.BINARY)
actual annotation class CommonRawValue