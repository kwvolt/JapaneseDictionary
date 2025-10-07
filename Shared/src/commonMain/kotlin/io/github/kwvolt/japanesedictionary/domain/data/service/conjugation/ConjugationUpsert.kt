package io.github.kwvolt.japanesedictionary.domain.data.service.conjugation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.RequiresTransaction
import io.github.kwvolt.japanesedictionary.domain.data.database.getOrReturn
import io.github.kwvolt.japanesedictionary.domain.data.database.returnOnFailure
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

    /**
     * Either Insert or Updates the Conjugation Pattern along with its variants
     *
     * Warning: Must be called within a database transaction. Fails if not.
     *
     **/
    @RequiresTransaction
    suspend fun upsertPattern(
        conjugationPatternId: Long? = null,
        idNameValue: NullableValue<String> = NullableValue.ValueNotProvided,
        displayTextValue: NullableValue<String> = NullableValue.ValueNotProvided,
        descriptionTextValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided,
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
                    val variantId: Long =
                        result.getOrReturn { return@processBatchWrite it.mapErrorTo() }
                    val linkExist: Boolean =
                        conjugationPatternRepository.selectCheckLinkExist(id, variantId)
                            .getOrReturn { return@processBatchWrite it.mapErrorTo() }
                    if (!linkExist) {
                        conjugationPatternRepository.insertLinkVariantToOriginal(id, variantId)
                            .returnOnFailure { return@processBatchWrite it.mapErrorTo() }
                    }
                    DatabaseResult.Success(Unit)


                }.map { id }
            }
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
        return upsertWithLookup(
            conjugationSuffixId,
            selectId = { conjugationSuffixRepository.selectId(suffixText, isShortOrLong, true) },
            insert = {conjugationSuffixRepository.insert(suffixText, isShortOrLong) },
            update = { id: Long ->
                conjugationSuffixRepository.update(id, suffixTextProvided, isShortOrLongProvided, suffixText, isShortOrLong)
            }
        )
    }

    suspend fun upsertVerbSuffixSwap(
        verbSuffixSwapId: Long? = null,
        originalValue: NullableValue<String> = NullableValue.ValueNotProvided,
        replacementValue: NullableValue<String> = NullableValue.ValueNotProvided
    ): DatabaseResult<Long>{
        val original: String? = originalValue.getOrNull()
        val replacement: String? = replacementValue.getOrNull()
        val insertOriginal = getOrFail(original, "original should not be null"){return it.mapErrorTo()}
        val insertReplacement = getOrFail(replacement, "replacement should not be null"){return it.mapErrorTo()}
        return upsertWithLookup(
            verbSuffixSwapId,
            selectId = {verbSuffixSwapRepository.selectId(insertOriginal, insertReplacement, true)},
            insert = {verbSuffixSwapRepository.insert(insertOriginal, insertReplacement) },
            update = { id: Long -> verbSuffixSwapRepository.update(id, original, replacement)}
        )
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
        return if(conjugationId != null){
            conjugationRepository.update(conjugationId, conjugationPatternId, conjugationPreprocessId, conjugationSuffixId).map { conjugationId }
        }
        else {
            val patternId: Long = getOrFail(conjugationPatternId, "patternId should not be null") {return it.mapErrorTo()}
            val preprocessId: Long = getOrFail(conjugationPreprocessId,"preprcId should not be null") { return it.mapErrorTo()}
            val suffixId: Long = getOrFail(conjugationSuffixId) { return it.mapErrorTo()}
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
        dictionaryEntryIdValue: NullableValue<Long> = NullableValue.ValueNotProvided,
        conjugationIdValue: NullableValue<Long> = NullableValue.ValueNotProvided,
        overrideNoteValue: OptionalNullableValue<String> = NullableValue.ValueNotProvided,
        overridePropertiesValue: NullableValue<Map<ConjugationOverrideProperty, OptionalNullableValue<String>>> = NullableValue.ValueNotProvided,
    ): DatabaseResult<Long>{
        suspend fun update(overrideId: Long, dictionaryEntryId: Long? , conjugationId: Long?, overrideNoteProvided: Boolean, overrideNote: String?, overrideProperties: Map<ConjugationOverrideProperty, OptionalNullableValue<String>>?): DatabaseResult<Unit>{
            conjugationOverrideRepository.update(
                overrideId,
                dictionaryEntryId,
                conjugationId,
                overrideNoteProvided,
                overrideNote
            ).returnOnFailure { return it.mapErrorTo() }
            if(overrideProperties != null){
                dbHandler.processBatchWrite(overrideProperties){ property ->
                    val propertyId: Long = conjugationOverrideRepository.selectPropertyId(property.key.toString()).getOrReturn { return@processBatchWrite it.mapErrorTo() }
                    val (propertyValue: String?, propertyValueProvided: Boolean) =  property.value.getValueWithPresenceFlag()
                    conjugationOverrideRepository.updatePropertyValue(
                        overrideId,
                        propertyId,
                        propertyValueProvided,
                        propertyValue
                    ).map {  }
                }
            }
            return DatabaseResult.Success(Unit)
        }
        return dbHandler.requireTransaction {
            val dictionaryEntryId: Long? = dictionaryEntryIdValue.getOrNull()
            val conjugationId: Long? = conjugationIdValue.getOrNull()
            val overrideProperties: Map<ConjugationOverrideProperty, OptionalNullableValue<String>>? = overridePropertiesValue.getOrNull()
            val (overrideNote: String?, overrideNoteProvided: Boolean) = overrideNoteValue.getValueWithPresenceFlag()

            val insertDictionaryEntryId: Long = getOrFail(dictionaryEntryId) { return@requireTransaction it.mapErrorTo() }
            val insertConjugationId: Long = getOrFail(conjugationId) { return@requireTransaction it.mapErrorTo() }
            upsertWithLookup(
                conjugationOverrideId,
                selectId = {conjugationOverrideRepository.selectId(insertDictionaryEntryId, insertConjugationId, true)},
                insert = {
                    val insertResult: DatabaseResult<Long> = conjugationOverrideRepository.insert(
                        insertDictionaryEntryId,
                        insertConjugationId,
                        overrideNote
                    )
                    val overrideId: Long = insertResult.getOrReturn { return@upsertWithLookup it.mapErrorTo() }
                    if (overrideProperties != null) {
                        dbHandler.processBatchWrite(overrideProperties) { property ->
                            val propertyId: Long = conjugationOverrideRepository.selectPropertyId(
                                property.key.toString()
                            ).getOrReturn { return@processBatchWrite it.mapErrorTo() }
                            val propertyValue: String? = property.value.getOrNull()
                            conjugationOverrideRepository.insertPropertyValue(
                                overrideId,
                                propertyId,
                                propertyValue
                            ).map { }
                        }
                    }
                    insertResult
                },
                update = { id: Long ->
                    update(id, dictionaryEntryId, conjugationId, overrideNoteProvided, overrideNote, overrideProperties)
                }
            )
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

    private suspend fun upsertWithLookup(
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