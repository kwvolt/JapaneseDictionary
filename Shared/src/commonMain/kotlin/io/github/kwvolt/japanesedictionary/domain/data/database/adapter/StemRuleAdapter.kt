package io.github.kwvolt.japanesedictionary.domain.data.database.adapter

import app.cash.sqldelight.ColumnAdapter
import io.github.kwvolt.japanesedictionary.domain.model.conjugation.StemRule

class StemRuleAdapter: ColumnAdapter<StemRule, String> {
    override fun decode(databaseValue: String): StemRule =
        StemRule.fromValue(databaseValue) ?: error("Unknown StemRule in DB: $databaseValue")

    override fun encode(value: StemRule): String = value.value
}