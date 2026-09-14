package com.kerpun.tutu.data

import com.kerpun.tutu.data.remote.SupabaseClientProvider
import com.kerpun.tutu.data.repository.AuthRepository
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseAuthRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseCategoryRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseSpaceRepository
import com.kerpun.tutu.data.repository.supabase.SupabaseTransactionRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of the app's repositories, backed by Supabase.
 */
object AppContainer {
    private val containerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val postgrest = SupabaseClientProvider.client.postgrest
    private val auth = SupabaseClientProvider.client.auth

    /** Space the user is currently working in. Null until one is created/selected, or on sign-out. */
    val activeSpaceId = MutableStateFlow<String?>(null)

    val categoryRepository: CategoryRepository = SupabaseCategoryRepository(postgrest)
    val transactionRepository: TransactionRepository = SupabaseTransactionRepository(postgrest, auth, activeSpaceId)
    val authRepository: AuthRepository = SupabaseAuthRepository(auth)
    val spaceRepository: SpaceRepository = SupabaseSpaceRepository(postgrest, auth)

    init {
        containerScope.launch {
            auth.sessionStatus.collect { status ->
                if (status is SessionStatus.NotAuthenticated) activeSpaceId.value = null
            }
        }
    }
}
