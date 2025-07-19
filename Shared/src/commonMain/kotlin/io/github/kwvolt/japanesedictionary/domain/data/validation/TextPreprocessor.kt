package io.github.kwvolt.japanesedictionary.domain.data.validation

import java.text.Normalizer

object TextPreprocessor {
    fun cleanInput(raw: String): String {
        return raw
            .trim()
            .replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "") // Remove invisible characters
            .replace(Regex("\\s+"), " ")                   // Collapse whitespace
            .let { Normalizer.normalize(it, Normalizer.Form.NFKC) } // Unicode normalization
    }
}