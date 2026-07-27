package com.kerpun.tutu.ui.home

import com.kerpun.tutu.ui.common.TransactionUi

data class HomeUiState(
    val balanceText: String = "S/ 0.00",
    val incomeText: String = "S/ 0.00",
    val expenseText: String = "S/ 0.00",
    val insightText: String = "",
    val recentTransactions: List<TransactionUi> = emptyList(),
    val isLoading: Boolean = true,
)
