package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult

enum class ValidationType() {
    Japanese,
    Kana,
    IdName,
    Empty,
    Duplicate,
    Section,
    InvalidSelection,
    MaxLength,
    KanaSection,
}

fun validationTypeErrorMessage(validationType: ValidationType): String{
    return when(validationType){
        ValidationType.Japanese ->  "Please make sure the text is in japanese"
        ValidationType.Kana -> "Please make sure the text is in hiragana/katakana"
        ValidationType.IdName -> "Please make sure the value only contains capital Letters (A-Z) and underscore (_)"
        ValidationType.Empty -> "Please enter a value"
        ValidationType.Duplicate -> "Duplicated value detected"
        ValidationType.Section -> ""
        ValidationType.InvalidSelection -> "Combination not found"
        ValidationType.MaxLength -> ""
        ValidationType.KanaSection -> "Please provide at least one kana reading for this section"
    }
}

fun formatValidationTypeToMessage(validationTypes: List<DatabaseResult.InvalidInput>): String {
    return validationTypes
        .mapNotNull { validationTypeErrorMessage(it.invalidType).takeIf { msg -> msg.isNotBlank() } }
        .joinToString(separator = "\n") { "• $it" }
}
