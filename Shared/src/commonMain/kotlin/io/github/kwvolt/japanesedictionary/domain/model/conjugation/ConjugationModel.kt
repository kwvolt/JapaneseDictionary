package io.github.kwvolt.japanesedictionary.domain.model.conjugation


enum class ConjugationBy(private val value: String) {
    PRIMARY_TEXT("PRIMARY_TEXT"),
    KANA("KANA"), ;

    override fun toString(): String = value

    companion object {
        private val lookup = entries.associateBy { it.value.lowercase() }
        fun fromValue(value: String) = lookup[value.lowercase()]
    }
}

sealed class ConjugationWords(open val displayText: String, open val form: ConjugationForm, open val description: String? = null){
    data class ConjugationWord(
        override val displayText: String,
        override val form: ConjugationForm,
        override val description: String? = null
    ): ConjugationWords(displayText, form, description)
    data class ConjugationWordWithVariant(
        override val displayText: String,
        override val form: ConjugationForm,
        override val description: String? = null,
        val variants: Map<Long, ConjugationWord>
    ): ConjugationWords(displayText, form, description)
}

sealed class ConjugationForm{
    data class ShortForm(val shortForm: ConjugationPolarity): ConjugationForm()
    data class LongForm(val longForm: ConjugationPolarity): ConjugationForm()
    data class NeitherForm(val neitherForm: ConjugationPolarity): ConjugationForm()
    data class BothForm(val shortForm: ConjugationPolarity, val longForm: ConjugationPolarity): ConjugationForm()
}

sealed class ConjugationPolarity {
    data class Positive(val positive: ConjugatedWord): ConjugationPolarity()
    data class Negative(val negative: ConjugatedWord): ConjugationPolarity()
    data class Neither(val neither: ConjugatedWord): ConjugationPolarity()
    data class Both(val positive: ConjugatedWord, val negative: ConjugatedWord): ConjugationPolarity()
}

data class ConjugatedWord(val text: String, val overrideNote: String? = null)