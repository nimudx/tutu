package com.kerpun.tutu.data.model

data class BalanceSummary(
    val income: Double,
    val expense: Double,
    val vaultDeposits: Double,
    val vaultWithdrawals: Double,
) {
    val vaultBalance: Double get() = vaultDeposits - vaultWithdrawals

    /** Money moved into the vault is set aside; withdrawing it makes it available again. */
    val availableBalance: Double get() = income - expense - vaultDeposits + vaultWithdrawals
}

fun List<Transaction>.toBalanceSummary(categoriesById: Map<Long, Category>): BalanceSummary {
    var income = 0.0
    var expense = 0.0
    var vaultDeposits = 0.0
    var vaultWithdrawals = 0.0
    for (transaction in this) {
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
    return BalanceSummary(income, expense, vaultDeposits, vaultWithdrawals)
}
