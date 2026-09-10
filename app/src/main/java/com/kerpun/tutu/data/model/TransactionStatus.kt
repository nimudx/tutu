package com.kerpun.tutu.data.model

enum class TransactionStatus {
    APPROVED,
    PENDING,
    REJECTED,
    ;

    fun toDb(): String = name.lowercase()

    companion object {
        fun fromDb(value: String): TransactionStatus = valueOf(value.uppercase())
    }
}
