package com.kerpun.tutu.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Tapping a row opens [detail] (a read-only sheet); long-pressing opens [menu] (a quick
 * Editar/Eliminar sheet). Either delete action first routes through [confirmDelete].
 */
class TransactionInteractionsState {
    var detail by mutableStateOf<TransactionUi?>(null)
        private set
    var menu by mutableStateOf<TransactionUi?>(null)
        private set
    var confirmDelete by mutableStateOf<TransactionUi?>(null)
        private set

    fun openDetail(transaction: TransactionUi) {
        detail = transaction
    }

    fun openMenu(transaction: TransactionUi) {
        menu = transaction
    }

    fun requestDelete(transaction: TransactionUi) {
        detail = null
        menu = null
        confirmDelete = transaction
    }

    fun dismissAll() {
        detail = null
        menu = null
        confirmDelete = null
    }
}

@Composable
fun rememberTransactionInteractions(): TransactionInteractionsState = remember { TransactionInteractionsState() }

/** Keeps the last non-null value around so an exit animation has something to render. */
@Composable
private fun <T> rememberLatestNonNull(value: T?): T? {
    var latest by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        if (value != null) latest = value
    }
    return latest
}

@Composable
fun TransactionInteractionsOverlay(
    state: TransactionInteractionsState,
    onEdit: (TransactionUi) -> Unit,
    onDelete: (TransactionUi) -> Unit,
) {
    rememberLatestNonNull(state.detail)?.let { transaction ->
        TransactionDetailSheet(
            transaction = transaction,
            visible = state.detail != null,
            onDismiss = { state.dismissAll() },
            onEdit = {
                state.dismissAll()
                onEdit(transaction)
            },
            onDelete = { state.requestDelete(transaction) },
        )
    }
    rememberLatestNonNull(state.menu)?.let { transaction ->
        TransactionMenuSheet(
            transaction = transaction,
            visible = state.menu != null,
            onDismiss = { state.dismissAll() },
            onEdit = {
                state.dismissAll()
                onEdit(transaction)
            },
            onDelete = { state.requestDelete(transaction) },
        )
    }
    rememberLatestNonNull(state.confirmDelete)?.let { transaction ->
        ConfirmDeleteDialog(
            title = "¿Eliminar este movimiento?",
            subtitle = "${transaction.label} · ${transaction.amountText}",
            visible = state.confirmDelete != null,
            onDismiss = { state.dismissAll() },
            onConfirm = {
                state.dismissAll()
                onDelete(transaction)
            },
        )
    }
}
