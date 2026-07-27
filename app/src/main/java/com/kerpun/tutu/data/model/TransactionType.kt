package com.kerpun.tutu.data.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    ;

    fun toDb(): String = name.lowercase()

    companion object {
        fun fromDb(value: String): TransactionType = valueOf(value.uppercase())
    }
}
