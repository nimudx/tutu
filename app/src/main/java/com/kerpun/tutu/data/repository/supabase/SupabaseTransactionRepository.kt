package com.kerpun.tutu.data.repository.supabase

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.remote.dto.TransactionInsert
import com.kerpun.tutu.data.remote.dto.TransactionRow
import com.kerpun.tutu.data.remote.dto.TransactionUpdate
import com.kerpun.tutu.data.repository.TransactionRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private const val TABLE = "transactions"

class SupabaseTransactionRepository(
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val activeSpaceId: StateFlow<String?>,
) : TransactionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    init {
        scope.launch { activeSpaceId.collect { refresh() } }
    }

    override fun observeTransactions(): Flow<List<Transaction>> = transactions.asStateFlow()

    override fun observeIsLoading(): Flow<Boolean> = isLoading.asStateFlow()

    override suspend fun addTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
        occurredAt: LocalDate,
    ): Transaction {
        val spaceId = activeSpaceId.value ?: error("No hay un espacio activo")
        val userId = auth.currentUserOrNull()?.id ?: error("No hay sesión activa")
        val row = postgrest.from(TABLE)
            .insert(
                TransactionInsert(
                    type = type.toDb(),
                    amount = amount,
                    categoryId = categoryId,
                    description = description,
                    occurredAt = occurredAt,
                    spaceId = spaceId,
                    createdBy = userId,
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
        val spaceId = activeSpaceId.value
        if (spaceId == null) {
            transactions.value = emptyList()
            isLoading.value = false
            return
        }
        runCatching {
            postgrest.from(TABLE)
                .select {
                    filter { eq("space_id", spaceId) }
                    order(column = "occurred_at", order = Order.DESCENDING)
                }
                .decodeList<TransactionRow>()
                .map { it.toDomain() }
        }.onSuccess { transactions.value = it }
        // On failure keep whatever was last known instead of crashing the app.
        isLoading.value = false
    }
}
