package io.github.kwvolt.japanesedictionary.domain.data.service

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionarySearchRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.DictionaryUserRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.MainClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionKanaRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionNoteRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.dictionary.SectionRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.SubClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.wordclass.WordClassRepositoryInterface
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.DictionaryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.DictionaryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.DictionarySearchRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.DictionaryUserRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.SectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.SectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.dictionary.SectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.wordclass.WordClassRepository
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
    internal val dictionaryRepository: DictionaryRepositoryInterface by lazy {
        DictionaryRepository(
            dbHandler,
            queries.dictionaryEntryQueries,
            queries.dictionaryEntryLinkConjugationTemplateQueries
        )
    }
    internal val dictionaryNoteRepository: DictionaryNoteRepositoryInterface by lazy {
        DictionaryNoteRepository(
            dbHandler,
            queries.dictionaryEntryNoteQueries
        )
    }
    internal val dictionarySearchRepository: DictionarySearchRepositoryInterface by lazy {
        DictionarySearchRepository(
            dbHandler,
            queries.dictionarySearchQueries
        )
    }
    internal val dictionaryUserRepository: DictionaryUserRepositoryInterface by lazy {
        DictionaryUserRepository(
            dbHandler,
            queries.dictionaryUserQueries
        )
    }
    internal val sectionRepository: SectionRepositoryInterface by lazy {
        SectionRepository(
            dbHandler,
            queries.dictionaryEntrySectionQueries
        )
    }
    internal val sectionKanaRepository: SectionKanaRepositoryInterface by lazy{
        SectionKanaRepository(
            dbHandler,
            queries.dictionaryEntrySectionKanaQueries
        )
    }
    internal val sectionNoteRepository: SectionNoteRepositoryInterface by lazy{
        SectionNoteRepository(
            dbHandler,
            queries.dictionaryEntrySectionNoteQueries
        )
    }
    internal val mainClassRepository: MainClassRepositoryInterface by lazy {
        MainClassRepository(
            dbHandler,
            queries.mainClassQueries
        )
    }
    internal val subClassRepository: SubClassRepositoryInterface by lazy {
        SubClassRepository(
            dbHandler,
            queries.subClassQueries
        )
    }
    internal val wordClassRepository: WordClassRepositoryInterface by lazy {
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