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

class ConjugationUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface,
    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface,
    private val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface,
    private val verbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface,
    private val conjugationRepository: ConjugationRepositoryInterface,
    private val conjugationOverrideRepository: ConjugationOverrideRepositoryInterface,
    private val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface,
) {
    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        displayTextValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        descriptionTextValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided
    ): DatabaseResult<Long>{
        return upsertWithIdAndIdName(conjugationPatternId, idNameValue,
            doesExistResult = { idName: String? ->conjugationPatternRepository.selectExist(conjugationPatternId, idName)},
            insert = { idName: String ->
                val displayText: String = displayTextValue.requireNotNullOrFail {return it }
                conjugationPatternRepository.insert(idName, displayText)
            },
            update={ id: Long, idName: String? ->
                val (descriptionText: String? ,descriptionTextProvided: Boolean) = descriptionTextValue.getValueWithPresenceFlag()
                conjugationPatternRepository.update(id, idName, displayTextValue.getOrNull(), descriptionTextProvided, descriptionText)
            }
        )
    }

    /**
     * Either Insert or Updates the Conjugation Pattern along with its variants
     *
     * Warning: Must be called within a database transaction. Fails if not.
     *
     **/
    @RequiresTransaction
    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        displayTextValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        descriptionTextValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided,
        variantList: List<ConjugationPatternUpsertContainer>
    ): DatabaseResult<Long>{
        return dbHandler.requireTransaction {
            upsertPattern(
                conjugationPatternId,
                idNameValue,
                displayTextValue,
                descriptionTextValue
            ).flatMap { id: Long ->
                dbHandler.processBatchWrite(variantList) { container: ConjugationPatternUpsertContainer ->
                    val result = upsertPattern(
                        container.conjugationPatternId,
                        container.idNameValue,
                        container.displayTextValue,
                        container.descriptionTextValue
                    )
                    val variantId: Long = result.getOrReturn { return@processBatchWrite it}
                    val linkExist: Boolean =
                        conjugationPatternRepository.selectCheckLinkExist(id, variantId)
                            .getOrReturn { return@processBatchWrite it}
                    if (!linkExist) {
                        linkVariantToPattern(id, variantId)
                            .returnOnFailure { return@processBatchWrite it }
                    }
                    DatabaseResult.Success(Unit)


                }.map { id }
            }
        }
    }

    suspend fun linkVariantToPattern(conjugationPatternId: Long, conjugationVariantId: Long): DatabaseResult<Unit>{
        return conjugationPatternRepository.insertLinkVariantToOriginal(conjugationPatternId, conjugationVariantId)
            .returnOnFailure { return it }
    }

    suspend fun  upsertPreprocess(
        conjugationPreprocessId: Long? = null,
        idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        return upsertWithIdAndIdName(
            conjugationPreprocessId,
            idNameValue,
            doesExistResult = { idName: String? -> conjugationPreprocessRepository.selectExist(conjugationPreprocessId, idName) },
            insert = { idName: String -> conjugationPreprocessRepository.insert(idName) },
            update={ id: Long, idName: String? ->
                val idName = getOrFail(idName) {return it }
                conjugationPreprocessRepository.update(id, idName)
            }
        )
    }

    suspend fun upsertSuffix(
        conjugationSuffixId: Long? = null,
        suffixTextValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided,
        isShortOrLongValue: OptionalProvidedValue<Boolean> = ProvidedValue.ValueNotProvided,
        isPositiveValue: OptionalProvidedValue<Boolean> = ProvidedValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        val (suffixText: String? ,suffixTextProvided: Boolean) = suffixTextValue.getValueWithPresenceFlag()
        val (isShortOrLong: Boolean? ,isShortOrLongProvided: Boolean) = isShortOrLongValue.getValueWithPresenceFlag()
        val (isPositive: Boolean? ,isPositiveProvided: Boolean) = isPositiveValue.getValueWithPresenceFlag()
        return upsertWithId(
            conjugationSuffixId,
            selectId = { conjugationSuffixRepository.selectId(suffixText, isShortOrLong, isPositive,true) },
            insert = {conjugationSuffixRepository.insert(suffixText, isShortOrLong, isPositive) },
            update = { id: Long ->
                conjugationSuffixRepository.update(id, suffixTextProvided, suffixText, isShortOrLongProvided, isShortOrLong, isPositiveProvided, isPositive)
            }
        )
    }

    suspend fun upsertVerbSuffixSwap(
        verbSuffixSwapId: Long? = null,
        originalValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        replacementValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided
    ): DatabaseResult<Long>{
        val original: String? = originalValue.getOrNull()
        val replacement: String? = replacementValue.getOrNull()
        val insertOriginal = getOrFail(original, "original should not be null"){return it}
        val insertReplacement = getOrFail(replacement, "replacement should not be null"){return it}
        return upsertWithId(
            verbSuffixSwapId,
            selectId = {verbSuffixSwapRepository.selectId(insertOriginal, insertReplacement, true)},
            insert = {verbSuffixSwapRepository.insert(insertOriginal, insertReplacement) },
            update = { id: Long -> verbSuffixSwapRepository.update(id, original, replacement)}
        )
    }

    suspend fun upsertConjugation(
        conjugationId: Long? = null,
        conjugationPatternIdValue: ProvidedValue<Long> = ProvidedValue.ValueNotProvided,
        conjugationPreprocessIdValue: ProvidedValue<Long> = ProvidedValue.ValueNotProvided,
        conjugationSuffixIdValue: ProvidedValue<Long> = ProvidedValue.ValueNotProvided
    ): DatabaseResult<Long>{
        val conjugationPatternId: Long? = conjugationPatternIdValue.getOrNull()
        val conjugationPreprocessId: Long? = conjugationPreprocessIdValue.getOrNull()
        val conjugationSuffixId: Long? = conjugationSuffixIdValue.getOrNull()
        return if(conjugationId != null){
            conjugationRepository.update(conjugationId, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { conjugationId }
        }
        else {
            val patternId: Long = getOrFail(conjugationPatternId, "patternId should not be null") {return it}
            val preprocessId: Long = getOrFail(conjugationPreprocessId,"preprcId should not be null") { return it}
            val suffixId: Long = getOrFail(conjugationSuffixId) { return it}
            conjugationRepository.insert(patternId, preprocessId, suffixId)
        }
    }



    /**
     * Either Insert or Updates the Conjugation Override along with its Properties
     *
     * Warning: Must be called within a database transaction. Fails if not.
     *
     **/
    @RequiresTransaction
    suspend fun upsertOverride(
        conjugationOverrideId: Long? = null,
        idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
        dictionaryEntryIdValue: ProvidedValue<Long> = ProvidedValue.ValueNotProvided,
        conjugationIdValue: ProvidedValue<Long> = ProvidedValue.ValueNotProvided,
        overrideNoteValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided,
        overridePropertiesValue: ProvidedValue<Map<ConjugationOverrideProperty, UpsertOrDelete<OptionalProvidedValue<String>>>> = ProvidedValue.ValueNotProvided,
    ): DatabaseResult<Long> {
        return dbHandler.requireTransaction {
            val dictionaryEntryId: Long? = dictionaryEntryIdValue.getOrNull()
            val conjugationId: Long? = conjugationIdValue.getOrNull()
            val overrideProperties = overridePropertiesValue.getOrNull()
            upsertWithIdAndIdName(
                conjugationOverrideId,
                idNameValue,
                doesExistResult = { idName: String? -> conjugationOverrideRepository.selectExist(conjugationOverrideId, idName) },
                insert = { idName: String ->
                    val insertDictionaryEntryId: Long = getOrFail(dictionaryEntryId, "dictionaryEntryId is required") { return@upsertWithIdAndIdName it }
                    val insertConjugationId: Long = getOrFail(conjugationId, "conjugationId is required") { return@upsertWithIdAndIdName it }
                    val overrideNote: String? = overrideNoteValue.getOrNull()
                    conjugationOverrideRepository.insert(idName, insertDictionaryEntryId, insertConjugationId, overrideNote)
                        .flatMap { insertedId ->
                            processOverrideProperties(insertedId, overrideProperties)
                                .map { insertedId }
                        }
                },
                update={ id: Long, idName: String? ->
                    val idName = getOrFail(idName, "idName is required") { return@upsertWithIdAndIdName it }
                    val (overrideNote: String?, overrideNoteProvided: Boolean) = overrideNoteValue.getValueWithPresenceFlag()
                    conjugationOverrideRepository.update(
                        id,
                        idName,
                        dictionaryEntryId,
                        conjugationId,
                        overrideNoteProvided,
                        overrideNote
                    ).returnOnFailure { return@upsertWithIdAndIdName it }
                    processOverrideProperties(id, overrideProperties).map { id }
                }
            )
        }
    }

    suspend fun upsertProperty(
        conjugationPropertyId: Long? = null,
        idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        return upsertWithIdAndIdName(
            conjugationPropertyId,
            idNameValue,
            doesExistResult = { idName: String? -> conjugationOverrideRepository.selectPropertyExist(conjugationPropertyId, idName) },
            insert = { idName: String -> conjugationOverrideRepository.insertProperty(idName) },
            update={ id: Long, idName: String? ->
                val idName = getOrFail(idName) {return it}
                conjugationOverrideRepository.updateProperty(id, idName).toUnit()
            }
        )
    }

    suspend fun upsertPropertyValue(
        conjugationOverrideId: Long,
        conjugationOverridePropertyId: Long,
        propertyValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided
    ): DatabaseResult<Unit>{
        return when (val result = conjugationOverrideRepository.selectPropertyValue(conjugationOverrideId, conjugationOverridePropertyId, true)) {
            is DatabaseResult.Success -> {
                val (property: String?, propertyProvided: Boolean) = propertyValue.getValueWithPresenceFlag()
                if(result.value != property && propertyProvided){
                    conjugationOverrideRepository.updatePropertyValue(conjugationOverrideId, conjugationOverridePropertyId, true, property)
                }else {
                    DatabaseResult.UnknownError(IllegalArgumentException("Value Already exist or not provided"))
                }
            }
            is DatabaseResult.NotFound -> {
                val property: String?= propertyValue.getOrNull()
                conjugationOverrideRepository.insertPropertyValue(conjugationOverrideId, conjugationOverridePropertyId, property).toUnit()
            }
            else -> result.mapErrorTo()
        }
    }

    private suspend fun processOverrideProperties(
        overrideId: Long,
        properties: Map<ConjugationOverrideProperty, UpsertOrDelete<OptionalProvidedValue<String>>>?
    ): DatabaseResult<Unit> {
        if (properties == null) return DatabaseResult.Success(Unit)
        return dbHandler.processBatchWrite(properties) { (property, action) ->
            val propertyId = conjugationOverrideRepository.selectPropertyId(property.toString())
                .getOrReturn { return@processBatchWrite it}
            when (action) {
                UpsertOrDelete.Delete -> {
                    conjugationOverrideRepository.deletePropertyValue(overrideId, propertyId)
                }
                is UpsertOrDelete.Upsert -> {
                    val (value, isProvided) = action.value.getValueWithPresenceFlag()
                    conjugationOverrideRepository.updatePropertyValue(
                        overrideId, propertyId, isProvided, value
                    ).toUnit()
                }
            }
        }
    }

    private suspend inline fun upsertWithIdAndIdName(
        id: Long? = null,
        idNameValue: ProvidedValue<String>,
        doesExistResult: suspend (String?)-> DatabaseResult<Boolean>,
        insert: suspend (String) -> DatabaseResult<Long>,
        update: suspend (Long, String?)-> DatabaseResult<Unit>,
    ): DatabaseResult<Long>{
        if(id == null && idNameValue is ProvidedValue.ValueNotProvided) return DatabaseResult.UnknownError(IllegalArgumentException("either id or idname have to be not null"))
        val isExist: Boolean = doesExistResult(idNameValue.getOrNull()).getOrReturn { return it }
        return if(!isExist){
            val idName: String = idNameValue.requireNotNullOrFail {return it}
            insert(idName)
        }
        else {
            val id: Long = getOrFail(id){return it }
            update( id, idNameValue.getOrNull()).map { id }
        }
    }

    private suspend fun upsertWithId(
        id: Long?,
        selectId: suspend () -> DatabaseResult<Long>,
        insert: suspend () -> DatabaseResult<Long>,
        update: suspend (Long) -> DatabaseResult<Unit>
    ): DatabaseResult<Long> {
        if (id != null) {
            return update(id).map { id }
        }
        return when (val result = selectId()) {
            is DatabaseResult.Success -> {
                val foundId = result.value
                update(foundId).map { foundId }
            }
            is DatabaseResult.NotFound -> insert()
            else -> result.mapErrorTo()
        }
    }

    private inline fun <T, R> getOrFail(
        value: T?,
        errorMessage: String = "either id or idName have to be not null",
        error: (DatabaseResult<R>)-> Nothing
    ): T{
        return value ?: error(DatabaseResult.UnknownError(IllegalArgumentException(errorMessage)))
    }
}

