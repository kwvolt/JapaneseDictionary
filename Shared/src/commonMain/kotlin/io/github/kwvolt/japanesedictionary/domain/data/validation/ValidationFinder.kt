package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem

fun findDuplicateIdentifiers(inputs: List<InputTextItem>): Set<String> {
    val nonEmptyInputs = inputs.filter { it.inputTextValue.trim().isNotEmpty() }
    val counts = nonEmptyInputs
        .groupingBy { it.inputTextValue }
        .eachCount()

    return nonEmptyInputs
        .filter { (counts[it.inputTextValue] ?: 0) > 1 }
        .map { it.itemProperties.getIdentifier() }
        .toSet()
}
