package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.EntryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.EntryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.EntrySectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.EntrySectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.EntrySectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.WordClassRepository

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

        val wordEntryFormBuilder = WordEntryFormBuilder(
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
            wordEntryFormBuilder,
            wordEntryFormUpsert,
            wordEntryFormDelete,
            wordClassBuilder,
            wordEntryFormValidation
        )
    }
}
