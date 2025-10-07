package io.github.kwvolt.japanesedictionary.domain.data.database.initialize

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.getOrReturn
import io.github.kwvolt.japanesedictionary.domain.data.database.returnOnFailure
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationPatternUpsertContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationTemplateInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.NullableValue
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.StemRule

class InitializeConjugation (
    private val databaseHandler: DatabaseHandlerBase,
    private val conjugationUpsert: ConjugationUpsert,
    private val conjugationTemplateInserter: ConjugationTemplateInserter
) {
    suspend fun initialize(): DatabaseResult<Unit> {
        // patterns
        val patternResult: DatabaseResult<Unit>  = databaseHandler.performTransaction {
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("CONJUNCTIVE"), displayTextValue = NullableValue.Value("Conjunctive"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("POLITE"), displayTextValue = NullableValue.Value("Polite"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("TE_FORM"), displayTextValue = NullableValue.Value("Te-form"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("TA_FORM"), displayTextValue = NullableValue.Value("Ta-form"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idNameValue = NullableValue.Value("PRESENT_NEGATIVE"),
                displayTextValue = NullableValue.Value("Present Negative")
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("PAST_NEGATIVE"), displayTextValue = NullableValue.Value("Past Negative"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("VOLITIONAL"), displayTextValue = NullableValue.Value("Volitional"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("PASSIVE"), displayTextValue = NullableValue.Value("Passive"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("CAUSATIVE"), displayTextValue = NullableValue.Value("Causative"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idNameValue = NullableValue.Value("CAUSATIVE_PASSIVE"),
                displayTextValue = NullableValue.Value("Causative-passive"
            )).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idNameValue = NullableValue.Value("IMPERATIVE"), displayTextValue = NullableValue.Value("Imperative"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idNameValue = NullableValue.Value("POTENTIAL"), displayTextValue = NullableValue.Value("Potential"),
                variantList = listOf(
                    ConjugationPatternUpsertContainer(
                        idNameValue = NullableValue.Value("POTENTIAL_RA_LESS"),
                        displayTextValue = NullableValue.Value("Potential (ら-less)")
                    )
                )
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idNameValue = NullableValue.Value("CONDITIONAL"), 
                displayTextValue = NullableValue.Value("Conditional")
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            DatabaseResult.Success(Unit)
        }
        patternResult.returnOnFailure { return it.mapErrorTo() }

        // preprocess
        val preprocessResult = databaseHandler.performTransaction {
            StemRule.entries.forEach { rule ->
                conjugationUpsert.upsertPreprocess(idNameValue = NullableValue.Value(rule.toString()))
                    .returnOnFailure { return@performTransaction it.mapErrorTo() }
            }
            DatabaseResult.Success(Unit)
        }
        preprocessResult.returnOnFailure { return it.mapErrorTo() }

        // verb suffix swaps
        val verbSuffixSwapResult = databaseHandler.performTransaction {
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("う"), replacementValue = NullableValue.Value("い"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("う"), replacementValue = NullableValue.Value("え"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("う"), replacementValue = NullableValue.Value("お"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("う"), replacementValue = NullableValue.Value("っ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("う"), replacementValue = NullableValue.Value("わ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("く"), replacementValue = NullableValue.Value("い"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("く"), replacementValue = NullableValue.Value("か"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("く"), replacementValue = NullableValue.Value("き"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("く"), replacementValue = NullableValue.Value("け"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("く"), replacementValue = NullableValue.Value("こ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぐ"), replacementValue = NullableValue.Value("い"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぐ"), replacementValue = NullableValue.Value("が"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぐ"), replacementValue = NullableValue.Value("ぎ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぐ"), replacementValue = NullableValue.Value("げ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぐ"), replacementValue = NullableValue.Value("ご"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("す"), replacementValue = NullableValue.Value("さ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("す"), replacementValue = NullableValue.Value("し"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("す"), replacementValue = NullableValue.Value("せ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("す"), replacementValue = NullableValue.Value("ぞ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("す"), replacementValue = NullableValue.Value("そ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("つ"), replacementValue = NullableValue.Value("た"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("つ"), replacementValue = NullableValue.Value("ち"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("つ"), replacementValue = NullableValue.Value("っ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("つ"), replacementValue = NullableValue.Value("て"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("つ"), replacementValue = NullableValue.Value("と"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぬ"), replacementValue = NullableValue.Value("な"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぬ"), replacementValue = NullableValue.Value("に"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぬ"), replacementValue = NullableValue.Value("ね"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぬ"), replacementValue = NullableValue.Value("の"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぬ"), replacementValue = NullableValue.Value("ん"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("ち"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("ば"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("び"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("べ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("ぼ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("ぶ"), replacementValue = NullableValue.Value("ん"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("む"), replacementValue = NullableValue.Value("ま"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("む"), replacementValue = NullableValue.Value("み"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("む"), replacementValue = NullableValue.Value("め"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("む"), replacementValue = NullableValue.Value("も"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("む"), replacementValue = NullableValue.Value("ん"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("る"), replacementValue = NullableValue.Value("っ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("る"), replacementValue = NullableValue.Value("ら"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("る"), replacementValue = NullableValue.Value("り"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("る"), replacementValue = NullableValue.Value("れ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(originalValue = NullableValue.Value("る"), replacementValue = NullableValue.Value("ろ"))
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
        }
        verbSuffixSwapResult.returnOnFailure { return it.mapErrorTo() }

        //override property

        // conjugation template
        conjugationTemplateInserter.defineTemplate("RU_CONJUGATION", "る Verb Conjugation") {
            insert {
                withPreprocess(StemRule.DROP_RU) {
                    getOrInsertConjugation("CONJUNCTIVE"){ nullSuffix(null)}
                    getOrInsertConjugation("IMPERATIVE"){ nullSuffix("ろ")}
                    getOrInsertConjugation("CONDITIONAL"){ nullSuffix("れば") }
                    getOrInsertConjugation("TE_FORM"){
                        shortSuffix("て")
                        longSuffix("まして")
                    }
                    getOrInsertConjugation("TA_FORM"){
                        shortSuffix("た")
                        longSuffix("ました")
                    }
                    getOrInsertConjugation("PRESENT_NEGATIVE"){
                        shortSuffix("ない")
                        longSuffix("ません")
                    }
                    getOrInsertConjugation("PAST_NEGATIVE"){
                        shortSuffix("なかった")
                        longSuffix("ませんでした")
                    }
                    getOrInsertConjugation("PAST_NEGATIVE"){
                        shortSuffix("なかった")
                        longSuffix("ませんでした")
                    }
                    getOrInsertConjugation("VOLITIONAL"){
                        shortSuffix("よう")
                        longSuffix("ましょう")
                    }
                    getOrInsertConjugation("PASSIVE"){
                        shortSuffix("られる")
                        longSuffix("られます")
                    }
                    getOrInsertConjugation("CAUSATIVE"){
                        shortSuffix("させる")
                        longSuffix("させます")
                    }
                    getOrInsertConjugation("CAUSATIVE_PASSIVE"){
                        shortSuffix("させられる")
                        longSuffix("させられます")
                    }
                    getOrInsertConjugation("POTENTIAL"){
                        shortSuffix("られる")
                        longSuffix("られます")
                    }
                    getOrInsertConjugation("POTENTIAL_RA_LESS"){
                        shortSuffix("れる")
                    }
                    getOrInsertConjugation("POLITE"){
                        shortSuffix("ます")
                    }
                }
            }
        }.returnOnFailure { return it.mapErrorTo() }
        conjugationTemplateInserter.defineTemplate("U_CONJUGATION", "う Verb Conjugation") {
            insert {
                withPreprocess(StemRule.REPLACE_SUFFIX) {
                    getOrInsertConjugation("CONJUNCTIVE") {
                        nullSuffix(null) {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("IMPERATIVE") {
                        nullSuffix(null) {
                            linkVerbSuffixSwap("う", "え")
                            linkVerbSuffixSwap("く", "け")
                            linkVerbSuffixSwap("ぐ", "げ")
                            linkVerbSuffixSwap("す", "せ")
                            linkVerbSuffixSwap("つ", "て")
                            linkVerbSuffixSwap("ぬ", "ね")
                            linkVerbSuffixSwap("ぶ", "べ")
                            linkVerbSuffixSwap("む", "め")
                            linkVerbSuffixSwap("る", "れ")
                        }
                    }
                    getOrInsertConjugation("CONDITIONAL") {
                        nullSuffix("ば") {
                            linkVerbSuffixSwap("う", "え")
                            linkVerbSuffixSwap("く", "け")
                            linkVerbSuffixSwap("ぐ", "げ")
                            linkVerbSuffixSwap("す", "せ")
                            linkVerbSuffixSwap("つ", "て")
                            linkVerbSuffixSwap("ぬ", "ね")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "め")
                            linkVerbSuffixSwap("る", "れ")
                        }
                    }
                    getOrInsertConjugation("TE_FORM") {
                        shortSuffix("て") {
                            linkVerbSuffixSwap("う", "っ")
                            linkVerbSuffixSwap("く", "い")
                            linkVerbSuffixSwap("ぐ", "い")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "っ")
                            linkVerbSuffixSwap("ぬ", "ん")
                            linkVerbSuffixSwap("ぶ", "ん")
                            linkVerbSuffixSwap("む", "ん")
                            linkVerbSuffixSwap("る", "っ")
                        }
                        longSuffix("まして") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("TA_FORM") {
                        shortSuffix("た") {
                            linkVerbSuffixSwap("う", "っ")
                            linkVerbSuffixSwap("く", "い")
                            linkVerbSuffixSwap("ぐ", "い")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "っ")
                            linkVerbSuffixSwap("ぬ", "ん")
                            linkVerbSuffixSwap("ぶ", "ん")
                            linkVerbSuffixSwap("む", "ん")
                            linkVerbSuffixSwap("る", "っ")
                        }
                        longSuffix("ました") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("PRESENT_NEGATIVE") {
                        shortSuffix("ない") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                        longSuffix("ません") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("PAST_NEGATIVE") {
                        shortSuffix("なかった") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                        longSuffix("ませんでした") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("VOLITIONAL") {
                        shortSuffix("う") {
                            linkVerbSuffixSwap("う", "お")
                            linkVerbSuffixSwap("く", "こ")
                            linkVerbSuffixSwap("ぐ", "ご")
                            linkVerbSuffixSwap("す", "そ")
                            linkVerbSuffixSwap("つ", "と")
                            linkVerbSuffixSwap("ぬ", "の")
                            linkVerbSuffixSwap("ぶ", "ぼ")
                            linkVerbSuffixSwap("む", "も")
                            linkVerbSuffixSwap("る", "ろ")
                        }
                        longSuffix("ましょう") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                    getOrInsertConjugation("PASSIVE") {
                        shortSuffix("れる") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                        longSuffix("れます") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                    }
                    getOrInsertConjugation("CAUSATIVE") {
                        shortSuffix("せる") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                        longSuffix("せます") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                    }
                    getOrInsertConjugation("CAUSATIVE_PASSIVE") {
                        shortSuffix("される") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                        longSuffix("されます") {
                            linkVerbSuffixSwap("う", "わ")
                            linkVerbSuffixSwap("く", "か")
                            linkVerbSuffixSwap("ぐ", "が")
                            linkVerbSuffixSwap("す", "さ")
                            linkVerbSuffixSwap("つ", "た")
                            linkVerbSuffixSwap("ぬ", "な")
                            linkVerbSuffixSwap("ぶ", "ば")
                            linkVerbSuffixSwap("む", "ま")
                            linkVerbSuffixSwap("る", "ら")
                        }
                    }
                    getOrInsertConjugation("POTENTIAL") {
                        shortSuffix("る") {
                            linkVerbSuffixSwap("う", "え")
                            linkVerbSuffixSwap("く", "け")
                            linkVerbSuffixSwap("ぐ", "げ")
                            linkVerbSuffixSwap("す", "せ")
                            linkVerbSuffixSwap("つ", "て")
                            linkVerbSuffixSwap("ぬ", "ね")
                            linkVerbSuffixSwap("ぶ", "べ")
                            linkVerbSuffixSwap("む", "め")
                            linkVerbSuffixSwap("る", "れ")
                        }
                        longSuffix("ます") {
                            linkVerbSuffixSwap("う", "え")
                            linkVerbSuffixSwap("く", "け")
                            linkVerbSuffixSwap("ぐ", "げ")
                            linkVerbSuffixSwap("す", "せ")
                            linkVerbSuffixSwap("つ", "て")
                            linkVerbSuffixSwap("ぬ", "ね")
                            linkVerbSuffixSwap("ぶ", "べ")
                            linkVerbSuffixSwap("む", "め")
                            linkVerbSuffixSwap("る", "れ")
                        }
                    }
                    getOrInsertConjugation("POLITE") {
                        longSuffix("ます") {
                            linkVerbSuffixSwap("う", "い")
                            linkVerbSuffixSwap("く", "き")
                            linkVerbSuffixSwap("ぐ", "ぎ")
                            linkVerbSuffixSwap("す", "し")
                            linkVerbSuffixSwap("つ", "ち")
                            linkVerbSuffixSwap("ぬ", "に")
                            linkVerbSuffixSwap("ぶ", "び")
                            linkVerbSuffixSwap("む", "み")
                            linkVerbSuffixSwap("る", "り")
                        }
                    }
                }
            }
        }.returnOnFailure { return it.mapErrorTo() }

        return DatabaseResult.Success(Unit)


    }
}