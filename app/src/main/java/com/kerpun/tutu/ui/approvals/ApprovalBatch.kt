package com.kerpun.tutu.ui.approvals

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.model.VAULT_WITHDRAWAL_CATEGORY_NAME
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.toDisplayLabel
import com.kerpun.tutu.ui.spaces.colorForMemberId
import kotlinx.datetime.LocalDate

data class ApprovalBatchItem(
    val label: String,
    val amountText: String,
    val amountColor: String,
)

data class ApprovalBatch(
    val key: String,
    val ids: List<Long>,
    val memberLabel: String,
    val initial: String,
    val color: String,
    val metaText: String,
    val totalText: String,
    val totalColor: String,
    val items: List<ApprovalBatchItem>,
    /** True when the viewer is an admin and this batch is still pending. */
    val canApprove: Boolean,
    val statusNote: String?,
    val statusColor: String?,
    val isPending: Boolean,
)

private const val INCOME_COLOR = "#3ECF7A"
private const val EXPENSE_COLOR = "#FF6B6B"

/**
 * Groups movements the viewer isn't free to just see-and-forget into "batches" — one per
 * (author, day, status) — the same shape as an admin's approval queue or a member's own
 * pending/rejected history. Only PENDING movements are ever included; REJECTED ones are
 * included too, but only for the member who created them (nothing left for an admin to act
 * on there, it's just their own history).
 */
fun List<Transaction>.toApprovalBatches(
    categoriesById: Map<Long, Category>,
    members: List<SpaceMember>,
    currentUserId: String?,
    isAdmin: Boolean,
    today: LocalDate,
): List<ApprovalBatch> {
    val pool = this.filter { tx ->
        val ownedByViewer = tx.createdBy == currentUserId
        when (tx.status) {
            TransactionStatus.PENDING -> isAdmin || ownedByViewer
            TransactionStatus.REJECTED -> !isAdmin && ownedByViewer
            TransactionStatus.APPROVED -> false
        }
    }
    return pool
        .groupBy { Triple(it.createdBy, it.occurredAt, it.status) }
        .entries
        .sortedByDescending { it.key.second }
        .map { (key, items) ->
            val (authorId, at, status) = key
            val member = members.find { it.userId == authorId }
            val memberLabel = when {
                authorId == currentUserId -> "Vos"
                member != null -> member.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                else -> "—"
            }
            val net = items.sumOf { signedAmount(it, categoriesById) }
            val canApprove = isAdmin && status == TransactionStatus.PENDING
            val statusNote = if (canApprove) {
                null
            } else if (status == TransactionStatus.PENDING) {
                "Esperando aprobación"
            } else {
                "Rechazado por el admin"
            }
            val statusColor = if (status == TransactionStatus.REJECTED) EXPENSE_COLOR else null
            ApprovalBatch(
                key = "$authorId|$at|$status",
                ids = items.map { it.id },
                memberLabel = memberLabel,
                initial = memberLabel.take(1).uppercase(),
                color = authorId?.let { colorForMemberId(it) } ?: "#8A8F98",
                metaText = at.toDisplayLabel(today) + " · " + items.size + if (items.size == 1) " movimiento" else " movimientos",
                totalText = (if (net < 0) "- " else "+ ") + formatAmount(kotlin.math.abs(net)),
                totalColor = if (net < 0) EXPENSE_COLOR else INCOME_COLOR,
                items = items.map {
                    ApprovalBatchItem(
                        label = it.description?.takeIf { d -> d.isNotBlank() } ?: categoriesById[it.categoryId]?.name ?: "Otros",
                        amountText = formatAmount(it.amount),
                        amountColor = categoriesById[it.categoryId]?.color ?: "#8A8F98",
                    )
                },
                canApprove = canApprove,
                statusNote = statusNote,
                statusColor = statusColor,
                isPending = status == TransactionStatus.PENDING,
            )
        }
}

private fun signedAmount(transaction: Transaction, categoriesById: Map<Long, Category>): Double = when (transaction.type) {
    TransactionType.INCOME -> transaction.amount
    TransactionType.EXPENSE -> -transaction.amount
    TransactionType.VAULT -> {
        val isWithdrawal = categoriesById[transaction.categoryId]?.name == VAULT_WITHDRAWAL_CATEGORY_NAME
        if (isWithdrawal) transaction.amount else -transaction.amount
    }
}
