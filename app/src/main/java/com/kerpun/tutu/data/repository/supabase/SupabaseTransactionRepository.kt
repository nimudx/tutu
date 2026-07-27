package com.kerpun.tutu.data.repository.supabase

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.remote.dto.TransactionInsert
import com.kerpun.tutu.data.remote.dto.TransactionRow
import com.kerpun.tutu.data.remote.dto.TransactionUpdate
import com.kerpun.tutu.data.repository.TransactionRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private const val TABLE = "transactions"

class SupabaseTransactionRepository(private val postgrest: Postgrest) : TransactionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    init {
        scope.launch { refresh() }
    }

    override fun observeTransactions(): Flow<List<Transaction>> = transactions.asStateFlow()

    override suspend fun addTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
        occurredAt: LocalDate,
    ): Transaction {
        val row = postgrest.from(TABLE)
            .insert(
                TransactionInsert(
                    type = type.toDb(),
                    amount = amount,
                    categoryId = categoryId,
                    description = description,
                    occurredAt = occurredAt,
                ),
            ) {
                select()
            }
            .decodeSingle<TransactionRow>()
        refresh()
        return row.toDomain()
    }

    override suspend fun updateTransaction(
        id: Long,
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
    ) {
        postgrest.from(TABLE)
            .update(
                TransactionUpdate(
                    type = type.toDb(),
                    amount = amount,
                    categoryId = categoryId,
                    description = description,
                ),
            ) {
                filter { eq("id", id) }
            }
        refresh()
    }

    override suspend fun deleteTransaction(id: Long) {
        postgrest.from(TABLE).delete { filter { eq("id", id) } }
        refresh()
    }

    private suspend fun refresh() {
        val rows = postgrest.from(TABLE)
            .select { order(column = "occurred_at", order = Order.DESCENDING) }
            .decodeList<TransactionRow>()
        transactions.value = rows.map { it.toDomain() }
    }
}
