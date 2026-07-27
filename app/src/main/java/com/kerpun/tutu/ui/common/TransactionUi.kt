package com.kerpun.tutu.ui.common

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import kotlinx.datetime.LocalDate

data class TransactionUi(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long?,
    val description: String?,
    val occurredAt: LocalDate,
    val label: String,
    val dateLabel: String,
    val color: String,
    val initial: String,
    val amountText: String,
    val amountColor: String,
)

private const val INCOME_AMOUNT_COLOR = "#3ECF7A"
private const val EXPENSE_AMOUNT_COLOR = "#FF6B6B"
private const val FALLBACK_CATEGORY_COLOR = "#8A8F98"

fun Transaction.toUi(category: Category?, today: LocalDate = todayLocalDate()): TransactionUi {
    val label = description?.takeIf { it.isNotBlank() } ?: category?.name ?: "Otros"
    val isIncome = type == TransactionType.INCOME
    val sign = if (isIncome) "+ " else "- "
    return TransactionUi(
        id = id,
        type = type,
        amount = amount,
        categoryId = categoryId,
        description = description,
        occurredAt = occurredAt,
        label = label,
        dateLabel = occurredAt.toDisplayLabel(today),
        color = category?.color ?: FALLBACK_CATEGORY_COLOR,
        initial = label.take(1).uppercase(),
        amountText = sign + formatAmount(amount),
        amountColor = if (isIncome) INCOME_AMOUNT_COLOR else EXPENSE_AMOUNT_COLOR,
    )
}
