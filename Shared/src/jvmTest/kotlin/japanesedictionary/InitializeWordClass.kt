package japanesedictionary

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassUpsert

class InitializeWordClass(
    private val databaseHandler: DatabaseHandlerBase,
    private val wordClassUpsert: WordClassUpsert,
    ) {
    suspend fun createWordClass(): DatabaseResult<Unit>{
        val result = databaseHandler.performTransaction {
            val particle = wordClassUpsert.initializeWordClass("PARTICLE", "Particle")
            if(particle.isFailure) { return@performTransaction particle.mapErrorTo<Unit, Unit>() }

            val adverb = wordClassUpsert.initializeWordClass(
                "ADVERB", "Adverb", mapOf(
                    "MANNER" to "Manner",
                    "TIME" to "Time",
                    "FREQUENCY" to "Frequency",
                    "DEGREE" to "Degree",
                    "PLACE" to "Place",
                    "NEGATIVE" to "Negative"
                )
            )
            if(adverb.isFailure) { return@performTransaction adverb.mapErrorTo<Unit, Unit>() }

            val adjectivalNoun = wordClassUpsert.initializeWordClass("ADJECTIVAL_NOUN", "Adjectival Noun")
            if(adjectivalNoun.isFailure) { return@performTransaction adjectivalNoun.mapErrorTo<Unit, Unit>() }

            val conjunction = wordClassUpsert.initializeWordClass("CONJUNCTION", "Conjunction")
            if(conjunction.isFailure) { return@performTransaction conjunction.mapErrorTo<Unit, Unit>() }

            val interjection = wordClassUpsert.initializeWordClass("INTERJECTION", "Interjection")
            if(interjection.isFailure) { return@performTransaction interjection.mapErrorTo<Unit, Unit>() }

            val number = wordClassUpsert.initializeWordClass("NUMBER", "Number")
            if(number.isFailure) { return@performTransaction number.mapErrorTo<Unit, Unit>() }

            val pronoun = wordClassUpsert.initializeWordClass(
                "PRONOUN", "Pronoun", mapOf(
                    "PERSONAL" to "Personal",
                    "DEMONSTRATIVE" to "Demonstrative",
                    "INTERROGATIVE" to "Interrogative"
                )
            )
            if(pronoun.isFailure){
                return@performTransaction pronoun.mapErrorTo<Unit, Unit>()
            }

            val noun = wordClassUpsert.initializeWordClass(
                "NOUN", "Noun", mapOf(
                    "COMMON" to "Common",
                    "PROPER" to "Proper",
                    "COLLECTIVE" to "Collective",
                    "ABSTRACT" to "Abstract"
                )
            )
            if(noun.isFailure){
                return@performTransaction noun.mapErrorTo<Unit, Unit>()
            }

            val verb = wordClassUpsert.initializeWordClass(
                "VERB",
                "Verb",
                mapOf("RU_VERB" to "る", "U_VERB" to "う", "IRREGULAR" to "Irregular")
            )
            if(verb.isFailure){
                return@performTransaction verb.mapErrorTo<Unit, Unit>()
            }

            val adjective = wordClassUpsert.initializeWordClass(
                "ADJECTIVE",
                "Adjective",
                mapOf("I_ADJECTIVE" to "い", "NA_ADJECTIVE" to "な", "IRREGULAR" to "Irregular")
            )
            if(adjective.isFailure){
                return@performTransaction adjective.mapErrorTo<Unit, Unit>()
            }
            DatabaseResult.Success(Unit)
        }

        return result
    }

}