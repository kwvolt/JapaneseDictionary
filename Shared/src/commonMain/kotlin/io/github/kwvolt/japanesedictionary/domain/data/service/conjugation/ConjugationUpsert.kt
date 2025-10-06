package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.getOrReturn
import io.github.kwvolt.japanesedictionary.domain.data.database.returnOnFailure
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface

class ConjugationUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface,
    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface,
    private val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface,
    private val verbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface,
    private val conjugationRepository: ConjugationRepositoryInterface,
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface
) {

    suspend fun insertPattern(
        idName: String,
        displayText: String,
        descriptionText: String? =null,
    ): DatabaseResult<Long>{
        return conjugationPatternRepository.insert(idName, displayText, descriptionText)
    }

    suspend fun updatePattern(
        conjugationPatternId: Long,
        idName: String? = null,
        displayText: String? = null,
        descriptionTextProvided: Boolean,
        descriptionText: String? =null
    ): DatabaseResult<Unit>{
        return conjugationPatternRepository.update(
            conjugationPatternId,
            idName,
            displayText,
            true,
            descriptionText
        )
    }

    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idName: String? = null,
        displayText: String? = null,
        descriptionText: String? =null,
    ): DatabaseResult<Long>{
        return upsertBlock(conjugationPatternId, idName,
            doesExistResult = { conjugationPatternRepository.selectExist(conjugationPatternId, idName)},
            insert = {idName: String ->
                val displayText: String = getOrFail(displayText) {return it.mapErrorTo()}
                conjugationPatternRepository.insert(idName, displayText)
            },
            update={ id: Long, idName: String? ->conjugationPatternRepository.update(
                id,
                idName,
                displayText,
                false,
                descriptionText,
            ) }
        )
    }

    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idName: String?,
        displayText: String?,
        descriptionText: String? = null,
        variantList: List<ConjugationPatternUpsertContainer>
    ): DatabaseResult<Long>{
        return upsertPattern(conjugationPatternId, idName, displayText, descriptionText).flatMap { id: Long ->
            dbHandler.processBatchWrite(variantList){ container: ConjugationPatternUpsertContainer ->
                val result = upsertPattern(
                    container.conjugationPatternId,
                    container.idName,
                    container.displayText,
                    container.descriptionText)
                val variantId: Long = result.getOrReturn { return@processBatchWrite it.mapErrorTo() }
                val linkResult: DatabaseResult<Unit> = conjugationPatternRepository.insertLinkVariantToOriginal(id, variantId)
                linkResult.returnOnFailure { return@processBatchWrite it.mapErrorTo() }
            }.map { id }
        }
    }

    suspend fun  upsertPreprocess(
        conjugationPreprocessId: Long? = null,
        idName: String?
    ): DatabaseResult<Long>{
        return upsertBlock(
            conjugationPreprocessId,
            idName,
            doesExistResult = { conjugationPreprocessRepository.selectExist(conjugationPreprocessId, idName) },
            insert = { idName: String -> conjugationPreprocessRepository.insert(idName) },
            update={ id: Long, idName: String? ->
                val idName = getOrFail(idName) {return it.mapErrorTo()}
                conjugationPreprocessRepository.update(id, idName)
            }
        )
    }

    suspend fun upsertSuffix(
        conjugationSuffixId: Long? = null,
        suffixTextProvided: Boolean = false,
        isShortOrLongProvided: Boolean = false,
        suffixText: String? = null,
        isShortOrLong: Boolean? = null
    ): DatabaseResult<Long>{
        if(conjugationSuffixId != null){
                return conjugationSuffixRepository.update(
                    conjugationSuffixId,
                    suffixTextProvided,
                    isShortOrLongProvided,
                    suffixText,
                    isShortOrLong).map { conjugationSuffixId }
            }

        val result = conjugationSuffixRepository.selectId(suffixText, isShortOrLong, true)
        return when(result){
            is DatabaseResult.Success -> {
                val id: Long = result.value
                conjugationSuffixRepository.update(
                    id,
                    suffixTextProvided,
                    isShortOrLongProvided,
                    suffixText,
                    isShortOrLong).map { id }
            }
            is DatabaseResult.NotFound -> { conjugationSuffixRepository.insert(suffixText, isShortOrLong) }
            else -> result.mapErrorTo()
        }
    }


    suspend fun upsertVerbSuffixSwap(
        verbSuffixSwapId: Long? = null,
        original: String? = null,
        replacement: String? = null
    ): DatabaseResult<Long>{
        if(verbSuffixSwapId != null){
            return verbSuffixSwapRepository.update(verbSuffixSwapId, original, replacement).map { verbSuffixSwapId }
        }
        val insertOriginal = getOrFail(original, "original should not be null"){return it.mapErrorTo()}
        val insertReplacement = getOrFail(replacement, "replacement should not be null"){return it.mapErrorTo()}
        val result = verbSuffixSwapRepository.selectId(insertOriginal, insertReplacement, true)
        return when(result){
            is DatabaseResult.Success -> {
                val id: Long = result.value
                verbSuffixSwapRepository.update(id, insertOriginal, insertReplacement).map { id }
            }
            is DatabaseResult.NotFound -> { verbSuffixSwapRepository.insert(insertOriginal, insertReplacement) }
            else -> result.mapErrorTo()
        }
    }

    suspend fun upsertConjugation(
        conjugationId: Long? = null,
        conjugationPatternId: Long? = null,
        conjugationPreprocessId: Long? = null,
        conjugationSuffixId: Long? = null
    ): DatabaseResult<Long>{
        if(conjugationId != null){
            return conjugationRepository.update(conjugationId, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { conjugationId }
        }
        val conjugationPatternId: Long = getOrFail(conjugationPatternId) {return it.mapErrorTo()}
        val conjugationPreprocessId: Long = getOrFail(conjugationPreprocessId) { return it.mapErrorTo()}
        val conjugationSuffixId: Long = getOrFail(conjugationSuffixId) { return it.mapErrorTo()}
        val result: DatabaseResult<Long> = conjugationRepository.selectId(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId)
        return when(result){
            is DatabaseResult.Success -> {
                val id: Long = result.value
                conjugationRepository.update(id, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { id }
            }
            is DatabaseResult.NotFound -> { conjugationRepository.insert(conjugationPatternId, conjugationPreprocessId, conjugationSuffixId) }
            else -> result.mapErrorTo()
        }
    }

    suspend fun upsertConjugation(
        conjugationId: Long? = null,
        conjugationPatternIdName: String,
        conjugationPreprocessIdName: String,
        suffixText: String? = null,
        isShortOrLong: Boolean? = null
    ): DatabaseResult<Long>{
        if(conjugationId != null){
            val preprocessId: Long = conjugationPreprocessRepository.selectId(conjugationPreprocessIdName).getOrReturn { return it.mapErrorTo() }
            val patternId: Long = conjugationPatternRepository.selectId(conjugationPatternIdName).getOrReturn { return it.mapErrorTo() }
            val suffixResult: DatabaseResult<Long> =
                conjugationSuffixRepository.selectId(suffixText, isShortOrLong, true)
            val suffixId: Long = when(suffixResult){
                is DatabaseResult.Success -> suffixResult.value
                is DatabaseResult.NotFound -> {
                    upsertSuffix(
                        suffixTextProvided =  true,
                        isShortOrLongProvided =  true,
                        suffixText = suffixText,
                        isShortOrLong = isShortOrLong).getOrReturn { return it.mapErrorTo() }
                }
                else -> return suffixResult.mapErrorTo()

            }
            return upsertConjugation(conjugationId, preprocessId, patternId, suffixId)
        }
    }

    private suspend inline fun upsertBlock(
        id: Long? = null,
        idName: String? = null,
        doesExistResult: suspend ()-> DatabaseResult<Boolean>,
        insert: suspend (String) -> DatabaseResult<Long>,
        update: suspend (Long, String?)-> DatabaseResult<Unit>,
    ): DatabaseResult<Long>{
        if(id == null && idName == null) return DatabaseResult.UnknownError(IllegalArgumentException("either id or idname have to be not null"))
        val isExist: Boolean = doesExistResult().getOrReturn { return it.mapErrorTo() }
        return if(!isExist){
            val idName: String = getOrFail(idName){return it.mapErrorTo()}
            insert(idName)
        }
        else {
            val id: Long = getOrFail(id){return it.mapErrorTo()}
            update( id, idName).map { id }
        }
    }

    private inline fun <T> getOrFail(
        value: T?,
        errorMessage: String = "either id or idName have to be not null",
        error: (DatabaseResult<T>)-> T
    ): T{
        return value ?: error(DatabaseResult.UnknownError(IllegalArgumentException(errorMessage)))
    }
}

data class ConjugationPatternUpsertContainer(
    val conjugationPatternId: Long? = null,
    val idName: String?=null,
    val displayText: String?=null,
    val descriptionText: String?=null
)

sealed class  NullableValue<out T>  (){
    data class Value<T>(val value: T?): NullableValue<T>()
    data object ValueNotProvided: NullableValue<Nothing>()
}