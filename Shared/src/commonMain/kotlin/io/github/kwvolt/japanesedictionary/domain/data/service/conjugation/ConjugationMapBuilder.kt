package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult.*
import io.github.kwvolt.japanesedictionary.domain.data.database.getOrDefaultOrReturn
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryConjugationTemplateContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryEntryContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugatedWord
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationBy
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationForm
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationOverrideProperty
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationPolarity
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationWords
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule
import kotlin.collections.set

class ConjugationMapBuilder(
    private val dictionaryRepository: DictionaryRepositoryInterface,
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface,
    private val conjugationRepository: ConjugationRepositoryInterface,
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface,
    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface,
    private val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface,
    private val conjugationVerbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface,
    private val conjugationOverrideRepository: ConjugationOverrideRepositoryInterface
) {
    suspend fun buildConjugationTemplate(
        dictionaryEntryId: Long
    ): DatabaseResult<MutableMap<ConjugationBy, MutableMap<Long, ConjugationWords>>>{
        val dictionaryContainer: DictionaryEntryContainer =
            dictionaryRepository.selectRow(dictionaryEntryId).getOrReturn { return it }
        val linkContainer: DictionaryEntryConjugationTemplateContainer =
            dictionaryRepository.selectConjugationTemplate(dictionaryEntryId).getOrReturn { return it }
        return buildConjugationTemplate(
            dictionaryContainer.primaryText,
            linkContainer.kana,
            linkContainer.conjugationTemplateId
        )
    }

    suspend fun buildConjugationTemplate(
        primaryText: String,
        kana: String? = null,
        conjugationTemplateId: Long
    ): DatabaseResult<MutableMap<ConjugationBy, MutableMap<Long, ConjugationWords>>>{
        val patternCache: MutableMap<Long, ConjugationPatternContainer> = mutableMapOf()
        val preprocessCache: MutableMap<Long, ConjugationPreprocessContainer> = mutableMapOf()
        val suffixCache: MutableMap<Long, ConjugationSuffixContainer> = mutableMapOf()
        val variantLinkCache: MutableMap<Long, OptionalProvidedValue<Long>> = mutableMapOf()
        val verbSuffixSwapCache: MutableMap<Pair<Long, String>, String> = mutableMapOf()
        val conjugationMap: MutableMap<ConjugationBy, MutableMap<Long, ConjugationWords>> = mutableMapOf()
        conjugationMap[ConjugationBy.PRIMARY_TEXT] = mutableMapOf()
        kana?.let {conjugationMap[ConjugationBy.KANA] = mutableMapOf()}

        val conjugationList: List<Long> =
            conjugationTemplateRepository.selectConjugationIdByConjugationTemplateId(conjugationTemplateId).getOrReturn { return it }

        conjugationList.forEach { id ->
            val conjugationContainer: ConjugationContainer =
                conjugationRepository.selectRow(id).getOrReturn { return it }

            val patternContainer: ConjugationPatternContainer = patternCache.getOrPut(
                key = conjugationContainer.conjugationPatternId,
                defaultValue = {
                    conjugationPatternRepository.selectRow(conjugationContainer.conjugationPatternId).getOrReturn { return it }
                }
            )

            resolveVariantLinkIfNeeded(patternContainer, patternCache, variantLinkCache).getOrReturn { return it }

            val preprocessContainer: ConjugationPreprocessContainer = preprocessCache.getOrPut(
                key = conjugationContainer.conjugationPreprocessId,
                defaultValue = {
                    conjugationPreprocessRepository.selectRow(conjugationContainer.conjugationPreprocessId).getOrReturn { return it }
                }
            )

            val suffixContainer: ConjugationSuffixContainer = suffixCache.getOrPut(
                key = conjugationContainer.conjugationSuffixId,
                defaultValue = {
                    conjugationSuffixRepository.selectRow(conjugationContainer.conjugationSuffixId).getOrReturn { return it }
                }
            )

            val rule = ConjugationRule(preprocessContainer.idName,suffixContainer.suffixText)

            val kanjiOverride: ConjugationOverrideContainer? =
                conjugationOverrideRepository.selectRowByKeys(true, conjugationTemplateId, id, true)
                    .getOrDefaultOrReturn(onNotFound = { null }, errorTo = { return it })

            val bothOverride: ConjugationOverrideContainer? =
                conjugationOverrideRepository.selectRowByKeys(null, conjugationTemplateId, id, true)
                    .getOrDefaultOrReturn(onNotFound = { null }, errorTo = { return it })

            val isShortForm = suffixContainer.isShortForm
            val isPositive = suffixContainer.isPositive
            val variantTo: Long? =
                when (val patternProvided = variantLinkCache[patternContainer.id]) {
                    is ProvidedValue.Value -> patternProvided.value
                    ProvidedValue.ValueNotProvided -> null
                    null -> return DatabaseResult.UnknownError(IllegalStateException("unexpected null in variantLinkCache"))
                }

            val primaryTextConjugation: String = applyWordConjugation(
                id,
                primaryText,
                rule,
                kanjiOverride ?: bothOverride,
                verbSuffixSwapCache
            ).getOrReturn { return it }
            val overrideNote: String? = kanjiOverride?.overrideNote ?: bothOverride?.overrideNote
            val primaryTextForm: ConjugationForm = createConjugationForm(isShortForm, isPositive, ConjugatedWord(primaryTextConjugation, overrideNote))
            conjugationMap[ConjugationBy.PRIMARY_TEXT]?.insertIntoPatternMap(
                patternContainer.id,
                variantTo,
                primaryTextForm,
                patternContainer.displayText,
                patternContainer.descriptionText,
                patternCache
            )?.returnOnFailure { return it }


            kana?.let{ kana ->
                val kanaOverride: ConjugationOverrideContainer? =
                    conjugationOverrideRepository.selectRowByKeys(false, conjugationTemplateId, id, true)
                        .getOrDefaultOrReturn(onNotFound = { null }, errorTo = { return it })

                val kanaConjugation: String = applyWordConjugation(
                    id,
                    kana,
                    rule,
                    kanaOverride ?: bothOverride,
                    verbSuffixSwapCache
                ).getOrReturn { return it }

                val overrideNote: String? = kanaOverride?.overrideNote ?: bothOverride?.overrideNote
                val kanaForm: ConjugationForm = createConjugationForm(isShortForm, isPositive, ConjugatedWord(kanaConjugation, overrideNote))

                conjugationMap[ConjugationBy.KANA]?.insertIntoPatternMap(
                    patternContainer.id,
                    variantTo,
                    kanaForm,
                    patternContainer.displayText,
                    patternContainer.descriptionText,
                    patternCache
                )?.returnOnFailure { return it }
            }
        }
        return DatabaseResult.Success(conjugationMap)
    }

    private suspend fun applyWordConjugation(
        conjugationId: Long,
        input: String,
        conjugationRule: ConjugationRule,
        override: ConjugationOverrideContainer?,
        verbSuffixSwapCache: MutableMap<Pair<Long, String>, String>
    ): DatabaseResult<String>{
        val properties: Map<ConjugationOverrideProperty, String?> = override?.overrideProperty ?: mapOf()
        val rule = conjugationRule.copy(properties = properties)
        return applyRule(input, rule) { original ->
            verbSuffixSwapCache.getOrPut(
                key = Pair(conjugationId, original),
                defaultValue = {
                    conjugationVerbSuffixSwapRepository.selectReplacement(conjugationId, original).getOrReturn { return it }
                }
            )
        }
    }

    private fun  MutableMap<Long, ConjugationWords>.insertIntoPatternMap(
        patternId: Long,
        parentPatternId: Long?,
        form: ConjugationForm,
        patternDisplayText: String,
        patternNotes: String?,
        patternCache: Map<Long, ConjugationPatternContainer>,
    ): DatabaseResult<Unit> {
        val actualKey: Long = parentPatternId ?: patternId
        val current: ConjugationWords? = this[actualKey]
        val updated: ConjugationWords = when {
            parentPatternId != null -> {
                val parentTextError: DatabaseResult<Unit> = DatabaseResult.UnknownError(
                    IllegalStateException("missing parent display text")
                )
                when (current) {
                    // update a variant
                    is ConjugationWords.ConjugationWordWithVariant -> {
                        val updatedVariant = current.variants.toMutableMap().apply {
                            val variant = getOrElse(patternId) {
                                ConjugationWords.ConjugationWord(patternDisplayText, form, patternNotes)
                            }.copy(
                                form = mergeConjugationForms(get(patternId)?.form ?: form, form)
                            )
                            put(patternId, variant)
                        }
                        current.copy(variants = updatedVariant)
                    }
                    // create the variant
                    is ConjugationWords.ConjugationWord -> {
                        ConjugationWords.ConjugationWordWithVariant(
                            displayText = patternCache[parentPatternId]?.displayText ?: return parentTextError,
                            form = current.form,
                            variants = mapOf(
                                patternId to ConjugationWords.ConjugationWord(patternDisplayText, form, patternNotes)
                            )
                        )
                    }
                    // create the class to be able to store variant in
                    null -> {
                        ConjugationWords.ConjugationWordWithVariant(
                            displayText = patternCache[parentPatternId]?.displayText ?: return parentTextError,
                            form = form,
                            variants = mapOf(
                                patternId to ConjugationWords.ConjugationWord(patternDisplayText, form, patternNotes)
                            )
                        )
                    }
                }
            }
            else -> {
                when (current) {
                    // update main form
                    is ConjugationWords.ConjugationWord -> current.copy(form = mergeConjugationForms(current.form, form))
                    // update main form
                    is ConjugationWords.ConjugationWordWithVariant -> current.copy(form = mergeConjugationForms(current.form, form))
                    // create main form
                    null -> ConjugationWords.ConjugationWord(patternDisplayText, form, patternNotes)
                }
            }
        }
        this[actualKey] = updated
        return DatabaseResult.Success(Unit)
    }

    private suspend inline fun applyRule(
        input: String,
        rule: ConjugationRule,
        verbSuffixSwap: suspend (String) -> String
    ): DatabaseResult<String> {
        val props: Map<ConjugationOverrideProperty, String?> = rule.properties

        // Resolve stem rule
        val stemRule: StemRule = props[ConjugationOverrideProperty.STEM_RULE]?.let { StemRule.fromValue(it) }
            ?: rule.stemRule?.let { StemRule.fromValue(it) }
            ?: return  DatabaseResult.UnknownError(IllegalStateException("conjugation did not work properly"))

        // Handle irregular forms
        val irregularSuffix: String? = props[ConjugationOverrideProperty.IRREGULAR]
        val (prefixStem: String, baseInput: String) = if (irregularSuffix != null && input.endsWith(irregularSuffix)) {
            input.removeSuffix(irregularSuffix) to irregularSuffix
        } else {
            "" to input
        }

        // Resolve input to apply stem rule to
        val stemBase: String = props[ConjugationOverrideProperty.STEM_REPLACEMENT]
            ?: props[ConjugationOverrideProperty.IRREGULAR_REPLACEMENT]
            ?: baseInput

        // Apply stem rule
        val stem: String = when (stemRule) {
            StemRule.DROP_RU -> stemBase.removeSuffix("る")
            StemRule.REPLACE_SUFFIX -> {
                val staticSwap: String? = props[ConjugationOverrideProperty.VERB_SUFFIX_SWAP]
                val replacement: String =
                    staticSwap ?:
                    verbSuffixSwap(
                        stemBase.lastOrNull()?.toString() ?:
                        return UnknownError(IllegalStateException("does not have suffix to replace"))
                    )
                stemBase.dropLast(1) + replacement
            }
            StemRule.NOTHING -> stemBase
        }

        // Determine suffix (empty string allowed for something like conjunctive form)
        val suffix: String = props[ConjugationOverrideProperty.SUFFIX_OVERRIDE] ?: rule.suffix.orEmpty()

        // Build final conjugated result
        return DatabaseResult.Success(prefixStem + stem + suffix)
    }

    private suspend fun resolveVariantLinkIfNeeded(
        patternContainer: ConjugationPatternContainer,
        patternCache: MutableMap<Long, ConjugationPatternContainer>,
        variantLinkCache: MutableMap<Long, OptionalProvidedValue<Long>>
    ): DatabaseResult<Unit> {
        val patternId = patternContainer.id
        if (variantLinkCache.containsKey(patternId)) return DatabaseResult.Success(Unit)
        return when (val variantLink = conjugationPatternRepository.selectIsVariantOf(patternId, true)) {
            is DatabaseResult.Success -> {
                val variantId = variantLink.value
                variantLinkCache[patternId] = ProvidedValue.Value(variantId)

                val parentPattern = conjugationPatternRepository.selectRow(variantId).getOrReturn { return it }
                patternCache.putIfAbsent(variantId, parentPattern)
                variantLinkCache[variantId] = ProvidedValue.ValueNotProvided

                DatabaseResult.Success(Unit)
            }
            is DatabaseResult.NotFound -> {
                variantLinkCache[patternId] = ProvidedValue.ValueNotProvided
                DatabaseResult.Success(Unit)
            }
            else -> variantLink.mapErrorTo()
        }
    }

    private fun createConjugationForm(
        isShortForm: Boolean?,
        isPositive: Boolean?,
        value: ConjugatedWord
    ): ConjugationForm {
        val polarity = when (isPositive) {
            true -> ConjugationPolarity.Positive(value)
            false -> ConjugationPolarity.Negative(value)
            null -> ConjugationPolarity.Neither(value)
        }
        return when (isShortForm) {
            true -> ConjugationForm.ShortForm(polarity)
            false -> ConjugationForm.LongForm(polarity)
            null -> ConjugationForm.NeitherForm(polarity)
        }
    }

    private fun mergeConjugationForms(existing: ConjugationForm, incoming: ConjugationForm): ConjugationForm {
        return when {
            existing is ConjugationForm.ShortForm && incoming is ConjugationForm.ShortForm -> {
                val merged = mergePolarities(existing.shortForm, incoming.shortForm)
                ConjugationForm.ShortForm(merged)
            }
            existing is ConjugationForm.LongForm && incoming is ConjugationForm.LongForm -> {
                val merged = mergePolarities(existing.longForm, incoming.longForm)
                ConjugationForm.LongForm(merged)
            }
            existing is ConjugationForm.NeitherForm && incoming is ConjugationForm.NeitherForm -> {
                val merged = mergePolarities(existing.neitherForm, incoming.neitherForm)
                ConjugationForm.NeitherForm(merged)
            }
            existing is ConjugationForm.ShortForm && incoming is ConjugationForm.LongForm -> {
                ConjugationForm.BothForm(existing.shortForm, incoming.longForm)
            }
            existing is ConjugationForm.LongForm && incoming is ConjugationForm.ShortForm -> {
                ConjugationForm.BothForm(incoming.shortForm, existing.longForm)
            }
            existing is ConjugationForm.BothForm && incoming is ConjugationForm.ShortForm -> {
                existing.copy(shortForm = mergePolarities(existing.shortForm, incoming.shortForm))
            }
            existing is ConjugationForm.BothForm && incoming is ConjugationForm.LongForm -> {
                existing.copy(longForm = mergePolarities(existing.longForm, incoming.longForm))
            }
            else -> existing
        }
    }

    private fun mergePolarities(
        existing: ConjugationPolarity,
        incoming: ConjugationPolarity
    ): ConjugationPolarity = when {
        existing is ConjugationPolarity.Both -> existing
        incoming is ConjugationPolarity.Both -> incoming
        existing is ConjugationPolarity.Positive && incoming is ConjugationPolarity.Negative -> {
            ConjugationPolarity.Both(existing.positive, incoming.negative)
        }
        existing is ConjugationPolarity.Negative && incoming is ConjugationPolarity.Positive -> {
            ConjugationPolarity.Both(incoming.positive, existing.negative)
        }
        existing is ConjugationPolarity.Neither -> incoming
        incoming is ConjugationPolarity.Neither -> existing
        else -> existing
    }
}

internal data class ConjugationRule(
    val stemRule: String?,
    val suffix: String? = null,
    val properties: Map<ConjugationOverrideProperty, String?> = mapOf()
)
