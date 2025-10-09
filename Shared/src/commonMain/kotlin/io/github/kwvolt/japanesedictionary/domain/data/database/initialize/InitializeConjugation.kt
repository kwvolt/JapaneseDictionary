package io.github.kwvolt.japanesedictionary.domain.data.database.initialize

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseHandlerBase
import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationOverrideProperty
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationUpsert
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationPatternUpsertContainer
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ConjugationTemplateInserter
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.ProvidedValue
import io.github.kwvolt.japanesedictionary.domain.data.service.conjugation.StemRule

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
                displayTextValue = ProvidedValue.Value("Causative-passive"
            )).returnOnFailure<Unit> { return@performTransaction it }
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
                displayTextValue = ProvidedValue.Value("Conditional")
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
        }.returnOnFailure { return it }
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
        }.returnOnFailure { return it }
        return DatabaseResult.Success(Unit)
    }
}