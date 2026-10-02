package com.kerpun.tutu.ui.categories

import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.ui.spaces.SpaceColorPalette

data class CategoryRowUi(
    val id: Long,
    val name: String,
    val color: String,
    val type: TransactionType,
    val usageCount: Int,
)

data class CategoriesUiState(
    val spaceName: String = "",
    val spaceColor: String = "#4E8CFF",
    val rows: List<CategoryRowUi> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val isLoading: Boolean = true,
) {
    val totalCount: Int get() = rows.size
    val visibleRows: List<CategoryRowUi> get() = rows.filter { it.type == selectedType }
}

data class CategoryEditFormState(
    val isOpen: Boolean = false,
    val editingId: Long? = null,
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val color: String = SpaceColorPalette.first(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean get() = editingId != null
    val canSubmit: Boolean get() = !isSubmitting && name.isNotBlank()
}
