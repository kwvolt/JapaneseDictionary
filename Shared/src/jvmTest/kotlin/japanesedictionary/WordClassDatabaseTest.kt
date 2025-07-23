package japanesedictionary

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository
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

    @Test
    fun tester () = runTest {
        val initializeWordClass = InitializeWordClass(databaseHandler, wordClassUpsert)
        val result = initializeWordClass.createWordClass()
        if(result.isFailure){
            println(result::class.java)
        }
        val mainList: DatabaseResult<List<MainClassContainer>> = wordClassBuilder.getMainClassList()
        if(mainList.isFailure){
            println(mainList::class.java)
        }
        when(mainList){
            is DatabaseResult.Success -> {
                mainList.value.forEach { main ->
                    print(main)
                    print(": ")
                    println(databaseHandler.queries.subClassQueries.selectAllByMainClassId(main.id).awaitAsList())
                }
            }
            else -> Unit
        }
    }

    @Test
    fun sanityCheck() {
        println("✅ This test ran.")
        Assertions.assertTrue(true)
    }
}