package com.kerpun.tutu.data

import com.kerpun.tutu.data.remote.SupabaseClientProvider
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseCategoryRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseTransactionRepository
import io.github.jan.supabase.postgrest.postgrest

/**
 * Single source of the app's repositories, backed by Supabase.
 */
object AppContainer {
    private val postgrest = SupabaseClientProvider.client.postgrest

    val categoryRepository: CategoryRepository = SupabaseCategoryRepository(postgrest)
    val transactionRepository: TransactionRepository = SupabaseTransactionRepository(postgrest)
}
