package com.kerpun.tutu.ui.common

data class TransactionToastEvent(
    val message: String,
    val onUndo: (() -> Unit)? = null,
)
