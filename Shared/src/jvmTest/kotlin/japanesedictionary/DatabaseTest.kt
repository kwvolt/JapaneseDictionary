package japanesedictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.service.WordClassBuilder
import io.github.kwvolt.japanesedictionary.domain.service.WordClassUpsert
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
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
        println("hello")

        val result = databaseHandler.performTransaction {
            // particle
            println(databaseHandler.queries.wordClassQueries.initializeWordClass("PARTICLE", "Particle").awaitAsOneOrNull())
            // adverb
            val adverb = databaseHandler.queries.wordClassQueries.initializeWordClass("ADVERB", "Adverb").awaitAsOneOrNull()
            val adverbMap = mapOf(
                "MANNER" to "Manner",
                "TIME" to "Time",
                "FREQUENCY" to "Frequency",
                "DEGREE" to "Degree",
                "PLACE" to "Place",
                "NEGATIVE" to "Negative"
            )
            if(adverb != null) {
                for ((idName, displayText) in adverbMap.entries) {
                    databaseHandler.queries.subClassQueries.insertSubClassLinkToMainClass(
                        idName,
                        displayText,
                        adverb
                    ).awaitAsOneOrNull()
                }
            }

            databaseHandler.queries.wordClassQueries.initializeWordClass("ADJECTIVAL_NOUN", "Adjectival Noun").awaitAsOneOrNull()

            databaseHandler.queries.wordClassQueries.initializeWordClass("CONJUNCTION", "Conjunction").awaitAsOneOrNull()

            databaseHandler.queries.wordClassQueries.initializeWordClass("INTERJECTION","Interjection").awaitAsOneOrNull()

            databaseHandler.queries.wordClassQueries.initializeWordClass("NUMBER", "Number").awaitAsOneOrNull()

            // pronoun
            val pronoun = databaseHandler.queries.wordClassQueries.initializeWordClass("PRONOUN", "Pronoun").awaitAsOneOrNull()
            val pronounMap = mapOf(
                "PERSONAL" to "Personal",
                "DEMONSTRATIVE" to "Demonstrative",
                "INTERROGATIVE" to "Interrogative"
            )
            if(pronoun != null) {
                for ((idName, displayText) in pronounMap.entries) {
                    databaseHandler.queries.subClassQueries.insertSubClassLinkToMainClass(
                        idName,
                        displayText,
                        pronoun
                    ).awaitAsOneOrNull()
                }
            }

            // noun
            val noun = databaseHandler.queries.wordClassQueries.initializeWordClass("NOUN", "Noun").awaitAsOneOrNull()
            val nounMap = mapOf(
                "COMMON" to "Common",
                "PROPER" to "Proper",
                "COLLECTIVE" to "Collective",
                "ABSTRACT" to "Abstract"
            )
            if(noun != null) {
                for ((idName, displayText) in nounMap.entries) {
                    databaseHandler.queries.subClassQueries.insertSubClassLinkToMainClass(
                        idName,
                        displayText,
                        noun
                    ).awaitAsOneOrNull()
                }
            }

            // verb
            val verb  = databaseHandler.queries.wordClassQueries.initializeWordClass("VERB", "Verb").awaitAsOneOrNull()
            val verbMap = mapOf("RU_VERB" to "る", "U_VERB" to "う", "IRREGULAR" to "Irregular")
            if(verb != null) {
                for ((idName, displayText) in verbMap.entries) {
                    databaseHandler.queries.subClassQueries.insertSubClassLinkToMainClass(
                        idName,
                        displayText,
                        verb
                    ).awaitAsOneOrNull()
                }
            }

            val adjective  = databaseHandler.queries.wordClassQueries.initializeWordClass("ADJECTIVE", "Adjective").awaitAsOneOrNull()
            val adjectiveMap = mapOf("I_ADJECTIVE" to "い", "NA_ADJECTIVE" to "な", "IRREGULAR" to "Irregular")
            if(adjective != null) {
                for ((idName, displayText) in adjectiveMap.entries) {
                    println(idName + displayText)
                    databaseHandler.queries.subClassQueries.insertSubClassLinkToMainClass(
                        idName,
                        displayText,
                        adjective
                    ).awaitAsOneOrNull()
                }
            }


            DatabaseResult.Success(Unit)
        }

        if(result.isFailure){
            println(result::class.java)
        }

        val mainlist: DatabaseResult<List<MainClassContainer>> = MainClassRepository( databaseHandler, databaseHandler.queries.mainClassQueries).selectAllMainClass()

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