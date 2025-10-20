package io.github.kwvolt.japanesedictionary.domain.model.word_class

enum class SubClass(val idName: String, val displayName: String) {
    //verb
    RU_VERB("RU_VERB", "る"),
    U_VERB("U_VERB", "う"),
    IRREGULAR("IRREGULAR", "Irregular"),

    // adjective
    I_ADJECTIVE("I_ADJECTIVE", "い"),
    NA_ADJECTIVE("NA_ADJECTIVE", "な"),

    // pronoun
    PERSONAL("PERSONAL", "Personal"),
    DEMONSTRATIVE("DEMONSTRATIVE", "Demonstrative"),
    INTERROGATIVE("INTERROGATIVE", "Interrogative"),

    //adverb
    MANNER("MANNER", "Manner"),
    TIME("TIME","Time"),
    FREQUENCY("FREQUENCY","Frequency"),
    DEGREE("DEGREE","Degree"),
    PLACE("PLACE","Place"),
    NEGATIVE("NEGATIVE","Negative"),

    // noun
    COMMON("COMMON","Common"),
    PROPER("PROPER","Proper"),
    COLLECTIVE("COLLECTIVE","Collective"),
    ABSTRACT("ABSTRACT", "Abstract")
}