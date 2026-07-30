package com.kerpun.tutu.ui.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.common.TransactionToastEvent
import com.kerpun.tutu.ui.common.TransactionUi
import com.kerpun.tutu.ui.common.toUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovementsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(MovementsFilter.ALL)

    val uiState: StateFlow<MovementsUiState> = combine(
        transactionRepository.observeTransactions(),
        categoryRepository.observeCategories(),
        filter,
        transactionRepository.observeIsLoading(),
        categoryRepository.observeIsLoading(),
    ) { transactions, categories, currentFilter, transactionsLoading, categoriesLoading ->
        val isLoading = transactionsLoading || categoriesLoading
        if (isLoading) {
            MovementsUiState(filter = currentFilter, isLoading = true)
        } else {
            val categoriesById = categories.associateBy { it.id }
            val filtered = transactions
                .filter { matchesFilter(it, currentFilter) }
                .sortedWith(compareByDescending<Transaction> { it.occurredAt }.thenByDescending { it.id })
                .map { it.toUi(categoriesById[it.categoryId]) }
            MovementsUiState(filter = currentFilter, transactions = filtered, isLoading = false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovementsUiState(),
    )

    fun setFilter(newFilter: MovementsFilter) {
        filter.value = newFilter
    }

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

    private fun matchesFilter(transaction: Transaction, currentFilter: MovementsFilter): Boolean =
        when (currentFilter) {
            MovementsFilter.ALL -> true
            MovementsFilter.INCOME -> transaction.type == TransactionType.INCOME
            MovementsFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE
            MovementsFilter.VAULT -> transaction.type == TransactionType.VAULT
        }
}
