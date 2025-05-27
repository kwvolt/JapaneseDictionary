package japanesedictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordClassBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordClassUpsert
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DatabaseTester {
    private lateinit var driver: SqlDriver
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var wordClassRepository: WordClassRepository
    private lateinit var mainClassRepository: MainClassRepository
    private lateinit var subClassRepository: SubClassRepository

    private lateinit var wordClassUpsert: WordClassUpsert
    private lateinit var wordClassBuilder: WordClassBuilder
    @BeforeEach
    fun setup() = runBlocking {
        driver = DriverFactory().createTestDriver()
        databaseHandler = DatabaseHandler(driver)
        wordClassRepository = WordClassRepository(databaseHandler, databaseHandler.queries.wordClassQueries)
        mainClassRepository = MainClassRepository(databaseHandler, databaseHandler.queries.mainClassQueries)
        subClassRepository = SubClassRepository(databaseHandler, databaseHandler.queries.subClassQueries)


        wordClassUpsert = WordClassUpsert(databaseHandler, mainClassRepository, subClassRepository, wordClassRepository)
        wordClassBuilder = WordClassBuilder(mainClassRepository, subClassRepository)
    }
    @AfterEach
    fun cleaner(){
        databaseHandler.close()
    }

    suspend fun createWordClass(){
        val result = databaseHandler.performTransaction {
            val particle = wordClassUpsert.initializeWordClass("PARTICLE", "Particle")
            if(particle.isFailure) { return@performTransaction particle.mapErrorTo<Unit, Unit>() }


            val adverb = wordClassUpsert.initializeWordClassWithSubClasses(
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

            val pronoun = wordClassUpsert.initializeWordClassWithSubClasses(
                "PRONOUN", "Pronoun", mapOf(
                    "PERSONAL" to "Personal",
                    "DEMONSTRATIVE" to "Demonstrative",
                    "INTERROGATIVE" to "Interrogative"
                )
            )
            if(pronoun.isFailure){
                return@performTransaction pronoun.mapErrorTo<Unit, Unit>()
            }

            val noun = wordClassUpsert.initializeWordClassWithSubClasses(
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

            val verb = wordClassUpsert.initializeWordClassWithSubClasses(
                "VERB",
                "Verb",
                mapOf("RU_VERB" to "る", "U_VERB" to "う", "IRREGULAR" to "Irregular")
            )
            if(verb.isFailure){
                return@performTransaction verb.mapErrorTo<Unit, Unit>()
            }

            val adjective = wordClassUpsert.initializeWordClassWithSubClasses(
                "ADJECTIVE",
                "Adjective",
                mapOf("I_ADJECTIVE" to "い", "NA_ADJECTIVE" to "な", "IRREGULAR" to "Irregular")
            )
            if(adjective.isFailure){
                return@performTransaction adjective.mapErrorTo<Unit, Unit>()
            }
            DatabaseResult.Success(Unit)
        }

        if(result.isFailure){
            println(result::class.java)
        }

        val mainlist: DatabaseResult<List<MainClassContainer>> = wordClassBuilder.getMainClassList()

        if(mainlist.isFailure){
            println(mainlist::class.java)
        }

        return when(mainlist){
            is DatabaseResult.Success -> {
                mainlist.value.forEach { main ->
                    print(main)
                    print(": ")
                    println(databaseHandler.queries.subClassQueries.selectAllSubClassByMainClassId(main.id).awaitAsList())
                }
            }
            else -> return Unit
        }
    }


    @Test
    fun tester () = runTest {
        createWordClass()
    }

    @Test
    fun sanityCheck() {
        println("✅ This test ran.")
        Assertions.assertTrue(true)
    }
}