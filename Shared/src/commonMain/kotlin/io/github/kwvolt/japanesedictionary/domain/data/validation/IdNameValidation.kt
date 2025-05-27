package io.github.kwvolt.japanesedictionary.domain.data.validation


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