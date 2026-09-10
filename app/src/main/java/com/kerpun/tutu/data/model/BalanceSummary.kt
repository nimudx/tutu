package com.kerpun.tutu.data.model

data class BalanceSummary(
    val income: Double,
    val expense: Double,
    val vaultDeposits: Double,
    val vaultWithdrawals: Double,
    /** Sum of movements still awaiting an admin's approval — excluded from every total above. */
    val pending: Double = 0.0,
) {
    val vaultBalance: Double get() = vaultDeposits - vaultWithdrawals

    /** Money moved into the vault is set aside; withdrawing it makes it available again. */
    val availableBalance: Double get() = income - expense - vaultDeposits + vaultWithdrawals
}

/**
 * Only APPROVED movements count toward income/expense/vault — PENDING ones show up separately
 * as [BalanceSummary.pending] until an admin acts on them, and REJECTED ones don't count anywhere.
 */
fun List<Transaction>.toBalanceSummary(categoriesById: Map<Long, Category>): BalanceSummary {
    var income = 0.0
    var expense = 0.0
    var vaultDeposits = 0.0
    var vaultWithdrawals = 0.0
    var pending = 0.0
    for (transaction in this) {
        when (transaction.status) {
            TransactionStatus.PENDING -> {
                pending += transaction.amount
                continue
            }
            TransactionStatus.REJECTED -> continue
            TransactionStatus.APPROVED -> Unit
        }
        when (transaction.type) {
            TransactionType.INCOME -> income += transaction.amount
            TransactionType.EXPENSE -> expense += transaction.amount
            TransactionType.VAULT -> {
                if (categoriesById[transaction.categoryId]?.name == VAULT_WITHDRAWAL_CATEGORY_NAME) {
                    vaultWithdrawals += transaction.amount
                } else {
                    vaultDeposits += transaction.amount
                }
            }
        }
    }
    return BalanceSummary(income, expense, vaultDeposits, vaultWithdrawals, pending)
}
