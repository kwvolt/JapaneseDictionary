package io.github.kwvolt.japanesedictionary.domain.data.service.wordclass

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
            subClassRepository.insertLinkToMainClass(mainId, DEFAULT_ID_NAME, DEFAULT_DISPLAY_TEXT)
        }
    }

    suspend fun initializeWordClass(
        idName: String,
        displayText: String,
        subClassMap: Map<String, String>
    ): DatabaseResult<Long> {
        return mainClassRepository.insert(idName, displayText).flatMap { mainId ->
            subClassRepository.insertLinkToMainClass(mainId, DEFAULT_ID_NAME, DEFAULT_DISPLAY_TEXT).flatMap {
                dbHandler.processBatchWrite(subClassMap) { entry ->
                    subClassRepository.insertLinkToMainClass(mainId, entry.key, entry.value).map {  }
                }
            }.map {
                mainId
            }
        }
    }

    companion object {
        /**Default IdName used within both main and sub class repo when no value and unknown*/
        const val DEFAULT_ID_NAME = "NONE"

        /**Default displayText used within both main and sub class repo when no value and unknown*/
        const val DEFAULT_DISPLAY_TEXT = "--NA--"
    }
}