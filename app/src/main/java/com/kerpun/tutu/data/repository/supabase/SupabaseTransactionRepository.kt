package com.kerpun.tutu.data.repository.supabase

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.remote.dto.RequiresApprovalRow
import com.kerpun.tutu.data.remote.dto.ReviewTransactionsParams
import com.kerpun.tutu.data.remote.dto.TransactionInsert
import com.kerpun.tutu.data.remote.dto.TransactionRow
import com.kerpun.tutu.data.remote.dto.TransactionUpdate
import com.kerpun.tutu.data.repository.TransactionRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
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
private const val SPACE_MEMBERS_TABLE = "space_members"

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
        val status = if (isGated(spaceId, userId)) TransactionStatus.PENDING else TransactionStatus.APPROVED
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
                    status = status.toDb(),
                ),
            ) {
                select()
            }
            .decodeSingle<TransactionRow>()
        refresh()
        return row.toDomain()
    }

    override suspend fun isGated(spaceId: String, userId: String): Boolean {
        return postgrest.from(SPACE_MEMBERS_TABLE)
            .select(columns = Columns.list("requires_approval")) {
                filter {
                    eq("space_id", spaceId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<RequiresApprovalRow>()
            ?.requiresApproval
            ?: false
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

    override suspend fun reviewTransactions(ids: List<Long>, approve: Boolean) {
        val status = if (approve) TransactionStatus.APPROVED else TransactionStatus.REJECTED
        postgrest.rpc("review_transactions", ReviewTransactionsParams(ids = ids, status = status.toDb()))
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
