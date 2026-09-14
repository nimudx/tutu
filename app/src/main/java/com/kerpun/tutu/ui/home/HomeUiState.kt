package com.kerpun.tutu.ui.home

import com.kerpun.tutu.ui.common.MemberAvatarUi
import com.kerpun.tutu.ui.common.TransactionUi

data class HomeUiState(
    val balanceText: String = "S/ 0.00",
    val incomeText: String = "S/ 0.00",
    val expenseText: String = "S/ 0.00",
    val vaultText: String = "S/ 0.00",
    val summaryCards: List<SummaryCard> = emptyList(),
    val recentTransactions: List<TransactionUi> = emptyList(),
    val isLoading: Boolean = true,
    val spaceName: String = "",
    val spaceColor: String = "#4E8CFF",
    val memberAvatars: List<MemberAvatarUi> = emptyList(),
    val pendingBadgeCount: Int = 0,
)
