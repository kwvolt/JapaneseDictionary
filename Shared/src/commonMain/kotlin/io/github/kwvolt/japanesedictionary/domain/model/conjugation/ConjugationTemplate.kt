package io.github.kwvolt.japanesedictionary.domain.model.conjugation

enum class ConjugationTemplate(val idName: String, val displayName: String) {
    RU_CONJUGATION("RU_CONJUGATION", "る Verb Conjugation"),
    U_CONJUGATION("U_CONJUGATION", "う Verb Conjugation"),
    KURU_CONJUGATION("KURU_CONJUGATION", "くる Conjugation"),
    SURU_CONJUGATION("SURU_CONJUGATION", "する Conjugation")
}