package io.github.kwvolt.japanesedictionary.domain.service

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepositoryInterface

class WordClassUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface
) {
    suspend fun initalizeWordClass(idName: String, displayText: String): DatabaseResult<Long> {
        return dbHandler.performTransaction {
            mainClassRepository.insertMainClass(idName, displayText).flatMap { mainId ->
                subClassRepository.insertSubClassLinkToMainClass(mainId, "NONE", "--NA--").flatMap {
                    DatabaseResult.Success(mainId)
                }
            }
        }
    }

    suspend fun initializeWordClassWithSubClasses(
        idName: String,
        displayText: String,
        subClassMap: Map<String, String>
    ): DatabaseResult<Long> {
        return dbHandler.performTransaction {
            // First, insert the main class
            mainClassRepository.insertMainClass(idName, displayText).flatMap { mainId ->
                // Then insert the default "NONE" subclass
                subClassRepository.insertSubClassLinkToMainClass(mainId, "NONE", "--NA--").flatMap {
                    // Process each subClass in the provided map
                    dbHandler.processBatch(subClassMap) { entry ->
                        subClassRepository.insertSubClassLinkToMainClass(mainId, entry.key, entry.value).flatMap {
                            DatabaseResult.Success(Unit)
                        }
                    }
                }.flatMap {
                    // Once batch processing is done, return the mainId of the word class
                    DatabaseResult.Success(mainId)
                }
            }
        }
    }
}