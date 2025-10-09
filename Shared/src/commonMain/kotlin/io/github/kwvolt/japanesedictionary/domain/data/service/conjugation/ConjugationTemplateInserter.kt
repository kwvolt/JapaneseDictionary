package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface

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
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface
){
    private var templateIdName: String = ""
    private var templateDisplayText: String = ""

    suspend fun defineTemplate(idName: String, displayText: String,block: suspend ConjugationTemplateInserter.() -> DatabaseResult<Long>): DatabaseResult<Long>{
        templateIdName = idName
        templateDisplayText = displayText
        val result = block()
        templateIdName = ""
        templateDisplayText = ""
        return result
    }

    suspend fun insert(
        conjugation: suspend ConjugationTemplateInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long>{
        if(templateIdName.isBlank() && templateDisplayText.isBlank()) return DatabaseResult.UnknownError(
            IllegalArgumentException("requires defineTemplate to be called first"))
        val conjugationTemplateResult: DatabaseResult<Long> = dbHandler.performTransaction {
            val innerConjugationTemplateResult: DatabaseResult<Long> = conjugationTemplateRepository.insert(templateIdName, templateDisplayText).returnOnFailure<Long> { return@performTransaction it }
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
        block: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)? = null
    ): DatabaseResult<Long>{
        val conjugationResult: DatabaseResult<Long> = conjugationRepository.selectId(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId, returnNotFoundOnNull = true)
        val conjugationId: Long = when(conjugationResult){
            is DatabaseResult.Success -> conjugationResult.value
            is DatabaseResult.NotFound -> conjugationRepository.insert(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).getOrReturn { return it }
            else -> return conjugationResult.mapErrorTo()
        }
        block?.invoke(CTScopedVerbSuffixSwapInserter(this, conjugationId))
        return DatabaseResult.Success(conjugationId)
    }
    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationPreprocessId: Long,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long>{
        return conjugationSuffix(CTScopedSuffixInserter(this, conjugationPatternId, conjugationPreprocessId))
    }

    suspend fun getPatternId(idName: String): DatabaseResult<Long>{
        return conjugationPatternRepository.selectId(idName)
    }

    suspend fun getProcessId(stemRule: StemRule): DatabaseResult<Long>{
        return conjugationPreprocessRepository.selectId(stemRule.toString())
    }

    suspend fun getSuffixId(suffixText: String? = null, isShortForm: Boolean? = null): DatabaseResult<Long> {
        val suffixResult: DatabaseResult<Long> = conjugationSuffixRepository.selectId(suffixText, isShortForm, true)
        return when(suffixResult){
            is DatabaseResult.Success -> suffixResult
            is DatabaseResult.NotFound -> conjugationSuffixRepository.insert(suffixText, isShortForm)
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
        block: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)? = null
    ): DatabaseResult<Long> {
        return base.getOrInsertConjugation(conjugationPatternId, processId, conjugationSuffixId, block)
    }

    suspend fun getOrInsertConjugation(
        conjugationPatternId: Long,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long> {
        return conjugationSuffix(CTScopedSuffixInserter(base, conjugationPatternId, processId))
    }

    suspend fun getOrInsertConjugation(
        conjugationPatternIdName: String,
        conjugationSuffix: suspend CTScopedSuffixInserter.() -> DatabaseResult<Long>
    ): DatabaseResult<Long> {
        val patternId: Long = base.getPatternId(conjugationPatternIdName).getOrReturn { return it }
        return conjugationSuffix(CTScopedSuffixInserter(base, patternId, processId))
    }
}

@ConjugationDsl
class CTScopedVerbSuffixSwapInserter(
    private val base: ConjugationTemplateInserter,
    private val conjugationId: Long
){
    suspend fun linkVerbSuffixSwap(
        original: String,
        replacement: String,
    ): DatabaseResult<Unit> {
        return base.linkVerbSuffixSwap(conjugationId, original, replacement)
    }
}

@ConjugationDsl
class CTScopedSuffixInserter(
    private val base: ConjugationTemplateInserter,
    private val conjugationPatternId: Long,
    private val conjugationPreprocessId: Long
) {
    suspend fun nullSuffix(text: String?, swap: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)?=null): DatabaseResult<Long> {
        val suffixId = base.getSuffixId(text, null).getOrReturn { return it }
        return base.getOrInsertConjugation(conjugationPatternId, conjugationPreprocessId, suffixId, swap)
    }
    suspend fun shortSuffix(text: String?, swap: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)?=null): DatabaseResult<Long> {
        val suffixId = base.getSuffixId(text, true).getOrReturn { return it }
        return base.getOrInsertConjugation(conjugationPatternId, conjugationPreprocessId, suffixId, swap)

    }
    suspend fun longSuffix(text: String?, swap: (suspend CTScopedVerbSuffixSwapInserter.() -> Unit)?=null): DatabaseResult<Long> {
        val suffixId = base.getSuffixId(text, false).getOrReturn { return it }
        return base.getOrInsertConjugation(conjugationPatternId, conjugationPreprocessId, suffixId, swap)
    }
}