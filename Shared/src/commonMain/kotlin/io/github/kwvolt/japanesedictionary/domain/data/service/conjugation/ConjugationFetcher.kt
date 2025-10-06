package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface
import kotlinx.coroutines.coroutineScope

class ConjugationFetcher(
    private val dbHandler: DatabaseHandlerBase,
    private val dictionaryRepository: DictionaryRepositoryInterface,
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface,
    private val conjugationRepository: ConjugationRepositoryInterface,
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface,
    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface,
    private val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface,
    private val conjugationVerbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface
) {

    suspend fun fetchConjugationsForm(dictionaryId: Long): DatabaseResult<Map<String, Map<String, ConjugationForm>>> = coroutineScope {

        val dictionaryResult: DatabaseResult<DictionaryEntryContainer> = dictionaryRepository.selectRow(dictionaryId)
        val primaryText: String = dictionaryResult.getOrReturn { return@coroutineScope it.mapErrorTo() }.primaryText

        //val dictLinkConjTemplateResult: DatabaseResult<Long> = dictionaryLinkConjugationTemplateRepository.selectConjugationTemplateId(dictionaryId)
        //val conjugationTemplateId: Long = dictLinkConjTemplateResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

        val conjLinkConjTemplateResult: DatabaseResult<List<Long>> = conjugationTemplateRepository.selectConjugationIdByConjugationTemplateId(conjugationTemplateId)
        val conjugationIds: List<Long> = conjLinkConjTemplateResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

        val map = mutableMapOf<String, Map<String, ConjugationForm>>()

        conjugationIds.forEach { conjugationId: Long ->
            val conjugationResult: DatabaseResult<ConjugationContainer> = conjugationRepository.selectRow(conjugationId)
            val conjugationContainer: ConjugationContainer = conjugationResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

            val conjugationPatternResult: DatabaseResult<ConjugationPatternContainer> = conjugationPatternRepository.selectRow(conjugationContainer.conjugationPatternId)
            val conjugationPattern: ConjugationPatternContainer = conjugationPatternResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

            val conjugationPreprocessResult: DatabaseResult<ConjugationPreprocessContainer> = conjugationPreprocessRepository.selectRow(conjugationContainer.conjugationPreprocessId)
            val conjugationPreprocess: ConjugationPreprocessContainer = conjugationPreprocessResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

            val conjugationSuffixResult: DatabaseResult<ConjugationSuffixContainer> = conjugationSuffixRepository.selectRow(conjugationContainer.conjugationSuffixId)
            val conjugationSuffix: ConjugationSuffixContainer = conjugationSuffixResult.getOrReturn { return@coroutineScope it.mapErrorTo() }

            val rule = ConjugationRule(
                stemRule = conjugationPreprocess.idName,
                stemReplacement = conjugationSuffix.stemReplacement,
                suffix = conjugationSuffix.suffixText)
            val conjugatedWord: String = applyRule(primaryText, rule)

            if(conjugationSuffix.isShortForm != null){
                ConjugatedWord.ShortLong()
            }
            else {
                ConjugatedWord.Both(conjugatedWord)
            }




        }
        DatabaseResult.Success(mapOf())
    }

    private fun applyRule(input: String, rule: ConjugationRule): String{
        val stemRule: StemRule? = rule.stemRule?.let{StemRule.fromValue(it)}
        val stem: String = when(stemRule){
            StemRule.DROP_RU -> input.removeSuffix("る")
            StemRule.REPLACE_SUFFIX -> {
                if (rule.verbSuffixOriginal != null && input.lastOrNull() == rule.verbSuffixOriginal) {
                    input.dropLast(1) + (rule.verbSuffixReplacement ?: "")
                } else {
                    input
                }
            }
            StemRule.REPLACE_IRREGULAR_VERB -> rule.stemReplacement ?: input
            else -> input
        }

        return stem + (rule.suffix ?: "")
    }

    private inline fun <T> DatabaseResult<T>.getOrReturn(
        errorTo : (DatabaseResult<T>) -> T
    ) : T{
        return when (this) {
            is DatabaseResult.Success -> value
            else -> errorTo(this)
        }
    }
}

sealed class ConjugatedWord{
    data class ShortLong(val short: String, val long: String): ConjugatedWord()
    data class Both(val conjugatedWord: String): ConjugatedWord()
}

enum class StemRule(private val value: String){
    DROP_RU("DROP_RU"),
    REPLACE_SUFFIX("REPLACE_SUFFIX"),
    REPLACE_IRREGULAR_VERB("REPLACE_IRREGULAR_VERB");

    override fun toString(): String = value

    companion object {
        fun fromValue(value: String): StemRule? {
            return StemRule.entries.find { it.toString().equals(value, ignoreCase = true) }
        }
    }
}

enum class SpellingType(private val value: String) {
    KANJI("KANJI"),
    FURIGANA("FURIGANA");

    override fun toString(): String = value

    companion object {
        fun fromValue(value: String): SpellingType? {
            return SpellingType.entries.find { it.toString().equals(value, ignoreCase = true) }
        }
    }
}

enum class ConjugationOverrideProperty(private val value: String){
    SPELLING_TYPE("SPELLING_TYPE"),
    STEM_REPLACEMENT("STEM_REPLACEMENT"),
    SUFFIX_OVERRIDE("SUFFIX_OVERRIDE");

    override fun toString(): String = value

    companion object {
        fun fromValue(value: String): ConjugationOverrideProperty? {
            return ConjugationOverrideProperty.entries.find { it.toString().equals(value, ignoreCase = true) }
        }
    }
}

internal data class ConjugationRule(
    val stemRule: String?,
    val verbSuffixOriginal: Char? = null,
    val verbSuffixReplacement: Char? = null,
    val stemReplacement: String? = null,
    val suffix: String? = null
)