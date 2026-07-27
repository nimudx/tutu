package com.kerpun.tutu.data

import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.data.repository.fake.FakeCategoryRepository
import com.kerpun.tutu.data.repository.fake.FakeTransactionRepository

/**
 * Single source of the app's repositories. Swap the Fake implementations for
 * Supabase-backed ones here once the real backend is wired up — nothing above
 * this layer (ViewModels, UI) needs to change.
 */
object AppContainer {
    val categoryRepository: CategoryRepository = FakeCategoryRepository()
    val transactionRepository: TransactionRepository = FakeTransactionRepository()
}
