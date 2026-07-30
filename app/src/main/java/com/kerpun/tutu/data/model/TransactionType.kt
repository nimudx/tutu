package com.kerpun.tutu.data.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    VAULT,
    ;

    fun toDb(): String = name.lowercase()

    companion object {
        fun fromDb(value: String): TransactionType = valueOf(value.uppercase())
    }
}