data class ConjugationPatternUpsertContainer(
    val conjugationPatternId: Long? = null,
    val idNameValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
    val displayTextValue: ProvidedValue<String> = ProvidedValue.ValueNotProvided,
    val descriptionTextValue: OptionalProvidedValue<String> = ProvidedValue.ValueNotProvided,
)

sealed class  ProvidedValue<out T>{
    data class Value<T>(val value: T): ProvidedValue<T>()
    data object ValueNotProvided: ProvidedValue<Nothing>()

    fun getValueWithPresenceFlag(): Pair<T?, Boolean>{
        return when(this){
            is Value -> Pair( value, true)
            is ValueNotProvided -> Pair(null, false)
        }
    }

    fun getOrNull(): T?{
        return when(this){
            is Value -> value
            is ValueNotProvided -> null
        }
    }

    inline fun <R> requireNotNullOrFail(
        errorMessage: String = "value must not be null",
        error: (DatabaseResult<R>)-> Nothing
    ): T{
        return when(this){
            is Value -> value
            ValueNotProvided -> error(DatabaseResult.UnknownError(IllegalArgumentException(errorMessage)))
        }
    }
}

sealed interface UpsertOrDelete<out T> {
    data class Upsert<T>(val value: T): UpsertOrDelete<T>
    data object Delete: UpsertOrDelete<Nothing>

}

typealias OptionalProvidedValue<T> = ProvidedValue<T?>