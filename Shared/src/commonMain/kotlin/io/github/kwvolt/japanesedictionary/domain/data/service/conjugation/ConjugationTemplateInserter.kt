package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.RequiresTransaction
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.CTScopedOverrideInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.UpsertOrDelete
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationOverrideProperty
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule

@DslMarker
annotation class ConjugationDsl

@ConjugationDsl
class ConjugationTemplateInserter(
    private val dbHandler: DatabaseHandlerBase,
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface,
    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface,
    private val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface,
    private val verbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface,
    private val conjugationRepository: ConjugationRepositoryInterface,
    private val conjugationOverrideRepository: ConjugationOverrideRepositoryInterface,
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface
){
    private var templateIdName: String = ""
    private var templateDisplayText: String = ""
    private var conjugationTemplateId: Long? = null

    suspend fun defineTemplate(idName: String, displayText: String, block: suspend ConjugationTemplateInserter.() -> DatabaseResult<Long>): DatabaseResult<Long>{
        templateIdName = idName
        templateDisplayText = displayText
        val result = block()
        templateIdName = ""
        templateDisplayText = ""
        conjugationTemplateId = null
        return result
    }

    suspend fun insert(
        conjugation: suspend ConjugationTemplateInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long>{
        if(templateIdName.isBlank() && templateDisplayText.isBlank()) return DatabaseResult.UnknownError(
            IllegalArgumentException("requires defineTemplate to be called first").fillInStackTrace())
        val conjugationTemplateResult: DatabaseResult<Long> = dbHandler.performTransaction {
            val innerConjugationTemplateResult: DatabaseResult<Long> = conjugationTemplateRepository.insert(templateIdName, templateDisplayText).returnOnFailure<Long> { return@performTransaction it }
            conjugationTemplateId = innerConjugationTemplateResult.getOrReturn<Long> { return@performTransaction it }
            conjugation().returnOnFailure<Long> { return@performTransaction it }
            innerConjugationTemplateResult
        }
        return conjugationTemplateResult
    }

    suspend inline fun withPreprocess(
        stemRule: StemRule,
        block: suspend CTScopedPreprocessedInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long> {
        val processId: Long = getProcessId(stemRule).getOrReturn { return it }
        val scopedBuilder = CTScopedPreprocessedInserter(this, processId)
        val result = scopedBuilder.block()
        return result
    }

    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationPreprocessId: Long,
        conjugationSuffixId: Long,
        block: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)? = null,
        override: (suspend CTScopedOverrideInserter.() -> Unit)?=null
    ): DatabaseResult<Long>{
        val conjugationResult: DatabaseResult<Long> = conjugationRepository.selectId(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId, returnNotFoundOnNull = true)
        val conjugationId: Long = when(conjugationResult){
            is DatabaseResult.Success -> conjugationResult.value
            is DatabaseResult.NotFound -> conjugationRepository.insert(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).getOrReturn { return it }
            else -> return conjugationResult.mapErrorTo()
        }
        conjugationTemplateId?.let{
            conjugationTemplateRepository.insertLinkConjugationToConjugationTemplate(it, conjugationId).returnOnFailure { error -> return error }
        }
        block?.invoke(CTScopedVerbSuffixSwapInserter(this, dbHandler, conjugationId))
        override?.invoke(CTScopedOverrideInserter(this, conjugationId))
        return DatabaseResult.Success(conjugationId)
    }
    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationPreprocessId: Long,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long>{
        return conjugationSuffix(CTScopedSuffixInserter(this, conjugationPatternId, conjugationPreprocessId))
    }

    @RequiresTransaction
    suspend fun insertOverride(isKanji: Boolean? = null, conjugationId: Long, overrideNote: String? = null, properties: Map<ConjugationOverrideProperty, String?> = mapOf()): DatabaseResult<Unit>{
        return dbHandler.requireTransaction {
            conjugationTemplateId?.let { templateId: Long ->
                conjugationOverrideRepository.insert(isKanji, templateId, conjugationId, overrideNote)
                    .flatMap { insertedId: Long ->
                        if (properties.isEmpty()) return@requireTransaction DatabaseResult.Success(
                            Unit
                        )
                        dbHandler.processBatchWrite(properties) { (property: ConjugationOverrideProperty, propertyValue: String?) ->
                            val propertyId =
                                conjugationOverrideRepository.selectPropertyId(property.toString())
                                    .getOrReturn { return@processBatchWrite it }
                            conjugationOverrideRepository.insertPropertyValue(
                                insertedId,
                                propertyId,
                                propertyValue,
                            )
                        }
                    }
            } ?: return@requireTransaction DatabaseResult.UnknownError(IllegalStateException("requires defineTemplate to be called first"))
        }
    }

    suspend fun getPatternId(idName: String): DatabaseResult<Long>{
        return conjugationPatternRepository.selectId(idName)
    }

    suspend fun getProcessId(stemRule: StemRule): DatabaseResult<Long>{
        return conjugationPreprocessRepository.selectId(stemRule.toString())
    }

    suspend fun getSuffixId(suffixText: String? = null, isShortForm: Boolean? = null, isPositive: Boolean?=null): DatabaseResult<Long> {
        val suffixResult: DatabaseResult<Long> = conjugationSuffixRepository.selectId(suffixText, isShortForm, isPositive,true)
        return when(suffixResult){
            is DatabaseResult.Success -> suffixResult
            is DatabaseResult.NotFound -> conjugationSuffixRepository.insert(suffixText, isShortForm, isPositive)
            else -> suffixResult.mapErrorTo()
        }
    }

    suspend fun linkVerbSuffixSwap(conjugationId: Long, original: String, replacement: String): DatabaseResult<Unit>{
        val verbSuffixSwapId: Long = verbSuffixSwapRepository.selectId(original, replacement).getOrReturn { return it }
        return verbSuffixSwapRepository.insertLinkVerbSuffixSwapToConjugation(verbSuffixSwapId,conjugationId)
    }
}

