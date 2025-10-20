package io.github.kwvolt.japanesedictionary.domain.model.conjugation

enum class ConjugationPattern(val idName: String, val displayName: String, val descriptionText: String? = null) {
    CONJUNCTIVE("CONJUNCTIVE", "Conjunctive"),
    POLITE("POLITE", "Polite"),
    TE_FORM("TE_FORM", "Te-form"),
    TA_FORM("TA_FORM", "Ta-form"),
    PRESENT_NEGATIVE("PRESENT_NEGATIVE", "Present Negative"),
    PAST_NEGATIVE("PAST_NEGATIVE", "Past Negative"),
    VOLITIONAL("VOLITIONAL", "Volitional"),
    PASSIVE("PASSIVE", "Passive"),
    CAUSATIVE("CAUSATIVE", "Causative"),
    CAUSATIVE_PASSIVE("CAUSATIVE_PASSIVE","Causative-passive"),
    IMPERATIVE("IMPERATIVE", "Imperative"),
    POTENTIAL("POTENTIAL", "Potential"),
    POTENTIAL_RA_LESS("POTENTIAL_RA_LESS","Potential (ら-less)"),
    CONDITIONAL("CONDITIONAL","Conditional"),
    TARA_CONDITIONAL("TARA_CONDITIONAL","たら Conditional"),
    TO_CONDITIONAL("TO_CONDITIONAL","と Conditional"),
    PROGRESSIVE("PROGRESSIVE", "Progressive")
    
}