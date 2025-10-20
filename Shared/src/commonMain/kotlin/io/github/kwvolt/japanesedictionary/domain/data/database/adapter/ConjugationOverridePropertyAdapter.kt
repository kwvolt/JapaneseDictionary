package io.github.kwvolt.japanesedictionary.domain.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.ConjugationOverrideProperty
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule

class ConjugationOverridePropertyAdapter: ColumnAdapter<ConjugationOverrideProperty, String> {
    override fun decode(databaseValue: String): ConjugationOverrideProperty =
        ConjugationOverrideProperty.fromValue(databaseValue) ?: error("Unknown ConjugationOverrideProperty in DB: $databaseValue")

    override fun encode(value: ConjugationOverrideProperty): String = value.value
}