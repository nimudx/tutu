package com.kerpun.tutu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.todayLocalDate
import com.kerpun.tutu.ui.common.toUi
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
    ) { transactions, categories ->
        buildUiState(transactions, categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { transactionRepository.deleteTransaction(id) }
    }

    private fun buildUiState(transactions: List<Transaction>, categories: List<Category>): HomeUiState {
        val categoriesById = categories.associateBy { it.id }
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val recent = transactions
            .sortedWith(compareByDescending<Transaction> { it.occurredAt }.thenByDescending { it.id })
            .take(RECENT_TRANSACTIONS_LIMIT)
            .map { it.toUi(categoriesById[it.categoryId]) }

        return HomeUiState(
            balanceText = formatAmount(income - expense),
            incomeText = formatAmount(income),
            expenseText = formatAmount(expense),
            insightText = buildWeeklyInsight(transactions),
            recentTransactions = recent,
            isLoading = false,
        )
    }

    private fun buildWeeklyInsight(transactions: List<Transaction>): String {
        val today = todayLocalDate()
        val thisWeekStart = today.minus(WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)
        val lastWeekEnd = today.minus(WEEK_LENGTH_DAYS, DateTimeUnit.DAY)
        val lastWeekStart = today.minus(2 * WEEK_LENGTH_DAYS - 1, DateTimeUnit.DAY)

        fun expenseBetween(from: LocalDate, to: LocalDate): Double = transactions
            .filter { it.type == TransactionType.EXPENSE && it.occurredAt in from..to }
            .sumOf { it.amount }

        val thisWeek = expenseBetween(thisWeekStart, today)
        val lastWeek = expenseBetween(lastWeekStart, lastWeekEnd)

        return when {
            thisWeek <= 0.0 -> "Aún no tienes gastos registrados esta semana"
            lastWeek <= 0.0 -> "Empezaste a registrar gastos esta semana"
            else -> {
                val diffPct = ((thisWeek - lastWeek) / lastWeek * 100).roundToInt()
                when {
                    diffPct > 0 -> "Esta semana gastaste $diffPct% más que la semana anterior"
                    diffPct < 0 -> "Esta semana gastaste ${-diffPct}% menos que la semana anterior"
                    else -> "Gastaste igual que la semana anterior"
                }
            }
        }
    }
}
