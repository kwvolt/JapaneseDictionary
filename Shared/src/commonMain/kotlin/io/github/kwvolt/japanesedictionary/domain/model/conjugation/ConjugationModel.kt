package io.github.kwvolt.japanesedictionary.domain.model.conjugation

data class ConjugationModel(
    val id: Long,
    val conjugationTemplateName: String,
    val conjugationPatterns: List<ConjugationPatternModel>
)

data class ConjugationPatternModel(
    val id: Long,
    val idName: String,
    val patternName: String,
    val conjugatedWord: String,
    val variantConjugatedWords: List<String>? = null
)
