package com.kerpun.tutu.data.repository.fake

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.CategoryRepository
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCategoryRepository : CategoryRepository {

    private val nextId = AtomicLong(seedCategories.size + 1L)
    private val categories = MutableStateFlow(seedCategories)
    private val isLoading = MutableStateFlow(false)

    override fun observeCategories(): Flow<List<Category>> = categories.asStateFlow()

    override fun observeIsLoading(): Flow<Boolean> = isLoading.asStateFlow()

    override suspend fun addCategory(
        name: String,
        type: TransactionType,
        color: String,
        icon: String?,
    ): Category {
        val category = Category(
            id = nextId.getAndIncrement(),
            name = name,
            type = type,
            color = color,
            icon = icon,
        )
        categories.update { it + category }
        return category
    }

    override suspend fun updateCategory(category: Category) {
        categories.update { list -> list.map { if (it.id == category.id) category else it } }
    }

    override suspend fun deleteCategory(id: Long) {
        categories.update { list -> list.filterNot { it.id == id } }
    }

    companion object {
        // Mismos ids que asume FakeTransactionRepository para categoryId.
        private val seedCategories = listOf(
            Category(1, "Comida", TransactionType.EXPENSE, "#F0A048", "🍔", isDefault = true),
            Category(2, "Transporte", TransactionType.EXPENSE, "#4F8FE0", "🚗", isDefault = true),
            Category(3, "Hogar", TransactionType.EXPENSE, "#A984E8", "🏠", isDefault = true),
            Category(4, "Salud", TransactionType.EXPENSE, "#E86D8A", "💊", isDefault = true),
            Category(5, "Ocio", TransactionType.EXPENSE, "#45C0B0", "🎬", isDefault = true),
            Category(6, "Otros", TransactionType.EXPENSE, "#8A8F98", "📦", isDefault = true),
            Category(7, "Salario", TransactionType.INCOME, "#3ECF7A", "💼", isDefault = true),
            Category(8, "Freelance", TransactionType.INCOME, "#3EC7CF", "💻", isDefault = true),
            Category(9, "Ventas", TransactionType.INCOME, "#C7CF3E", "🛍️", isDefault = true),
            Category(10, "Otros", TransactionType.INCOME, "#8A8F98", "💰", isDefault = true),
            Category(11, "Agregar", TransactionType.VAULT, "#4E8CFF", "🔒", isDefault = true),
            Category(12, "Retirar", TransactionType.VAULT, "#4E8CFF", "🔓", isDefault = true),
        )
    }
}
