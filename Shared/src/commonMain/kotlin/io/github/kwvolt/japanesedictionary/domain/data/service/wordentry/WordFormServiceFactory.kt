package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntrySectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntrySectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.EntrySectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.sqlDelight.WordClassRepository

class WordFormServiceFactory(private val dbHandler: DatabaseHandler) {
    fun create(): WordFormService {
        val entryRepository = EntryRepository(dbHandler, dbHandler.queries.dictionaryEntryQueries)
        val entryNoteRepository = EntryNoteRepository(dbHandler, dbHandler.queries.dictionaryEntryNoteQueries)
        val entrySectionRepository = EntrySectionRepository(dbHandler, dbHandler.queries.dictionaryEntrySectionQueries)
        val entrySectionKanaRepository = EntrySectionKanaRepository(dbHandler, dbHandler.queries.dictionaryEntrySectionKanaQueries)
        val entrySectionNoteRepository = EntrySectionNoteRepository(dbHandler, dbHandler.queries.dictionaryEntrySectionNoteQueries)
        val wordClassRepository = WordClassRepository(dbHandler, dbHandler.queries.wordClassQueries)
        val mainClassRepository = MainClassRepository(dbHandler, dbHandler.queries.mainClassQueries)
        val subClassRepository = SubClassRepository(dbHandler, dbHandler.queries.subClassQueries)

        val wordEntryFormFetcher = WordEntryFormFetcher(
            dbHandler,
            entryRepository,
            entryNoteRepository,
            entrySectionRepository,
            entrySectionKanaRepository,
            entrySectionNoteRepository,
            mainClassRepository,
            subClassRepository,
            wordClassRepository
        )

        val wordClassBuilder = WordClassBuilder(
            mainClassRepository,
            subClassRepository
        )

        val wordEntryFormUpsert = WordEntryFormUpsert(
            dbHandler,
            entryRepository,
            entryNoteRepository,
            entrySectionRepository,
            entrySectionKanaRepository,
            entrySectionNoteRepository,
            wordClassRepository
        )

        val wordEntryFormDelete = WordEntryFormDelete(
            dbHandler,
            entryRepository,
            entryNoteRepository,
            entrySectionRepository,
            entrySectionKanaRepository,
            entrySectionNoteRepository,
        )

        val wordEntryFormValidation = WordEntryFormValidation()

        return WordFormService(
            wordEntryFormFetcher,
            wordEntryFormUpsert,
            wordEntryFormDelete,
            wordClassBuilder,
            wordEntryFormValidation
        )
    }
}
