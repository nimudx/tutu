package com.kerpun.tutu.data.repository

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    /** True until the first successful load completes; false from then on. */
    fun observeIsLoading(): Flow<Boolean>

    suspend fun addTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
        occurredAt: LocalDate,
    ): Transaction

    suspend fun updateTransaction(
        id: Long,
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
    )

    suspend fun deleteTransaction(id: Long)
}
