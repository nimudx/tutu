package com.kerpun.tutu.ui.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.AuthState
import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.Space
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.model.toBalanceSummary
import com.kerpun.tutu.data.repository.AuthRepository
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.approvals.toApprovalBatches
import com.kerpun.tutu.ui.common.AccentFixed
import com.kerpun.tutu.ui.common.PendingAccent
import com.kerpun.tutu.ui.common.TransactionToastEvent
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.authorLabelFor
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.todayLocalDate
import com.kerpun.tutu.ui.common.toAvatarUi
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.common.toUi
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private const val RECENT_TRANSACTIONS_LIMIT = 5
private const val WEEK_LENGTH_DAYS = 7

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val spaceRepository: SpaceRepository,
    private val authRepository: AuthRepository,
    activeSpaceId: StateFlow<String?>,
) : ViewModel() {

    private val activeSpace = combine(spaceRepository.observeSpaces(), activeSpaceId) { spaces, id ->
        spaces.find { it.id == id }
    }

    private val transactionsAndCategories = combine(
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
    ) { transactions, categories -> transactions to categories }

    private val isDataLoading = combine(
        transactionRepository.observeIsLoading(),
        categoryRepository.observeIsLoading(),
    ) { transactionsLoading, categoriesLoading -> transactionsLoading || categoriesLoading }

    private val spaceMembers = MutableStateFlow<List<SpaceMember>>(emptyList())
    private val currentUserId = authRepository.observeAuthState()
        .map { (it as? AuthState.SignedIn)?.userId }

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

    val uiState: StateFlow<HomeUiState> = combine(
        transactionsAndCategories,
        isDataLoading,
        activeSpace,
        spaceMembers,
        currentUserId,
    ) { (transactions, categories), isLoading, space, members, userId ->
        buildUiState(transactions, categories, isLoading, space, members, userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val toastEventsFlow = MutableSharedFlow<TransactionToastEvent>()
    val toastEvents: SharedFlow<TransactionToastEvent> = toastEventsFlow.asSharedFlow()

    fun deleteTransaction(transaction: TransactionUi) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction.id)
            toastEventsFlow.emit(
                TransactionToastEvent(
                    message = "Eliminado",
                    onUndo = {
                        viewModelScope.launch {
                            transactionRepository.addTransaction(
                                type = transaction.type,
                                amount = transaction.amount,
                                categoryId = transaction.categoryId,
                                description = transaction.description,
                                occurredAt = transaction.occurredAt,
                            )
                        }
                    },
                ),
            )
        }
    }

    private fun buildUiState(
        transactions: List<Transaction>,
        categories: List<Category>,
        isLoading: Boolean,
        space: Space?,
        members: List<SpaceMember>,
        userId: String?,
    ): HomeUiState {
        val avatars = members.map { it.toAvatarUi() }
        if (isLoading) return HomeUiState(spaceName = space?.name ?: "", spaceColor = space?.color ?: "#4E8CFF", memberAvatars = avatars, isLoading = true)

        val categoriesById = categories.associateBy { it.id }
        val summary = transactions.toBalanceSummary(categoriesById)

        val isAdmin = space?.role == SpaceRole.ADMIN
        val recent = transactions
            .filter { it.status == TransactionStatus.APPROVED || isAdmin || it.createdBy == userId }
            .sortedWith(compareByDescending<Transaction> { it.occurredAt }.thenByDescending { it.id })
            .take(RECENT_TRANSACTIONS_LIMIT)
            .map { it.toUi(categoriesById[it.categoryId], authorLabel = authorLabelFor(it.createdBy, members, userId)) }

        val pendingBatchCount = transactions
            .toApprovalBatches(categoriesById, members, userId, isAdmin, todayLocalDate())
            .count { it.isPending }

        val cards = buildSummaryCards(transactions, categoriesById, summary.pending, isAdmin, pendingBatchCount, summary.vaultBalance)

        return HomeUiState(
            balanceText = formatAmount(summary.availableBalance),
            incomeText = formatAmount(summary.income),
            expenseText = formatAmount(summary.expense),
            vaultText = formatAmount(summary.vaultBalance),
            summaryCards = cards,
            recentTransactions = recent,
            isLoading = false,
            spaceName = space?.name ?: "",
            spaceColor = space?.color ?: "#4E8CFF",
            memberAvatars = avatars,
            pendingBadgeCount = pendingBatchCount,
        )
    }

    /** Mirrors the source design's summaryCards(): pending, weekly insight, top category, vault. */
    private fun buildSummaryCards(
        transactions: List<Transaction>,
        categoriesById: Map<Long, Category>,
        pendingTotal: Double,
        isAdmin: Boolean,
        pendingBatchCount: Int,
        vaultBalance: Double,
    ): List<SummaryCard> = buildList {
        if (pendingTotal > 0) {
            val body = if (isAdmin) {
                val lotes = if (pendingBatchCount == 1) "lote" else "lotes"
                "${formatAmount(pendingTotal)} por aprobar en $pendingBatchCount $lotes"
            } else {
                "${formatAmount(pendingTotal)} esperando aprobación"
            }
            add(
                SummaryCard(
                    key = "pending",
                    glyph = "!",
                    iconBg = PendingAccent.copy(alpha = 0.16f),
                    iconFg = PendingAccent,
                    body = body,
                    clickable = true,
                ),
            )
        }

        add(
            SummaryCard(
                key = "week",
                glyph = "~",
                iconBg = AccentFixed.copy(alpha = 0.16f),
                iconFg = AccentFixed,
                body = buildWeeklyInsight(transactions),
            ),
        )

        topCategory(transactions, categoriesById)?.let { (name, color, total) ->
            add(
                SummaryCard(
                    key = "top",
                    glyph = name.take(1).uppercase(),
                    iconBg = Color.White.copy(alpha = 0.06f),
                    iconFg = color.toComposeColor(),
                    body = "Mayor gasto del mes: $name, ${formatAmount(total)}",
                ),
            )
        }

        if (vaultBalance > 0) {
            add(
                SummaryCard(
                    key = "vault",
                    glyph = "V",
                    iconBg = AccentFixed.copy(alpha = 0.16f),
                    iconFg = AccentFixed,
                    body = "${formatAmount(vaultBalance)} guardados en el Vault",
                ),
            )
        }
    }

    private fun buildWeeklyInsight(transactions: List<Transaction>): String {
        val today = todayLocalDate()
        val thisWeekStart = today.minus(WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)
        val lastWeekEnd = today.minus(WEEK_LENGTH_DAYS, DateTimeUnit.DAY)
        val lastWeekStart = today.minus(2 * WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)

        fun expenseBetween(from: LocalDate, to: LocalDate): Double = transactions
            .filter { it.type == TransactionType.EXPENSE && it.status == TransactionStatus.APPROVED && it.occurredAt in from..to }
            .sumOf { it.amount }

        val thisWeek = expenseBetween(thisWeekStart, today)
        val lastWeek = expenseBetween(lastWeekStart, lastWeekEnd)

        return when {
            thisWeek <= 0.0 -> "Aún no hay gastos aprobados esta semana"
            lastWeek <= 0.0 -> "Esta es su primera semana con gastos registrados"
            else -> {
                val diffPct = ((thisWeek - lastWeek) / lastWeek * 100).roundToInt()
                when {
                    diffPct > 0 -> "Esta semana gastaron $diffPct% más que la semana anterior"
                    diffPct < 0 -> "Esta semana gastaron ${-diffPct}% menos que la semana anterior"
                    else -> "Gastaron igual que la semana anterior"
                }
            }
        }
    }

    /** All-time (not scoped to the current month, matching the source design's own logic). */
    private fun topCategory(transactions: List<Transaction>, categoriesById: Map<Long, Category>): Triple<String, String, Double>? {
        return transactions
            .asSequence()
            .filter { it.type == TransactionType.EXPENSE && it.status == TransactionStatus.APPROVED }
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, txs) ->
                val category = categoriesById[categoryId] ?: return@mapNotNull null
                Triple(category.name, category.color, txs.sumOf { it.amount })
            }
            .maxByOrNull { it.third }
    }
}
