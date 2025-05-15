package io.github.kwvolt.japanesedictionary.domain.data.validation


fun validJapanese(text:String):Boolean{
    val regex = Regex("^[\u3000-\u303f\u3040-\u309f\u30a0-\u30ff\uff00-\uff9f\u4e00-\u9faf\u3400-\u4dbf]+$")
    return regex.matches(text)
}

fun validIdName(text: String): Boolean{
    val regex = Regex("^[A-Z_]+\$")
    return regex.matches(text)
}

fun validKana(text:String):Boolean{
    val regex = Regex("^[\u3000-\u303f\u3040-\u309f\u30a0-\u30ff\uff00-\uff9f\u4e00-\u9faf]+$")
    return regex.matches(text)
}

fun processIdName(text:String): String{
    return text.uppercase()
}

suspend fun <T> processValidateIdName(idName: String, block: suspend(String)->T): T {
    val tempIdName: String = processIdName(idName)
    if(validIdName(tempIdName)){
        return block(tempIdName)
    }
    else throw IllegalArgumentException("IdName does not meet the requirement of using only A-Z and _: $tempIdName")
}

enum class ValidationType {
    Japanese,
    Kana,
    IdName
}
