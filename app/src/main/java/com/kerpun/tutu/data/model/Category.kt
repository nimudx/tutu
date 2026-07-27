package com.kerpun.tutu.data.model

data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
    val color: String,
    val icon: String?,
    val isDefault: Boolean = false,
)
