package io.github.kwvolt.japanesedictionary.domain.model.word_class

enum class MainClass(val idName: String, val displayName: String) {
    VERB("VERB", "Verb"),
    NOUN("NOUN", "Noun"),
    ADJECTIVE("ADJECTIVE", "Adjective"),
    PRONOUN("PRONOUN", "Pronoun"),
    ADVERB("ADVERB", "Adverb"),
    CONJUNCTION("CONJUNCTION", "Conjunction"),
    INTERJECTION("INTERJECTION", "Interjection"),
    PARTICLE("PARTICLE", "Particle"),
    NUMBER("NUMBER", "Number"),
    ADJECTIVAL_NOUN("ADJECTIVAL_NOUN", "Adjectival Noun")
}