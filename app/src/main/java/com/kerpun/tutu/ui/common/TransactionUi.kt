package com.kerpun.tutu.ui.common

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionStatus
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.model.VAULT_WITHDRAWAL_CATEGORY_NAME
import kotlinx.datetime.LocalDate

data class TransactionUi(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val description: String?,
    val occurredAt: LocalDate,
    val label: String,
    val dateLabel: String,
    val color: String,
    val initial: String,
    val amountText: String,
    val amountColor: String,
    val authorLabel: String?,
    val status: TransactionStatus,
)

private const val INCOME_AMOUNT_COLOR = "#3ECF7A"
private const val EXPENSE_AMOUNT_COLOR = "#FF6B6B"
private const val VAULT_AMOUNT_COLOR = "#4E8CFF"
private const val FALLBACK_CATEGORY_COLOR = "#8A8F98"

/** Author + a non-approved status suffix, joined by " · " — null when there's nothing to show (solo space, approved). */
fun TransactionUi.subLabelText(): String? {
    val parts = buildList {
        authorLabel?.let { add(it) }
        when (status) {
            TransactionStatus.PENDING -> add("Pendiente")
            TransactionStatus.REJECTED -> add("Rechazado")
            TransactionStatus.APPROVED -> Unit
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

data class TransactionGroup(val label: String, val transactions: List<TransactionUi>)

/** Groups already-sorted transactions by their day label, preserving order (e.g. HOY, AYER, 12 sep…). */
fun List<TransactionUi>.groupByDay(): List<TransactionGroup> {
    val groups = LinkedHashMap<String, MutableList<TransactionUi>>()
    for (transaction in this) {
        groups.getOrPut(transaction.dateLabel) { mutableListOf() }.add(transaction)
    }
    return groups.map { (label, transactions) -> TransactionGroup(label, transactions) }
}

fun Transaction.toUi(category: Category?, today: LocalDate = todayLocalDate(), authorLabel: String? = null): TransactionUi {
    val label = description?.takeIf { it.isNotBlank() } ?: category?.name ?: "Otros"
    val (sign, amountColor) = when (type) {
        TransactionType.INCOME -> "+ " to INCOME_AMOUNT_COLOR
        TransactionType.EXPENSE -> "- " to EXPENSE_AMOUNT_COLOR
        TransactionType.VAULT -> {
            val isWithdrawal = category?.name == VAULT_WITHDRAWAL_CATEGORY_NAME
            (if (isWithdrawal) "← " else "→ ") to VAULT_AMOUNT_COLOR
        }
    }
    return TransactionUi(
        id = id,
        type = type,
        amount = amount,
        categoryId = categoryId,
        categoryName = category?.name ?: "Otros",
        description = description,
        occurredAt = occurredAt,
        label = label,
        dateLabel = occurredAt.toDisplayLabel(today),
        color = category?.color ?: FALLBACK_CATEGORY_COLOR,
        initial = label.take(1).uppercase(),
        amountText = sign + formatAmount(amount),
        amountColor = amountColor,
        authorLabel = authorLabel,
        status = status,
    )
}
