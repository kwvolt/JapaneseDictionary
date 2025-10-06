package io.github.kwvolt.japanesedictionary.domain.data.database.initialize

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.database.getOrReturn
import io.github.kwvolt.japanesedictionary.domain.data.database.returnOnFailure
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationPatternUpsertContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationTemplateInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.StemRule

class InitializeConjugation (
    private val databaseHandler: DatabaseHandlerBase,
    private val conjugationUpsert: ConjugationUpsert,
) {
    suspend fun initialize(): DatabaseResult<Unit> {
        // patterns
        val patternResult = databaseHandler.performTransaction {
            conjugationUpsert.upsertPattern(idName = "CONJUNCTIVE", displayText = "Conjunctive")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "POLITE", displayText = "Polite")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "TE_FORM", displayText = "Te-form")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "TA_FORM", displayText = "Ta-form")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idName = "PRESENT_NEGATIVE",
                displayText = "Present Negative"
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "PAST_NEGATIVE", displayText = "Past Negative")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "VOLITIONAL", displayText = "Volitional")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "PASSIVE", displayText = "Passive")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "CAUSATIVE", displayText = "Causative")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idName = "CAUSATIVE_PASSIVE",
                displayText = "Causative-passive"
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "IMPERATIVE", displayText = "Imperative")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(
                idName = "POTENTIAL", displayText = "Potential",
                variantList = listOf(
                    ConjugationPatternUpsertContainer(
                        idName = "POTENTIAL_RA_LESS",
                        displayText = "Potential (ら-less)"
                    )
                )
            ).returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertPattern(idName = "CONDITIONAL", displayText = "Conditional")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
        }
        patternResult.returnOnFailure { return it.mapErrorTo() }

        // preprocess
        val preprocessResult = databaseHandler.performTransaction {
            StemRule.entries.forEach { rule ->
                conjugationUpsert.upsertPreprocess(idName = rule.toString())
                    .returnOnFailure { return@performTransaction it.mapErrorTo() }
            }
            DatabaseResult.Success(Unit)
        }
        preprocessResult.returnOnFailure { return it.mapErrorTo() }

        // verb suffix swaps
        val verbSuffixSwapResult = databaseHandler.performTransaction {
            conjugationUpsert.upsertVerbSuffixSwap(original = "う", replacement = "い")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "う", replacement = "え")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "う", replacement = "お")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "う", replacement = "っ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "う", replacement = "わ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "く", replacement = "い")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "く", replacement = "か")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "く", replacement = "き")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "く", replacement = "け")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "く", replacement = "こ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぐ", replacement = "い")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぐ", replacement = "が")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぐ", replacement = "ぎ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぐ", replacement = "げ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぐ", replacement = "ご")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "す", replacement = "さ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "す", replacement = "し")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "す", replacement = "せ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "す", replacement = "ぞ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "す", replacement = "そ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "つ", replacement = "た")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "つ", replacement = "ち")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "つ", replacement = "っ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "つ", replacement = "て")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "つ", replacement = "と")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぬ", replacement = "な")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぬ", replacement = "に")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぬ", replacement = "ね")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぬ", replacement = "の")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぬ", replacement = "ん")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "ち")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "ば")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "び")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "べ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "ぼ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "ぶ", replacement = "ん")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "む", replacement = "ま")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "む", replacement = "み")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "む", replacement = "め")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "む", replacement = "も")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "む", replacement = "ん")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "る", replacement = "っ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "る", replacement = "ら")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "る", replacement = "り")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "る", replacement = "れ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
            conjugationUpsert.upsertVerbSuffixSwap(original = "る", replacement = "ろ")
                .returnOnFailure { return@performTransaction it.mapErrorTo() }
        }
        verbSuffixSwapResult.returnOnFailure { return it.mapErrorTo() }

        // conjugation template
        ConjugationTemplateInserter().defineTemplate("RU_CONJUGATION", "る Verb Conjugation") {
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

                withPreprocess(StemRule.REPLACE_SUFFIX) {
                    getOrInsertConjugation("CONJUNCTIVE") {
                        nullSuffix(null){
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
                    getOrInsertConjugation("CONDITIONAL"){
                        nullSuffix("ば")   {
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
                    getOrInsertConjugation("TE_FORM"){
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
                    getOrInsertConjugation("TA_FORM"){
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
                    getOrInsertConjugation("PRESENT_NEGATIVE"){
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
                    getOrInsertConjugation("PAST_NEGATIVE"){
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
                    getOrInsertConjugation("VOLITIONAL"){
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
                    getOrInsertConjugation("PASSIVE"){
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
                    getOrInsertConjugation("CAUSATIVE"){
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
                    getOrInsertConjugation("CAUSATIVE_PASSIVE"){
                        shortSuffix("される"){
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
                    getOrInsertConjugation("POTENTIAL"){
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
                    getOrInsertConjugation("POLITE"){
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
        }
        return DatabaseResult.Success(Unit)
    }
}