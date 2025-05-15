package io.github.kwvolt.japanesedictionary

import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DatabaseTester {
    lateinit var driver: SqlDriver
    lateinit var databaseHandler: DatabaseHandler
    lateinit var wordClassRepository: WordClassRepository

    @Before
    fun initialize()  = runTest {
        driver = DriverFactory().createTestDriver()
        databaseHandler = DatabaseHandler(driver)
        wordClassRepository = WordClassRepository(databaseHandler)

    }

    @After
    fun cleaner(){
        databaseHandler.close()
    }


    @Test
    fun tester () = runTest {

        coroutineScope {
            val particle = wordClassRepository.initializeMainClassWithSubClasses("PARTICLE", "Particle")

            val adverb = wordClassRepository.initializeMainClassWithSubClasses(
                "ADVERB", "Adverb",
                mapOf(
                    "MANNER" to "Manner",
                    "TIME" to "Time",
                    "FREQUENCY" to "Frequency",
                    "DEGREE" to "Degree",
                    "PLACE" to "Place",
                    "NEGATIVE" to "Negative"
                )
            )

            println("Meep")

            val adjectivalNoun = wordClassRepository.initializeMainClassWithSubClasses(
                "ADJECTIVAL_NOUN",
                "Adjectival Noun"
            )
            val conjunction =
                wordClassRepository.initializeMainClassWithSubClasses("CONJUNCTION", "Conjunction")
            val interjection =
                wordClassRepository.initializeMainClassWithSubClasses(
                    "INTERJECTION",
                    "Interjection"
                )
            val number = wordClassRepository.initializeMainClassWithSubClasses("NUMBER", "Number")

            val pronoun = wordClassRepository.initializeMainClassWithSubClasses(
                "PRONOUN", "Pronoun",
                mapOf(
                    "PERSONAL" to "Personal",
                    "DEMONSTRATIVE" to "Demonstrative",
                    "INTERROGATIVE" to "Interrogative"
                )
            )
            val noun = wordClassRepository.initializeMainClassWithSubClasses(
                "NOUN", "Noun",
                mapOf(
                    "COMMON" to "Common",
                    "PROPER" to "Proper",
                    "COLLECTIVE" to "Collective",
                    "ABSTRACT" to "Abstract"
                )
            )
            val verb = wordClassRepository.initializeMainClassWithSubClasses(
                "VERB",
                "Verb",
                mapOf("RU_VERB" to "る", "U_VERB" to "う", "IRREGULAR" to "Irregular")
            )
            val adjective = wordClassRepository.initializeMainClassWithSubClasses(
                "ADJECTIVE", "Adjective",
                mapOf("I_ADJECTIVE" to "い", "NA_ADJECTIVE" to "な", "IRREGULAR" to "Irregular")
            )


            val mainlist: List<MainClassContainer> = wordClassRepository.selectAllMainClass()

            println(mainlist)

            mainlist.forEach { main ->
                println(wordClassRepository.selectAllSubClassByMainClassId(main.id))
            }
        }
    }
}