package com.kerpun.tutu.ui.movements

import com.kerpun.tutu.ui.common.TransactionUi

enum class MovementsFilter {
    ALL,
    INCOME,
    EXPENSE,
    VAULT,
}

data class MovementsUiState(
    val filter: MovementsFilter = MovementsFilter.ALL,
    val transactions: List<TransactionUi> = emptyList(),
    val isLoading: Boolean = true,
)
