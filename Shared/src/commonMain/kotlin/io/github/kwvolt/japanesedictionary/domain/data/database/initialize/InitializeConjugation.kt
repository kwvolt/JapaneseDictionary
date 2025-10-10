package io.github.kwvolt.japanesedictionary.domain.data.database.initialize

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationOverrideProperty
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationPatternUpsertContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationTemplateInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ProvidedValue
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.SpellingType
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.StemRule
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.UpsertOrDelete

class InitializeConjugation (
    private val databaseHandler: DatabaseHandlerBase,
    private val conjugationUpsert: ConjugationUpsert,
    private val conjugationTemplateInserter: ConjugationTemplateInserter
) {
    suspend fun initialize(): DatabaseResult<Unit> {
        // patterns
        val patternResult: DatabaseResult<Unit>  = databaseHandler.performTransaction {
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("CONJUNCTIVE"), displayTextValue = ProvidedValue.Value("Conjunctive"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("POLITE"), displayTextValue = ProvidedValue.Value("Polite"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("TE_FORM"), displayTextValue = ProvidedValue.Value("Te-form"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("TA_FORM"), displayTextValue = ProvidedValue.Value("Ta-form"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(
                idNameValue = ProvidedValue.Value("PRESENT_NEGATIVE"),
                displayTextValue = ProvidedValue.Value("Present Negative")
            ).returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("PAST_NEGATIVE"), displayTextValue = ProvidedValue.Value("Past Negative"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("VOLITIONAL"), displayTextValue = ProvidedValue.Value("Volitional"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("PASSIVE"), displayTextValue = ProvidedValue.Value("Passive"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("CAUSATIVE"), displayTextValue = ProvidedValue.Value("Causative"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(
                idNameValue = ProvidedValue.Value("CAUSATIVE_PASSIVE"),
                displayTextValue = ProvidedValue.Value("Causative-passive"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(idNameValue = ProvidedValue.Value("IMPERATIVE"), displayTextValue = ProvidedValue.Value("Imperative"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(
                idNameValue = ProvidedValue.Value("POTENTIAL"), displayTextValue = ProvidedValue.Value("Potential"),
                variantList = listOf(
                    ConjugationPatternUpsertContainer(
                        idNameValue = ProvidedValue.Value("POTENTIAL_RA_LESS"),
                        displayTextValue = ProvidedValue.Value("Potential (ら-less)")
                    )
                )
            ).returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertPattern(
                idNameValue = ProvidedValue.Value("CONDITIONAL"),
                displayTextValue = ProvidedValue.Value("Conditional"),
                variantList = listOf(
                    ConjugationPatternUpsertContainer(
                        idNameValue = ProvidedValue.Value("TARA_CONDITIONAL"),
                        displayTextValue = ProvidedValue.Value("たら Conditional")
                    ),
                    ConjugationPatternUpsertContainer(
                        idNameValue = ProvidedValue.Value("TO_CONDITIONAL"),
                        displayTextValue = ProvidedValue.Value("と Conditional")
                    )
                )
            ).returnOnFailure<Unit> { return@performTransaction it }
            DatabaseResult.Success(Unit)
        }
        patternResult.returnOnFailure { return it }

        // preprocess
        val preprocessResult = databaseHandler.performTransaction<Unit> {
            StemRule.entries.forEach { rule ->
                conjugationUpsert.upsertPreprocess(idNameValue = ProvidedValue.Value(rule.toString()))
                    .returnOnFailure<Unit> { return@performTransaction it }
            }
            DatabaseResult.Success(Unit)
        }
        preprocessResult.returnOnFailure { return it }

        // verb suffix swaps
        val verbSuffixSwapResult = databaseHandler.performTransaction {
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("う"), replacementValue = ProvidedValue.Value("い"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("う"), replacementValue = ProvidedValue.Value("え"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("う"), replacementValue = ProvidedValue.Value("お"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("う"), replacementValue = ProvidedValue.Value("っ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("う"), replacementValue = ProvidedValue.Value("わ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("く"), replacementValue = ProvidedValue.Value("い"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("く"), replacementValue = ProvidedValue.Value("か"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("く"), replacementValue = ProvidedValue.Value("き"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("く"), replacementValue = ProvidedValue.Value("け"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("く"), replacementValue = ProvidedValue.Value("こ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぐ"), replacementValue = ProvidedValue.Value("い"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぐ"), replacementValue = ProvidedValue.Value("が"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぐ"), replacementValue = ProvidedValue.Value("ぎ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぐ"), replacementValue = ProvidedValue.Value("げ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぐ"), replacementValue = ProvidedValue.Value("ご"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("す"), replacementValue = ProvidedValue.Value("さ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("す"), replacementValue = ProvidedValue.Value("し"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("す"), replacementValue = ProvidedValue.Value("せ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("す"), replacementValue = ProvidedValue.Value("ぞ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("す"), replacementValue = ProvidedValue.Value("そ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("つ"), replacementValue = ProvidedValue.Value("た"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("つ"), replacementValue = ProvidedValue.Value("ち"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("つ"), replacementValue = ProvidedValue.Value("っ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("つ"), replacementValue = ProvidedValue.Value("て"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("つ"), replacementValue = ProvidedValue.Value("と"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぬ"), replacementValue = ProvidedValue.Value("な"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぬ"), replacementValue = ProvidedValue.Value("に"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぬ"), replacementValue = ProvidedValue.Value("ね"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぬ"), replacementValue = ProvidedValue.Value("の"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぬ"), replacementValue = ProvidedValue.Value("ん"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("ち"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("ば"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("び"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("べ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("ぼ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("ぶ"), replacementValue = ProvidedValue.Value("ん"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("む"), replacementValue = ProvidedValue.Value("ま"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("む"), replacementValue = ProvidedValue.Value("み"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("む"), replacementValue = ProvidedValue.Value("め"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("む"), replacementValue = ProvidedValue.Value("も"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("む"), replacementValue = ProvidedValue.Value("ん"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("る"), replacementValue = ProvidedValue.Value("っ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("る"), replacementValue = ProvidedValue.Value("ら"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("る"), replacementValue = ProvidedValue.Value("り"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("る"), replacementValue = ProvidedValue.Value("れ"))
                .returnOnFailure<Unit> { return@performTransaction it }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = ProvidedValue.Value("る"), replacementValue = ProvidedValue.Value("ろ"))
                .returnOnFailure<Unit> { return@performTransaction it }
        }
        verbSuffixSwapResult.returnOnFailure{ return it }

        // conjugation template
        conjugationTemplateInserter.defineTemplate("RU_CONJUGATION", "る Verb Conjugation") {
            insert {
                withPreprocess(StemRule.DROP_RU) {
                    getOrInsertConjugation("CONJUNCTIVE") {
                        suffix(null, SHORT_FORM, IS_NEITHER)
                    }
                    getOrInsertConjugation("IMPERATIVE") {
                        suffix("ろ", SHORT_FORM, IS_POSITIVE)
                        suffix("るな", SHORT_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("CONDITIONAL") {
                        suffix("れば", NEITHER_FORM, IS_POSITIVE)
                        suffix("なければ", NEITHER_FORM, IS_NEGATIVE)
                    }

                    getOrInsertConjugation("TARA_CONDITIONAL") {
                        suffix("と", NEITHER_FORM, IS_NEITHER)
                    }

                    getOrInsertConjugation("TO_CONDITIONAL") {
                        suffix("たら", NEITHER_FORM, IS_NEITHER)
                    }

                    getOrInsertConjugation("TE_FORM") {
                        suffix("て", SHORT_FORM, IS_NEITHER)
                        suffix("まして", LONG_FORM, IS_NEITHER)
                    }
                    getOrInsertConjugation("TA_FORM") {
                        suffix("た", SHORT_FORM, IS_POSITIVE)
                        suffix("ました", LONG_FORM, IS_POSITIVE)
                    }
                    getOrInsertConjugation("PRESENT_NEGATIVE") {
                        suffix("ない", SHORT_FORM, IS_NEGATIVE)
                        suffix("ません", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("PAST_NEGATIVE") {
                        suffix("なかった", SHORT_FORM, IS_NEGATIVE)
                        suffix("ませんでした", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("VOLITIONAL") {
                        suffix("よう", SHORT_FORM, IS_POSITIVE)
                        suffix("ましょう", LONG_FORM, IS_POSITIVE)
                    }
                    getOrInsertConjugation("PASSIVE") {
                        suffix("られる", SHORT_FORM, IS_POSITIVE)
                        suffix("られます", LONG_FORM, IS_POSITIVE)
                        suffix("られない", SHORT_FORM, IS_NEGATIVE)
                        suffix("られません", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("CAUSATIVE") {
                        suffix("させる", SHORT_FORM, IS_POSITIVE)
                        suffix("させます", LONG_FORM, IS_POSITIVE)
                        suffix("させない", SHORT_FORM, IS_NEGATIVE)
                        suffix("させません", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("CAUSATIVE_PASSIVE") {
                        suffix("させられる", SHORT_FORM, IS_POSITIVE)
                        suffix("させられます", LONG_FORM, IS_POSITIVE)
                        suffix("させられない", SHORT_FORM, IS_NEGATIVE)
                        suffix("させられません", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("POTENTIAL") {
                        suffix("られる", SHORT_FORM, IS_POSITIVE)
                        suffix("られます", LONG_FORM, IS_POSITIVE)
                        suffix("られない", SHORT_FORM, IS_NEGATIVE)
                        suffix("られません", LONG_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("POTENTIAL_RA_LESS") {
                        suffix("れる", SHORT_FORM, IS_POSITIVE)
                        suffix("れない", SHORT_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("POLITE") {
                        suffix("ます", LONG_FORM, IS_POSITIVE)
                    }
                }
            }
        }.returnOnFailure { return it }

        conjugationTemplateInserter.defineTemplate("U_CONJUGATION", "う Verb Conjugation") {
            val uToI: Map<String, String> = mapOf(
                "う" to "い",
                "く" to "き",
                "ぐ" to "ぎ",
                "す" to "し",
                "つ" to "ち",
                "ぬ" to "に",
                "ぶ" to "び",
                "む" to "み",
                "る" to "り"
            )
            val uToE: Map<String, String> = mapOf(
                "う" to "え",
                "く" to "け",
                "ぐ" to "げ",
                "す" to "せ",
                "つ" to "て",
                "ぬ" to "ね",
                "ぶ" to "べ",
                "む" to "め",
                "る" to "れ"
            )

            val uToTe: Map<String, String> = mapOf(
                "う" to "っ",
                "く" to "い",
                "ぐ" to "い",
                "す" to "し",
                "つ" to "っ",
                "ぬ" to "ん",
                "ぶ" to "ん",
                "む" to "ん",
                "る" to "っ"
            )

            val uToA: Map<String, String> = mapOf(
                "う" to "わ",
                "く" to "か",
                "ぐ" to "が",
                "す" to "さ",
                "つ" to "た",
                "ぬ" to "な",
                "ぶ" to "ば",
                "む" to "ま",
                "る" to "ら"
            )

            val uToO: Map<String, String> = mapOf(
                "う" to "お",
                "く" to "こ",
                "ぐ" to "ご",
                "す" to "そ",
                "つ" to "と",
                "ぬ" to "の",
                "ぶ" to "ぼ",
                "む" to "も",
                "る" to "ろ"
            )
            insert {
                withPreprocess(StemRule.REPLACE_SUFFIX) {
                    getOrInsertConjugation("CONJUNCTIVE") {
                        suffix(null, SHORT_FORM, IS_NEITHER, swap = { linkVerbSuffixSwap(uToI) })
                    }
                    getOrInsertConjugation("IMPERATIVE") {
                        suffix(null, SHORT_FORM, IS_POSITIVE)
                        suffix("な", SHORT_FORM, IS_NEGATIVE)
                    }
                    getOrInsertConjugation("CONDITIONAL") {
                        suffix("ば", NEITHER_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToE) })
                        suffix("なければ", NEITHER_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToE) })
                    }

                    getOrInsertConjugation("TARA_CONDITIONAL") {
                        suffix("たら", NEITHER_FORM, IS_NEITHER, swap = { linkVerbSuffixSwap(uToE) })
                    }

                    getOrInsertConjugation("TO_CONDITIONAL") {
                        suffix("と", NEITHER_FORM, IS_NEITHER, swap = { linkVerbSuffixSwap(uToE) })
                    }

                    getOrInsertConjugation("TE_FORM") {
                        suffix("て", SHORT_FORM, IS_NEITHER, swap = { linkVerbSuffixSwap(uToTe) })
                        suffix("まして", LONG_FORM, IS_NEITHER, swap = { linkVerbSuffixSwap(uToTe) })
                    }
                    getOrInsertConjugation("TA_FORM") {
                        suffix("た", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToTe) })
                        suffix("ました", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToI) })
                    }
                    getOrInsertConjugation("PRESENT_NEGATIVE") {
                        suffix("ない", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("ません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToI) })
                    }
                    getOrInsertConjugation("PAST_NEGATIVE") {
                        suffix("なかった", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("ませんでした", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToI) })
                    }
                    getOrInsertConjugation("VOLITIONAL") {
                        suffix("う", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToO) })
                        suffix("ましょう", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToI) })
                    }
                    getOrInsertConjugation("PASSIVE") {
                        suffix("れる", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("れます", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("れない", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("れません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                    }
                    getOrInsertConjugation("CAUSATIVE") {
                        suffix("せる", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("せます", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("せない", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("せません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                    }
                    getOrInsertConjugation("CAUSATIVE_PASSIVE") {
                        suffix("される", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("されます", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("されない", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                        suffix("されません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToA) })
                    }
                    getOrInsertConjugation("POTENTIAL") {
                        suffix("る", SHORT_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToE) })
                        suffix("ます", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToE) })
                        suffix("ない", SHORT_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToE) })
                        suffix("ません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToE) })
                    }
                    getOrInsertConjugation("POLITE") {
                        suffix("ます", LONG_FORM, IS_POSITIVE, swap = { linkVerbSuffixSwap(uToI) })
                        suffix("ません", LONG_FORM, IS_NEGATIVE, swap = { linkVerbSuffixSwap(uToI) })
                    }
                }
            }
        }.returnOnFailure { return it }

        //override property
        val propertyResult = databaseHandler.performTransaction {
            ConjugationOverrideProperty.entries.forEach { property: ConjugationOverrideProperty ->
                conjugationUpsert.upsertProperty(
                    idNameValue = ProvidedValue.Value(property.toString())
                ).returnOnFailure<Unit> { return@performTransaction it }
            }
            DatabaseResult.Success(Unit)
        }
        propertyResult.returnOnFailure { return it }

        //override
        conjugationTemplateInserter.defineTemplate("KURU_CONJUGATION", "くる Conjugation") {
            // temporary (example)
            val kuruId: Long = 1

            val kiKana: Map<ConjugationOverrideProperty, String?> = mapOf(
                ConjugationOverrideProperty.STEM_REPLACEMENT to "き",
                ConjugationOverrideProperty.SPELLING_TYPE to SpellingType.FURIGANA.toString()
            )
            val koKana: Map<ConjugationOverrideProperty, String?> = mapOf(
                ConjugationOverrideProperty.STEM_REPLACEMENT to "こ",
                ConjugationOverrideProperty.SPELLING_TYPE to SpellingType.FURIGANA.toString()
            )
            insert {
                withPreprocess(StemRule.DROP_RU) {
                    getOrInsertConjugation("CONJUNCTIVE") {
                        suffix(null, SHORT_FORM, IS_NEITHER){
                            insertOverride("KURU_CONJUNCTIVE_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("IMPERATIVE") {
                        suffix("ろ", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_IMPERATIVE_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("るな", SHORT_FORM, IS_NEGATIVE) {
                            insertOverride("KURU_IMPERATIVE_NEGATIVE_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("CONDITIONAL") {
                        suffix("れば", NEITHER_FORM, IS_POSITIVE)
                        suffix("なければ", NEITHER_FORM, IS_NEGATIVE){
                            insertOverride("KURU_CONDITIONAL_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }

                    getOrInsertConjugation("TARA_CONDITIONAL") {
                        suffix("と", NEITHER_FORM, IS_NEITHER){
                            insertOverride("KURU_TARA_CONDITIONAL_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }

                    getOrInsertConjugation("TO_CONDITIONAL") {
                        suffix("たら", NEITHER_FORM, IS_NEITHER){
                            insertOverride("KURU_TO_CONDITIONAL_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }

                    getOrInsertConjugation("TE_FORM") {
                        suffix("て", SHORT_FORM, IS_NEITHER){
                            insertOverride("KURU_TE_FORM_SHORT_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                        suffix("まして", LONG_FORM, IS_NEITHER){
                            insertOverride("KURU_TE_FORM_LONG_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("TA_FORM") {
                        suffix("た", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_TA_FORM_SHORT_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                        suffix("ました", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_TA_FORM_LONG_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("PRESENT_NEGATIVE") {
                        suffix("ない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PRESENT_NEGATIVE_SHORT_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("ません", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PRESENT_NEGATIVE_LONG_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("PAST_NEGATIVE") {
                        suffix("なかった", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PAST_NEGATIVE_SHORT_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("ませんでした", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PAST_NEGATIVE_LONG_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("VOLITIONAL") {
                        suffix("よう", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_VOLITIONAL_SHORT_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("ましょう", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_VOLITIONAL_LONG_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                    getOrInsertConjugation("PASSIVE") {
                        suffix("られる", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_PASSIVE_SHORT_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られます", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_PASSIVE_LONG_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PASSIVE_SHORT_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られません", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_PASSIVE_LONG_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }
                    getOrInsertConjugation("CAUSATIVE") {
                        suffix("させる", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_CAUSATIVE_SHORT_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させます", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_CAUSATIVE_LONG_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_CAUSATIVE_SHORT_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させません", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_CAUSATIVE_LONG_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }
                    getOrInsertConjugation("CAUSATIVE_PASSIVE") {
                        suffix("させられる", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_CAUSATIVE_PASSIVE_SHORT_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させられます", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_CAUSATIVE_PASSIVE_LONG_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させられない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_CAUSATIVE_PASSIVE_SHORT_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("させられません", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_CAUSATIVE_PASSIVE_LONG_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }
                    getOrInsertConjugation("POTENTIAL") {
                        suffix("られる", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_POTENTIAL_SHORT_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られます", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_POTENTIAL_LONG_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_POTENTIAL_SHORT_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("られません", LONG_FORM, IS_NEGATIVE){
                            insertOverride("KURU_POTENTIAL_LONG_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }
                    getOrInsertConjugation("POTENTIAL_RA_LESS") {
                        suffix("れる", SHORT_FORM, IS_POSITIVE){
                            insertOverride("KURU_POTENTIAL_RA_LESS_POSITIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                        suffix("れない", SHORT_FORM, IS_NEGATIVE){
                            insertOverride("KURU_POTENTIAL_RA_LESS_NEGATIVE_KANA", kuruId, "Irregular Verb", koKana)
                        }
                    }
                    getOrInsertConjugation("POLITE") {
                        suffix("ます", LONG_FORM, IS_POSITIVE){
                            insertOverride("KURU_POLITE_KANA", kuruId, "Irregular Verb", kiKana)
                        }
                    }
                }
            }
        }.returnOnFailure { return it }


        return DatabaseResult.Success(Unit)
    }

    companion object {
        const val SHORT_FORM: Boolean = true
        const val LONG_FORM: Boolean = false
        val NEITHER_FORM: Boolean? = null

        const val IS_POSITIVE: Boolean = true
        const val IS_NEGATIVE: Boolean = false
        val IS_NEITHER: Boolean? = null
    }
}