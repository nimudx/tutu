package com.kerpun.tutu.data.model

data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
    val color: String,
    val icon: String?,
    val isDefault: Boolean = false,
)

/** Default VAULT category names: depositing locks money away, withdrawing returns it to the available balance. */
const val VAULT_DEPOSIT_CATEGORY_NAME = "Agregar"
const val VAULT_WITHDRAWAL_CATEGORY_NAME = "Retirar"
