package com.kerpun.tutu.ui.approvals

data class ApprovalsUiState(
    val spaceName: String = "",
    val spaceColor: String = "#4E8CFF",
    val subtitle: String = "",
    val batches: List<ApprovalBatch> = emptyList(),
    val emptyText: String? = null,
    val isLoading: Boolean = true,
)
