package io.github.kwvolt.japanesedictionary.domain.data.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.conjugations.ConjugationPatternVariantQueries
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPatternRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.conjugation.ConjugationPreprocessRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationPatternRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.conjugation.ConjugationPreprocessRepository

class ConjugationServiceContainer(private val dbHandler: DatabaseHandler) {
    private val queries = dbHandler.queries
    private val conjugationPatternRepository: ConjugationPatternRepositoryInterface by lazy {
        ConjugationPatternRepository(dbHandler, queries.conjugationPatternQueries, queries.conjugationPatternVariantQueries)
    }

    private val conjugationPreprocessRepository: ConjugationPreprocessRepositoryInterface by lazy {
        ConjugationPreprocessRepository(dbHandler, queries.conjugationPreprocessQueries)
    }
}