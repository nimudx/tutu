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
import com.kerpun.tutu.ui.common.TransactionToastEvent
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.authorLabelFor
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.groupByDay
import com.kerpun.tutu.ui.common.todayLocalDate
import com.kerpun.tutu.ui.common.toAvatarUi
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

private const val RECENT_TRANSACTIONS_LIMIT = 6
private const val WEEK_LENGTH_DAYS = 7
private const val EXPENSE_TREND_COLOR = 0xFFFF6B6B
private const val INCOME_TREND_COLOR = 0xFF3ECF7A

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

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
        val recentGroups = transactions
            .filter { it.status == TransactionStatus.APPROVED || isAdmin || it.createdBy == userId }
            .sortedWith(compareByDescending<Transaction> { it.occurredAt }.thenByDescending { it.id })
            .take(RECENT_TRANSACTIONS_LIMIT)
            .map { it.toUi(categoriesById[it.categoryId], authorLabel = authorLabelFor(it.createdBy, members, userId)) }
            .groupByDay()

        val pendingBatchCount = transactions
            .toApprovalBatches(categoriesById, members, userId, isAdmin, todayLocalDate())
            .count { it.isPending }

        val trend = buildTrendLine(transactions)
        val totalMoved = summary.income + summary.expense
        val incomeShare = if (totalMoved > 0) (summary.income / totalMoved).toFloat() else 0f
        val expenseShare = if (totalMoved > 0) 1f - incomeShare else 0f
        val today = todayLocalDate()

        return HomeUiState(
            balanceText = formatAmount(summary.availableBalance),
            trendText = trend.first,
            trendColor = trend.second,
            monthLabel = "${monthNames[today.monthNumber - 1]} · este mes",
            incomeShare = incomeShare,
            expenseShare = expenseShare,
            incomeText = formatAmount(summary.income),
            expenseText = formatAmount(summary.expense),
            vaultText = formatAmount(summary.vaultBalance),
            hasVault = summary.vaultBalance > 0,
            recentGroups = recentGroups,
            isLoading = false,
            spaceName = space?.name ?: "",
            spaceColor = space?.color ?: "#4E8CFF",
            memberAvatars = avatars,
            pendingBadgeCount = pendingBatchCount,
        )
    }

    /** This week's approved expenses vs. last week's, mirroring the source design's trendLine(). */
    private fun buildTrendLine(transactions: List<Transaction>): Pair<String, Color?> {
        val today = todayLocalDate()
        val thisWeekStart = today.minus(WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)
        val lastWeekEnd = today.minus(WEEK_LENGTH_DAYS, DateTimeUnit.DAY)
        val lastWeekStart = today.minus(2 * WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)

        fun expenseBetween(from: LocalDate, to: LocalDate): Double = transactions
            .filter { it.type == TransactionType.EXPENSE && it.status == TransactionStatus.APPROVED && it.occurredAt in from..to }
            .sumOf { it.amount }

        val thisWeek = expenseBetween(thisWeekStart, today)
        val lastWeek = expenseBetween(lastWeekStart, lastWeekEnd)

        if (thisWeek <= 0.0) return "Sin gastos esta semana" to null
        val base = "${formatAmount(thisWeek)} esta semana"
        if (lastWeek <= 0.0) return base to null
        val diffPct = ((thisWeek - lastWeek) / lastWeek * 100).roundToInt()
        return when {
            diffPct == 0 -> "$base · igual que la anterior" to null
            diffPct > 0 -> "$base · +$diffPct% vs. la anterior" to Color(EXPENSE_TREND_COLOR)
            else -> "$base · $diffPct% vs. la anterior" to Color(INCOME_TREND_COLOR)
        }
    }
}
