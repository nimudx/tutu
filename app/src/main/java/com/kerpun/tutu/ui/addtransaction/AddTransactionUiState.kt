package com.kerpun.tutu.ui.addtransaction

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val description: String = "",
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val editingId: Long? = null,
) {
    val isEditing: Boolean get() = editingId != null
}
