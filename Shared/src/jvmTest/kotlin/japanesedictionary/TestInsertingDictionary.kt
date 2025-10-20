package japanesedictionary

import app.cash.sqldelight.db.SqlDriver
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DriverFactory
import io.github.kwvolt.japanesedictionary.domain.data.service.ServiceContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidUpsertResult
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.util.CoroutineEnvironment
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestInsertingDictionary {
    private lateinit var driver: SqlDriver
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var serviceContainer: ServiceContainer


    @BeforeEach
    fun setup() = runBlocking {
        driver = DriverFactory().createTestDriver()
        databaseHandler = DatabaseHandler(driver)
        serviceContainer = ServiceContainer(databaseHandler)
        CoroutineEnvironment.isTestEnvironment = true

    }
    @AfterEach
    fun cleaner(){
        databaseHandler.close()
    }

    @Test
    fun testCreatingDictionary () = runTest {
        val formItemManager = FormItemManager()
        val wordClassItem: WordClassItem = getInitializedWordClassItem()
        assertEquals("VERB", wordClassItem.chosenMainClass.idName)
        assertEquals("RU_VERB", wordClassItem.chosenSubClass.idName)

        val wordEntryFormUpsertValidation = serviceContainer.wordEntryFormUpsertValidation
        val wordEntryFormData = buildWordEntryForm(
            wordClassItem,
            "日本語",
            listOf(),
            listOf(
                SectionForm(
                    "meeo",
                    listOf("にほんご", "にほん", "にほ", "に"),
                    listOf()
                )
            ),
            formItemManager
        )

        val result: ValidUpsertResult<Long> = wordEntryFormUpsertValidation.wordEntryForm(wordEntryFormData, emptyList())
        val fetchResult = when(result){
            is ValidUpsertResult.Success -> {
                assert(true)
                serviceContainer.wordEntryFormBuilder.buildDetailedFormData(result.value, formItemManager)
            }
            else -> error("Expected successful insert")
        }

        when(fetchResult){
            is DatabaseResult.Success ->
                assertEquals("日本語", fetchResult.value.primaryTextInput.inputTextValue)
            else -> error("Failed to fetch result")

        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["nope", ""])
    fun testFailingKana (input: String) = runTest {
        val formItemManager = FormItemManager()
        val wordClassItem: WordClassItem = getInitializedWordClassItem()
        val wordEntryFormUpsertValidation = serviceContainer.wordEntryFormUpsertValidation
        val wordEntryFormData = buildWordEntryForm(
            wordClassItem,
            "日本語",
            listOf(),
            listOf(
                SectionForm(
                    "meeo",
                    listOf("にほんご", "にほん", "にほ", "に", input),
                    listOf()
                )
            ),
            formItemManager
        )

        val result: ValidUpsertResult<Long> = wordEntryFormUpsertValidation.wordEntryForm(wordEntryFormData, emptyList())
        when(result){
            is ValidUpsertResult.Success -> {
                assert(false)
            }
            else -> assert(true)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["nope", ""])
    fun testFailingPrimaryText (input: String) = runTest {
        val formItemManager = FormItemManager()
        val wordClassItem: WordClassItem = getInitializedWordClassItem()
        val wordEntryFormUpsertValidation = serviceContainer.wordEntryFormUpsertValidation
        val wordEntryFormData = buildWordEntryForm(
            wordClassItem,
            input,
            listOf(),
            listOf(
                SectionForm(
                    "meeo",
                    listOf("にほんご", "にほん", "にほ", "に"),
                    listOf()
                )
            ),
            formItemManager
        )

        val result: ValidUpsertResult<Long> = wordEntryFormUpsertValidation.wordEntryForm(wordEntryFormData, emptyList())
        when(result){
            is ValidUpsertResult.Success -> {
                assert(false)
            }
            else -> assert(true)
        }
    }

    private suspend fun getInitializedWordClassItem(mainClassIdName: String = "VERB", subClassIdName: String = "RU_VERB"): WordClassItem {
        val initializeWordClass = InitializeWordClass(databaseHandler, serviceContainer.wordClassUpsert)
        initializeWordClass.createWordClass()
        val result = serviceContainer.wordClassFetcher.fetchWordClassItem(mainClassIdName, subClassIdName)
        return when(result) {
            is DatabaseResult.Success -> result.value
            else -> error("Expected word class to be found, but got: $result")
        }
    }

    private fun buildWordEntryForm(
        wordClassItem: WordClassItem,
        primaryText: String,
        noteList: List<String>,
        sectionFromList: List<SectionForm>,
        formItemManager: FormItemManager
    ): WordEntryFormData {

        var index = 0
        val sectionMap = sectionFromList.associate { sectionForm ->
            index++ to WordSectionFormData(
                formItemManager.createNewTextItem(InputTextType.MEANING, sectionForm.meaning, formItemManager.createItemProperties()),
                getListToMap(sectionForm.kanaList, InputTextType.KANA, formItemManager),
                getListToMap(sectionForm.noteList, InputTextType.SECTION_NOTE_DESCRIPTION, formItemManager)
            )
        }.toPersistentMap()

        return WordEntryFormData(
            wordClassItem,
            formItemManager.createNewTextItem(InputTextType.PRIMARY_TEXT, primaryText, formItemManager.createItemProperties()),
            getListToMap(noteList, InputTextType.DICTIONARY_NOTE_DESCRIPTION, formItemManager),
            sectionMap
            )
    }

    private fun getListToMap(list: List<String>, inputTextType: InputTextType, formItemManager: FormItemManager): PersistentMap<String, TextItem>{
        return list.associate { text ->
            val item = formItemManager.createNewTextItem(inputTextType, text, formItemManager.createItemProperties())
            item.itemProperties.getIdentifier() to item
        }.toPersistentMap()
    }

    internal data class SectionForm(val meaning: String, val kanaList: List<String>, val noteList: List<String>)
}