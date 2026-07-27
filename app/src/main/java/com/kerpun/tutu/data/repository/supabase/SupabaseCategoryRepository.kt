package com.kerpun.tutu.data.repository.supabase

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.remote.dto.CategoryInsert
import com.kerpun.tutu.data.remote.dto.CategoryRow
import com.kerpun.tutu.data.remote.dto.CategoryUpdate
import com.kerpun.tutu.data.repository.CategoryRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TABLE = "categories"

class SupabaseCategoryRepository(private val postgrest: Postgrest) : CategoryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val categories = MutableStateFlow<List<Category>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    init {
        scope.launch { refresh() }
    }

    override fun observeCategories(): Flow<List<Category>> = categories.asStateFlow()

    override fun observeIsLoading(): Flow<Boolean> = isLoading.asStateFlow()

    override suspend fun addCategory(
        name: String,
        type: TransactionType,
        color: String,
        icon: String?,
    ): Category {
        val row = postgrest.from(TABLE)
            .insert(CategoryInsert(name = name, type = type.toDb(), color = color, icon = icon)) {
                select()
            }
            .decodeSingle<CategoryRow>()
        refresh()
        return row.toDomain()
    }

    override suspend fun updateCategory(category: Category) {
        postgrest.from(TABLE)
            .update(
                CategoryUpdate(
                    name = category.name,
                    type = category.type.toDb(),
                    color = category.color,
                    icon = category.icon,
                ),
            ) {
                filter { eq("id", category.id) }
            }
        refresh()
    }

    override suspend fun deleteCategory(id: Long) {
        postgrest.from(TABLE).delete { filter { eq("id", id) } }
        refresh()
    }

    private suspend fun refresh() {
        val rows = postgrest.from(TABLE)
            .select { order(column = "id", order = Order.ASCENDING) }
            .decodeList<CategoryRow>()
        categories.value = rows.map { it.toDomain() }
        isLoading.value = false
    }
}
