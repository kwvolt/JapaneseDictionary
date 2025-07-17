package io.github.kwvolt.japanesedictionary.domain.data.database

enum class DatabaseErrorType() {
    UNIQUE,
    FOREIGN_KEY,
    CHECK,
    NOT_NULL,
    NOT_FOUND
}

fun databaseErrorMessage(databaseErrorType: DatabaseErrorType): String{
    return when(databaseErrorType){
        DatabaseErrorType.UNIQUE -> "Value Already Exists In Database"
        DatabaseErrorType.FOREIGN_KEY -> "Reference To Parent Missing"
        DatabaseErrorType.CHECK -> "Invalid Input"
        DatabaseErrorType.NOT_NULL -> "VALUE is Required"
        DatabaseErrorType.NOT_FOUND -> "Value does not exists"
    }
}

fun formatDatabaseErrorTypeToMessage(databaseError: DatabaseError): String {
    return databaseErrorMessage(databaseError.type)
}


