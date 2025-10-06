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

    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idNameValue: NullableValue<String> = NullableValue.ValueNotProvided,
        displayTextValue: NullableValue<String> = NullableValue.ValueNotProvided,
        descriptionTextValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided
    ): DatabaseResult<Long>{
        return upsertBlock(conjugationPatternId, idNameValue,
            doesExistResult = { idName: String? ->conjugationPatternRepository.selectExist(conjugationPatternId, idName)},
            insert = { idName: String ->
                val displayText: String = displayTextValue.requireNotNullOrFail {return it.mapErrorTo()}
                conjugationPatternRepository.insert(idName, displayText)
            },
            update={ id: Long, idName: String? ->
                val (descriptionText: String? ,descriptionTextProvided: Boolean) = descriptionTextValue.getValueWithPresenceFlag()
                conjugationPatternRepository.update(id, idName, displayTextValue.getOrNull(), descriptionTextProvided, descriptionText)
            }
        )
    }

    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idNameValue: NullableValue<String> = NullableValue.ValueNotProvided,
        displayTextValue: NullableValue<String> = NullableValue.ValueNotProvided,
        descriptionTextValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided,
        variantList: List<ConjugationPatternUpsertContainer>
    ): DatabaseResult<Long>{
        return upsertPattern(conjugationPatternId, idNameValue, displayTextValue, descriptionTextValue).flatMap { id: Long ->
            dbHandler.processBatchWrite(variantList){ container: ConjugationPatternUpsertContainer ->
                val result = upsertPattern(
                    container.conjugationPatternId,
                    container.idNameValue,
                    container.displayTextValue,
                    container.descriptionTextValue)
                val variantId: Long = result.getOrReturn { return@processBatchWrite it.mapErrorTo() }
                val linkExist: Boolean = conjugationPatternRepository.selectCheckLinkExist(id, variantId).getOrReturn { return@processBatchWrite it.mapErrorTo() }
                if(linkExist){
                    conjugationPatternRepository.insertLinkVariantToOriginal(id, variantId).returnOnFailure { return@processBatchWrite it.mapErrorTo() }
                }
                DatabaseResult.Success(Unit)


            }.map { id }
        }
    }

    suspend fun  upsertPreprocess(
        conjugationPreprocessId: Long? = null,
        idNameValue: NullableValue<String> = NullableValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        return upsertBlock(
            conjugationPreprocessId,
            idNameValue,
            doesExistResult = { idName: String? -> conjugationPreprocessRepository.selectExist(conjugationPreprocessId, idName) },
            insert = { idName: String -> conjugationPreprocessRepository.insert(idName) },
            update={ id: Long, idName: String? ->
                val idName = getOrFail(idName) {return it.mapErrorTo()}
                conjugationPreprocessRepository.update(id, idName)
            }
        )
    }

    suspend fun upsertSuffix(
        conjugationSuffixId: Long? = null,
        suffixTextValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided,
        isShortOrLongValue: OptionalNullableValue<Boolean> = NullableValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        val (suffixText: String? ,suffixTextProvided: Boolean) = suffixTextValue.getValueWithPresenceFlag()
        val (isShortOrLong: Boolean? ,isShortOrLongProvided: Boolean) = isShortOrLongValue.getValueWithPresenceFlag()
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
        originalValue: NullableValue<String> = NullableValue.ValueNotProvided,
        replacementValue: NullableValue<String> = NullableValue.ValueNotProvided
    ): DatabaseResult<Long>{
        val original: String? = originalValue.getOrNull()
        val replacement: String? = replacementValue.getOrNull()
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
        conjugationPatternIdValue: NullableValue<Long> = NullableValue.ValueNotProvided,
        conjugationPreprocessIdValue: NullableValue<Long> = NullableValue.ValueNotProvided,
        conjugationSuffixIdValue: NullableValue<Long> = NullableValue.ValueNotProvided
    ): DatabaseResult<Long>{
        val conjugationPatternId: Long? = conjugationPatternIdValue.getOrNull()
        val conjugationPreprocessId: Long? = conjugationPreprocessIdValue.getOrNull()
        val conjugationSuffixId: Long? = conjugationSuffixIdValue.getOrNull()
        if(conjugationId != null){
            return conjugationRepository.update(conjugationId, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { conjugationId }
        }
        val patternId: Long = getOrFail(conjugationPatternId) {return it.mapErrorTo()}
        val preprocessId: Long = getOrFail(conjugationPreprocessId) { return it.mapErrorTo()}
        val suffixId: Long = getOrFail(conjugationSuffixId) { return it.mapErrorTo()}
        val result: DatabaseResult<Long> = conjugationRepository.selectId(patternId, preprocessId, suffixId)
        return when(result){
            is DatabaseResult.Success -> {
                val id: Long = result.value
                conjugationRepository.update(id, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { id }
            }
            is DatabaseResult.NotFound -> { conjugationRepository.insert(patternId, preprocessId, suffixId) }
            else -> result.mapErrorTo()
        }
    }

    private suspend inline fun upsertBlock(
        id: Long? = null,
        idNameValue: NullableValue<String>,
        doesExistResult: suspend (String?)-> DatabaseResult<Boolean>,
        insert: suspend (String) -> DatabaseResult<Long>,
        update: suspend (Long, String?)-> DatabaseResult<Unit>,
    ): DatabaseResult<Long>{
        if(id == null && idNameValue is NullableValue.ValueNotProvided) return DatabaseResult.UnknownError(IllegalArgumentException("either id or idname have to be not null"))
        val isExist: Boolean = doesExistResult(idNameValue.getOrNull()).getOrReturn { return it.mapErrorTo() }
        return if(!isExist){
            val idName: String = idNameValue.requireNotNullOrFail {return it.mapErrorTo()}
            insert(idName)
        }
        else {
            val id: Long = getOrFail(id){return it.mapErrorTo()}
            update( id, idNameValue.getOrNull()).map { id }
        }
    }

    private inline fun <T> getOrFail(
        value: T?,
        errorMessage: String = "either id or idName have to be not null",
        error: (DatabaseResult<T>)-> T
    ): T{
        return value ?: error(DatabaseResult.UnknownError(IllegalArgumentException(errorMessage)))
    }

    private inline fun <T> NullableValue<T>.requireNotNullOrFail(
        errorMessage: String = "value much not be null",
        error: (DatabaseResult<T>)-> T
    ): T{
        return when(this){
            is NullableValue.Value -> value
            NullableValue.ValueNotProvided -> error(DatabaseResult.UnknownError(IllegalArgumentException(errorMessage)))
        }
    }

    private fun <T> NullableValue<T>.getValueWithPresenceFlag(): Pair<T?, Boolean>{
        return when(this){
            is NullableValue.Value -> Pair( value, true)
            is NullableValue.ValueNotProvided -> Pair(null, false)
        }
    }

    private fun <T> NullableValue<T>.getOrNull(): T?{
        return when(this){
            is NullableValue.Value -> value
            is NullableValue.ValueNotProvided -> null
        }
    }



}

data class ConjugationPatternUpsertContainer(
    val conjugationPatternId: Long? = null,
    val idNameValue: NullableValue<String> = NullableValue.ValueNotProvided,
    val displayTextValue: NullableValue<String> = NullableValue.ValueNotProvided,
    val descriptionTextValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided,
)

sealed class  NullableValue<out T>  (){
    data class Value<T>(val value: T): NullableValue<T>()
    data object ValueNotProvided: NullableValue<Nothing>()
}

typealias OptionalNullableValue<T> = NullableValue<T?>