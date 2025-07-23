package japanesedictionary

import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.EntryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordClassBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordClassUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestInsertingDictionary {
    private lateinit var driver: SqlDriver
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var dictionaryRepository: EntryRepositoryInterface



    private lateinit var wordClassRepository: WordClassRepositoryInterface
    private lateinit var mainClassRepository: MainClassRepositoryInterface
    private lateinit var subClassRepository: SubClassRepositoryInterface

    private lateinit var wordClassUpsert: WordClassUpsert
    private lateinit var wordClassBuilder: WordClassBuilder

    private lateinit var wordEntryFormUpsert: WordEntryFormUpsert

    @BeforeEach
    fun setup() = runBlocking {
        driver = DriverFactory().createTestDriver()


        databaseHandler = DatabaseHandler(driver)
        dictionaryRepository = EntryRepository(databaseHandler, databaseHandler.queries.dictionaryEntryQueries)

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

    @Test
    fun testCreatingDictionary () = runTest {
        val initializeWordClass = InitializeWordClass(databaseHandler, wordClassUpsert)
        initializeWordClass.createWordClass()

        val wordClassResult = wordClassRepository.selectIdByMainClassIdNameAndSubClassIdName("VERB","RU_VERB")
        var wordClassId: Long? = null
        when(wordClassResult){
            is DatabaseResult.Success<Long> -> wordClassId = wordClassResult.value
            else -> Unit
        }

        val result = databaseHandler.performTransaction {
            if (wordClassId != null) {
                val result1 = databaseHandler.wrapQuery("1"){dictionaryRepository.insert(wordClassId, "Meep")}

                if(result1.isFailure){
                    return@performTransaction result1
                }
                val result2 = databaseHandler.wrapQuery("2"){dictionaryRepository.insert(99, "NOPE")}

                if(result2.isFailure){
                    return@performTransaction result2
                }
            }
            DatabaseResult.Success(Unit)
        }
        println("hello")
        println(result::class.java)
        if(result.isFailure){
            println(result::class.java)
            if (result is DatabaseResult.UnknownError){
                println(result.message)
                println(result.exception.message)
                println(result.exception::class.java)

            }
            else if (result is DatabaseResult.InvalidInput){
                println(result.key)
                println(result.error.type)
            }
        }
    }
}