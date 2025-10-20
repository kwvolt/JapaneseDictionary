package japanesedictionary.domain.data.service

import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.database.initialize.InitializeConjugation
import io.github.kwvolt.japanesedictionary.domain.data.service.ConjugationServiceContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationMapBuilder
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationBy
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationForm
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationPolarity
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationWords
import io.github.kwvolt.japanesedictionary.util.CoroutineEnvironment
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
class TestConjugation {
    private lateinit var driver: SqlDriver
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var serviceContainer: ServiceContainer
    private lateinit var conjugationServiceContainer: ConjugationServiceContainer


    @BeforeEach
    fun setup() = runBlocking {
        driver = DriverFactory().createTestDriver()
        databaseHandler = DatabaseHandler(driver)
        serviceContainer = ServiceContainer(databaseHandler)
        conjugationServiceContainer = ConjugationServiceContainer(databaseHandler)

        CoroutineEnvironment.isTestEnvironment = true

    }
    @AfterEach
    fun cleaner(){
        databaseHandler.close()
    }

    @Test
    fun testInsertingConjugations () = runTest {
        val conjugationTemplateRepository =  conjugationServiceContainer.conjugationTemplateRepository

        val initializeConjugation = conjugationServiceContainer.getServices {
            InitializeConjugation(databaseHandler, conjugationUpsert, conjugationTemplateInserter)
        }

        initializeConjugation.initialize().getOrReturn<Unit> {
            when(it){
                is DatabaseResult.UnknownError -> error(it.exception.message ?: it.exception.stackTrace.toString())
                else -> error("failed to initialize conjugation")
            }
        }

        val conjugationTemplateId = conjugationTemplateRepository.selectId("KURU_CONJUGATION").getOrReturn<Unit> {
            when(it){
                is DatabaseResult.UnknownError -> error(it.exception.stackTrace)
                else -> error("failed to get conjugationTemplateId")
            }
        }

        val conjugationMapBuilder: ConjugationMapBuilder = conjugationServiceContainer.getServices {
            ConjugationMapBuilder(
                serviceContainer.dictionaryRepository,
                conjugationTemplateRepository,
                conjugationRepository,
                conjugationPatternRepository,
                conjugationPreprocessRepository,
                conjugationSuffixRepository,
                verbSuffixSwapRepository,
                conjugationOverrideRepository
            )
        }

        val primaryText: String = "来る"
        val kana: String = "くる"

        val result = conjugationMapBuilder.buildConjugationTemplate(primaryText, kana, conjugationTemplateId).getOrReturn<Unit> { error("failed to fetch conjugationMap") }


        fun getResult(word: ConjugationPolarity){
            when(word){
                is ConjugationPolarity.Both -> print("Positive: ${word.positive.text} | Negative: ${word.negative.text}")
                is ConjugationPolarity.Negative -> print("Negative: ${word.negative.text}")
                is ConjugationPolarity.Neither -> print(word.neither.text)
                is ConjugationPolarity.Positive -> print("Positive: ${word.positive.text}")
            }
            println()

        }

        fun getForm(form: ConjugationForm){
            when(form){
                is ConjugationForm.BothForm -> {
                    print("\tShort: ")
                    getResult(form.shortForm)
                    print("\tLong: ")
                    getResult(form.longForm)
                }
                is ConjugationForm.LongForm -> {
                    print("\tLong: ")
                    getResult(form.longForm)
                }
                is ConjugationForm.NeitherForm -> {
                    print("\t ")
                    getResult(form.neitherForm)
                }
                is ConjugationForm.ShortForm -> {
                    print("\tShort: ")
                    getResult(form.shortForm)
                }
            }
        }

        result[ConjugationBy.KANA]?.forEach { entry ->
            println("Pattern: ${entry.value.displayText}")
            when(val word = entry.value){
                is ConjugationWords.ConjugationWord -> {
                    println("Main:")
                    getForm(word.form)
                }
                is ConjugationWords.ConjugationWordWithVariant -> {
                    println("Main: ")
                    getForm(word.form)
                    println("variant:")
                    word.variants.values.forEach { forms ->
                        println("\t Pattern: ${forms.displayText}")
                        print("\t")
                        getForm(forms.form)
                    }
                }
            }
        }
    }
}