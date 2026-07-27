package com.kerpun.tutu.data.model

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long?,
    val description: String?,
    val occurredAt: LocalDate,
)
