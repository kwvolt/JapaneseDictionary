package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem

fun findDuplicateIdentifiers(inputs: List<TextItem>): Set<String> {
    val nonEmptyInputs = inputs.filter { it.inputTextValue.trim().isNotEmpty() }
    val counts = nonEmptyInputs
        .groupingBy { it.inputTextValue }
        .eachCount()

    return nonEmptyInputs
        .filter { (counts[it.inputTextValue] ?: 0) > 1 }
        .map { it.itemProperties.getIdentifier() }
        .toSet()
}
