package io.github.kwvolt.japanesedictionary.domain.data.validation

import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.BaseItem


fun validJapanese(text:String):Boolean{
    val regex = Regex("^[\u3000-\u303F\u3040-\u309F\u30A0-\u30FF\uFF00-\uFF9F\uFF10-\uFF19\u4E00-\u9FAF\u3400-\u4DBF]+\$")
    return regex.matches(text)
}

fun validIdName(text: String): Boolean{
    val regex = Regex("^[A-Z_]+\$")
    return regex.matches(text)
}

fun validKana(text:String):Boolean{
    val regex = Regex("^[\u3000-\u303F\u3040-\u309F\u30A0-\u30FF\uFF00-\uFF9F]+\$")
    return regex.matches(text)
}

fun validateNotEmptyString(text:String): Boolean{
    return text.trim().isNotEmpty()
}

fun validateMaxLength(text: String, max: Int): Boolean = text.length <= max

fun validateNoDuplicate(text: String, textList: List<BaseItem>){

}