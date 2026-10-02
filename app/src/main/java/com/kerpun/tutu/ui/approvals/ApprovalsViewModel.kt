package com.kerpun.tutu.ui.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.AuthState
import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.toBalanceSummary
import com.kerpun.tutu.data.repository.AuthRepository
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.todayLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApprovalsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val spaceRepository: SpaceRepository,
    authRepository: AuthRepository,
    activeSpaceId: StateFlow<String?>,
) : ViewModel() {

    private val spaceMembers = MutableStateFlow<List<SpaceMember>>(emptyList())
    private val currentUserId = authRepository.observeAuthState()
        .map { (it as? AuthState.SignedIn)?.userId }
    private val activeSpace = combine(spaceRepository.observeSpaces(), activeSpaceId) { spaces, id ->
        spaces.find { it.id == id }
    }

    /** Transaction ids the admin deselected from the default "everything checked" bulk approval. */
    private val uncheckedIds = MutableStateFlow<Set<Long>>(emptySet())

    private val transactionsAndCategories = combine(
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
    ) { transactions, categories -> transactions to categories }

    init {
        viewModelScope.launch {
            activeSpaceId.collect { spaceId ->
                spaceMembers.value = emptyList()
                if (spaceId != null) {
                    runCatching { spaceRepository.listMembers(spaceId) }
                        .onSuccess { members -> spaceMembers.value = members }
                }
            }
        }
    }

    val uiState: StateFlow<ApprovalsUiState> = combine(
        transactionsAndCategories,
        spaceMembers,
        currentUserId,
        activeSpace,
        uncheckedIds,
    ) { (transactions, categories), members, userId, space, unchecked ->
        val isAdmin = space?.role == SpaceRole.ADMIN
        val categoriesById = categories.associateBy { it.id }
        val batches = transactions.toApprovalBatches(categoriesById, members, userId, isAdmin, todayLocalDate(), unchecked)
        val pendingCount = batches.count { it.isPending }
        val pendingTotal = transactions.toBalanceSummary(categoriesById).pending
        val selectedIds = batches.flatMap { it.items }.filter { it.checked }.map { it.id }

        ApprovalsUiState(
            spaceName = space?.name ?: "",
            spaceColor = space?.color ?: "#4E8CFF",
            subtitle = if (pendingCount == 0) {
                "nada pendiente"
            } else {
                "$pendingCount ${if (pendingCount == 1) "lote" else "lotes"} · ${formatAmount(pendingTotal)}"
            },
            batches = batches,
            emptyText = if (batches.isEmpty()) {
                if (isAdmin) "No hay nada por aprobar" else "No tienes movimientos pendientes"
            } else {
                null
            },
            isLoading = false,
            selectedCount = selectedIds.size,
            approveBarVisible = isAdmin && selectedIds.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApprovalsUiState(),
    )

    fun toggleItem(id: Long) {
        uncheckedIds.update { current -> if (id in current) current - id else current + id }
    }

    private fun selectedIds(): List<Long> = uiState.value.batches.flatMap { it.items }.filter { it.checked }.map { it.id }

    fun approveSelected() {
        val ids = selectedIds()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            transactionRepository.reviewTransactions(ids, approve = true)
            uncheckedIds.value = emptySet()
        }
    }

    fun rejectSelected() {
        val ids = selectedIds()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            transactionRepository.reviewTransactions(ids, approve = false)
            uncheckedIds.value = emptySet()
        }
    }
}
