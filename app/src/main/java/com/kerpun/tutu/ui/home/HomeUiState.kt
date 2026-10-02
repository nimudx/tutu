package com.kerpun.tutu.ui.home

import androidx.compose.ui.graphics.Color
import com.kerpun.tutu.ui.common.MemberAvatarUi
import com.kerpun.tutu.ui.common.TransactionGroup

data class HomeUiState(
    val balanceText: String = "S/ 0.00",
    val trendText: String = "",
    /** Null means "use the default secondary text color" — no green/red vs. last week to show. */
    val trendColor: Color? = null,
    val monthLabel: String = "",
    val incomeShare: Float = 0f,
    val expenseShare: Float = 0f,
    val incomeText: String = "S/ 0.00",
    val expenseText: String = "S/ 0.00",
    val vaultText: String = "S/ 0.00",
    val hasVault: Boolean = false,
    val recentGroups: List<TransactionGroup> = emptyList(),
    val isLoading: Boolean = true,
    val spaceName: String = "",
    val spaceColor: String = "#4E8CFF",
    val memberAvatars: List<MemberAvatarUi> = emptyList(),
    val pendingBadgeCount: Int = 0,
)