@ConjugationDsl
class CTScopedPreprocessedInserter(
    private val base: ConjugationTemplateInserter,
    private val processId: Long
) {
    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationSuffixId: Long,
        block: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)? = null,
        override: (suspend CTScopedOverrideInserter.() -> Unit)?=null
    ): DatabaseResult<Long> {
        return base.getOrInsertConjugation(conjugationPatternId, processId, conjugationSuffixId, block, override)
    }

    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>,
    ): DatabaseResult<Long> {
        return conjugationSuffix(CTScopedSuffixInserter(base, conjugationPatternId, processId))
    }

    suspend fun getOrInsertConjugation(
        conjugationPatternIdName: String,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>,
    ): DatabaseResult<Long> {
        val patternId: Long = base.getPatternId(conjugationPatternIdName).getOrReturn { return it }
        return conjugationSuffix(CTScopedSuffixInserter(base, patternId, processId))
    }
}

@ConjugationDsl
class CTScopedVerbSuffixSwapInserter(
    private val base: ConjugationTemplateInserter,
    private val dbHandler: DatabaseHandlerBase,
    private val conjugationId: Long
){
    suspend fun linkVerbSuffixSwap(
        original: String,
        replacement: String,
    ): DatabaseResult<Unit> {
        return base.linkVerbSuffixSwap(conjugationId, original, replacement)
    }

    suspend fun linkVerbSuffixSwap(
        originalToReplace: Map<String, String>
    ): DatabaseResult<Unit> {
        return dbHandler.requireTransaction {
            dbHandler.processBatchWrite(originalToReplace){ (original: String, replacement: String ) ->
                base.linkVerbSuffixSwap(conjugationId, original, replacement)
            }
        }

    }
}

@ConjugationDsl
class CTScopedSuffixInserter(
    private val base: ConjugationTemplateInserter,
    private val conjugationPatternId: Long,
    private val conjugationPreprocessId: Long
) {
    suspend fun suffix(
        text: String?,
        isShortForm: Boolean?,
        isPositive: Boolean?,
        swap: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)?=null,
        override: (suspend CTScopedOverrideInserter.() -> Unit)?=null
    ): DatabaseResult<Long> {
        val suffixId = base.getSuffixId(text, isShortForm, isPositive).getOrReturn { return it }
        return base.getOrInsertConjugation(conjugationPatternId, conjugationPreprocessId, suffixId, swap, override)
    }
}

@ConjugationDsl
class CTScopedOverrideInserter(
    private val base: ConjugationTemplateInserter,
    private val conjugationId: Long,
) {
    suspend fun insertOverride(isKanji: Boolean?, overrideNote: String? = null, properties: Map<ConjugationOverrideProperty, String?> = mapOf()): DatabaseResult<Unit>{
        return base.insertOverride(isKanji, conjugationId, overrideNote, properties)
    }
}