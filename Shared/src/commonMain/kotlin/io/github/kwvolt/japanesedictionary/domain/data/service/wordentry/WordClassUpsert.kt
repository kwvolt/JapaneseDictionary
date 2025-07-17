package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface

class WordClassUpsert(
    private val dbHandler: DatabaseHandlerBase,
    private val mainClassRepository: MainClassRepositoryInterface,
    private val subClassRepository: SubClassRepositoryInterface,
    private val wordClassRepository: WordClassRepositoryInterface
) {
    suspend fun initializeWordClass(idName: String, displayText: String): DatabaseResult<Long> {
        return mainClassRepository.insert(idName, displayText).flatMap { mainId ->
            subClassRepository.insertLinkToMainClass(mainId, "NONE", "--NA--")
        }
    }

    suspend fun initializeWordClassWithSubClasses(
        idName: String,
        displayText: String,
        subClassMap: Map<String, String>
    ): DatabaseResult<Long> {
        // First, insert the main class
        return mainClassRepository.insert(idName, displayText).flatMap { mainId ->
            // Then insert the default "NONE" subclass
            subClassRepository.insertLinkToMainClass(mainId, "NONE", "--NA--").flatMap {
                // Process each subClass in the provided map
                dbHandler.processBatch(subClassMap) { entry ->
                    subClassRepository.insertLinkToMainClass(mainId, entry.key, entry.value).map {  }
                }
            }.map {
                // Once batch processing is done, return the mainId of the word class
                mainId
            }
        }
    }
}