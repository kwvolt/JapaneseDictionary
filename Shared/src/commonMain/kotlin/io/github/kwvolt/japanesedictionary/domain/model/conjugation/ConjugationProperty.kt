package io.github.kwvolt.japanesedictionary.domain.model.conjugation


enum class StemRule(private val value: String){
    DROP_RU("DROP_RU"),
    REPLACE_SUFFIX("REPLACE_SUFFIX"),
    NOTHING("NOTHING");

    override fun toString(): String = value

    companion object {
        private val lookup = entries.associateBy { it.value.lowercase() }
        fun fromValue(value: String) = lookup[value.lowercase()]
    }
}

enum class ConjugationOverrideProperty(private val value: String){
    STEM_REPLACEMENT("STEM_REPLACEMENT"),
    STEM_RULE("STEM_RULE"),
    SUFFIX_OVERRIDE("SUFFIX_OVERRIDE"),
    IRREGULAR("IRREGULAR"),
    IRREGULAR_REPLACEMENT("IRREGULAR_REPLACEMENT"),
    VERB_SUFFIX_SWAP("SUFFIX_SWAP");
    override fun toString(): String = value
    companion object {
        private val lookup = entries.associateBy { it.value.lowercase() }
        fun fromValue(value: String) = lookup[value.lowercase()]
    }
}