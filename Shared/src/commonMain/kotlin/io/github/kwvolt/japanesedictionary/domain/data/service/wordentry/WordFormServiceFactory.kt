package io.github.kwvolt.japanesedictionary.domain.data.service.wordentry

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandler
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentry.EntryRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionKanaRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionNoteRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.dictionaryentrysection.EntrySectionRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassRepository
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.WordClassRepository

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
            entryRepository
        )

        val wordEntryFormValidation = WordEntryFormValidation(dbHandler, wordClassRepository)

        return WordFormService(
            wordEntryFormBuilder,
            wordEntryFormUpsert,
            wordEntryFormDelete,
            wordClassBuilder,
            wordEntryFormValidation
        )
    }
}
