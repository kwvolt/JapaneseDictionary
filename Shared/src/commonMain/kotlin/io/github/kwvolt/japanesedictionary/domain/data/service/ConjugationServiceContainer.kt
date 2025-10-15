package io.github.kwvolt.japanesedictionary.domain.data.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationOverrideRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationSuffixRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationTemplateRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationVerbSuffixSwapRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationOverrideRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationPatternRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationPreprocessRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationSuffixRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationTemplateRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.VerbSuffixSwapRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationTemplateInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationUpsert

class ConjugationServiceContainer(private val dbHandler: DatabaseHandler) {
    private val queries = dbHandler.queries
    internal val conjugationPatternRepository: ConjugationPatternRepositoryInterface by lazy {
        ConjugationPatternRepository(dbHandler, queries.conjugationPatternQueries, queries.conjugationPatternVariantQueries)
    }
    internal val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface by lazy {
        ConjugationPreprocessRepository(dbHandler, queries.conjugationPreprocessQueries)
    }
    internal val conjugationOverrideRepository: ConjugationOverrideRepositoryInterface by lazy {
        ConjugationOverrideRepository(dbHandler, queries.conjugationOverrideQueries, queries.conjugationOverrideDetailsQueries, queries.conjugationOverridePropertyQueries)
    }
    internal val conjugationRepository: ConjugationRepositoryInterface by lazy {
        ConjugationRepository(dbHandler, queries.conjugationQueries)
    }
    internal val conjugationSuffixRepository: ConjugationSuffixRepositoryInterface by lazy {
        ConjugationSuffixRepository(dbHandler, queries.conjugationSuffixQueries)
    }
    internal val conjugationTemplateRepository: ConjugationTemplateRepositoryInterface by lazy {
        ConjugationTemplateRepository(dbHandler, queries.conjugationTemplateQueries, queries.conjugationTemplateLinkConjugationQueries)
    }
    internal val verbSuffixSwapRepository: ConjugationVerbSuffixSwapRepositoryInterface by lazy {
        VerbSuffixSwapRepository(dbHandler, queries.verbSuffixSwapQueries, queries.verbSuffixSwapLinkConjugationQueries)
    }

    val conjugationTemplateInserter: ConjugationTemplateInserter by lazy {
        ConjugationTemplateInserter(
            dbHandler,
            conjugationPatternRepository,
            conjugationPreprocessRepository,
            conjugationSuffixRepository,
            verbSuffixSwapRepository,
            conjugationRepository,
            conjugationOverrideRepository,
            conjugationTemplateRepository
        )
    }

    val conjugationUpsert: ConjugationUpsert by lazy {
        ConjugationUpsert(
            dbHandler,
            conjugationPatternRepository,
            conjugationPreprocessRepository,
            conjugationSuffixRepository,
            verbSuffixSwapRepository,
            conjugationRepository,
            conjugationOverrideRepository,
            conjugationTemplateRepository
        )
    }

    fun <T> getServices(block: ConjugationServiceContainer.() -> T) : T {
        return block(this)
    }

    suspend fun <T> withServices(block: ConjugationServiceContainer.() -> T) : T {
        return block(this)
    }
}