package com.kerpun.tutu.data.repository

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    /** True until the first successful load completes; false from then on. */
    fun observeIsLoading(): Flow<Boolean>

    suspend fun addCategory(name: String, type: TransactionType, color: String, icon: String?): Category

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(id: Long)
}
