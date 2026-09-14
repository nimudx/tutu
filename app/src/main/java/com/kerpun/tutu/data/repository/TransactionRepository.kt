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

    /** Batch-approves or rejects pending transactions. Server-side checks the caller is an admin. */
    suspend fun reviewTransactions(ids: List<Long>, approve: Boolean)

    /** Whether [userId] currently needs an admin's approval for what they create in [spaceId]. */
    suspend fun isGated(spaceId: String, userId: String): Boolean
}
