package com.kerpun.tutu.ui.movements

import com.kerpun.tutu.ui.common.TransactionGroup

enum class MovementsFilter {
    ALL,
    INCOME,
    EXPENSE,
    VAULT,
}

data class MovementsUiState(
    val filter: MovementsFilter = MovementsFilter.ALL,
    val groups: List<TransactionGroup> = emptyList(),
    val isLoading: Boolean = true,
)
