package io.github.kwvolt.japanesedictionary.domain.data.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionarySearchRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.DictionaryUserRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionarySearchRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.DictionaryUserRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.service.user.UserFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormBuilder
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormDelete
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormItemFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormSearchFilter
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormValidation

class ServiceContainer(private val dbHandler: DatabaseHandler) {
    private val queries = dbHandler.queries
    private val dictionaryRepository: DictionaryRepositoryInterface by lazy {
        DictionaryRepository(
            dbHandler,
            queries.dictionaryEntryQueries
        )
    }
    private val dictionaryNoteRepository: DictionaryNoteRepositoryInterface by lazy {
        DictionaryNoteRepository(
            dbHandler,
            queries.dictionaryEntryNoteQueries
        )
    }
    private val dictionarySearchRepository: DictionarySearchRepositoryInterface by lazy {
        DictionarySearchRepository(
            dbHandler,
            queries.dictionarySearchQueries
        )
    }
    private val dictionaryUserRepository: DictionaryUserRepositoryInterface by lazy {
        DictionaryUserRepository(
            dbHandler,
            queries.dictionaryUserQueries
        )
    }
    private val sectionRepository: SectionRepositoryInterface by lazy {
        SectionRepository(
            dbHandler,
            queries.dictionaryEntrySectionQueries
        )
    }
    private val sectionKanaRepository: SectionKanaRepositoryInterface by lazy{
        SectionKanaRepository(
            dbHandler,
            queries.dictionaryEntrySectionKanaQueries
        )
    }
    private val sectionNoteRepository: SectionNoteRepositoryInterface by lazy{
        SectionNoteRepository(
            dbHandler,
            queries.dictionaryEntrySectionNoteQueries
        )
    }
    private val mainClassRepository: MainClassRepositoryInterface by lazy {
        MainClassRepository(
            dbHandler,
            queries.mainClassQueries
        )
    }
    private val subClassRepository: SubClassRepositoryInterface by lazy {
        SubClassRepository(
            dbHandler,
            queries.subClassQueries
        )
    }
    private val wordClassRepository: WordClassRepositoryInterface by lazy {
        WordClassRepository(
            dbHandler,
            queries.wordClassQueries
        )
    }

    val wordClassBuilder: WordClassBuilder by lazy {
        WordClassBuilder(mainClassRepository, subClassRepository)
    }
    val wordClassUpsert: WordClassUpsert by lazy {
        WordClassUpsert(dbHandler, mainClassRepository, subClassRepository, wordClassRepository)
    }

    val wordEntryFormDelete: WordEntryFormDelete by lazy {
        WordEntryFormDelete(
            dbHandler,
            dictionaryRepository,
            dictionaryNoteRepository,
            sectionRepository,
            sectionKanaRepository,
            sectionNoteRepository
        )
    }

    val wordEntryFormItemFetcher: WordEntryFormItemFetcher by lazy {
        WordEntryFormItemFetcher(
            wordClassFetcher,
            wordEntryFormFetcher
        )
    }

    val wordEntryFormUpsert: WordEntryFormUpsert by lazy {
        WordEntryFormUpsert(
            dbHandler,
            dictionaryRepository,
            dictionaryNoteRepository,
            sectionRepository,
            sectionKanaRepository,
            sectionNoteRepository,
            wordClassRepository,
            dictionaryUserRepository
        )
    }

    val wordEntryFormValidation: WordEntryFormValidation by lazy {
        WordEntryFormValidation()
    }

    val wordEntryFormUpsertValidation: WordEntryFormUpsertValidation by lazy {
        WordEntryFormUpsertValidation(
            wordEntryFormUpsert,
            wordEntryFormValidation
        )
    }

    val wordEntryFormSearchFilter: WordEntryFormSearchFilter by lazy {
        WordEntryFormSearchFilter(wordClassRepository, dictionarySearchRepository)
    }

    val wordClassFetcher: WordClassFetcher by lazy {
        WordClassFetcher(mainClassRepository, subClassRepository, wordClassRepository)
    }

    val userFetcher: UserFetcher by lazy { UserFetcher(dictionaryUserRepository) }

    val wordEntryFormFetcher: WordEntryFormFetcher by lazy {
        WordEntryFormFetcher(
            dictionaryRepository,
            dictionaryNoteRepository,
            sectionRepository,
            sectionKanaRepository,
            sectionNoteRepository
        )
    }

    val wordEntryFormBuilder: WordEntryFormBuilder by lazy {
        WordEntryFormBuilder(wordEntryFormItemFetcher, wordEntryFormFetcher)
    }

    fun <T> getServices(block: ServiceContainer.() -> T) : T {
        return block(this)
    }

    suspend fun <T> withServices(block: ServiceContainer.() -> T) : T {
        return block(this)
    }
}