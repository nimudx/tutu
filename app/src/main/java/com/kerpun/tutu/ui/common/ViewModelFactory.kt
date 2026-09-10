package com.kerpun.tutu.ui.common

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kerpun.tutu.data.AppContainer
import com.kerpun.tutu.ui.addtransaction.AddTransactionViewModel
import com.kerpun.tutu.ui.approvals.ApprovalsViewModel
import com.kerpun.tutu.ui.auth.AuthViewModel
import com.kerpun.tutu.ui.home.HomeViewModel
import com.kerpun.tutu.ui.members.MembersViewModel
import com.kerpun.tutu.ui.movements.MovementsViewModel
import com.kerpun.tutu.ui.settings.SettingsViewModel
import com.kerpun.tutu.ui.spaces.SpacesViewModel

val TutuViewModelFactory = viewModelFactory {
    initializer { HomeViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository, AppContainer.spaceRepository, AppContainer.authRepository, AppContainer.activeSpaceId) }
    initializer { MovementsViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository, AppContainer.spaceRepository, AppContainer.authRepository, AppContainer.activeSpaceId) }
    initializer { AddTransactionViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository, AppContainer.authRepository, AppContainer.activeSpaceId) }
    initializer {
        SettingsViewModel(
            AppContainer.categoryRepository,
            AppContainer.spaceRepository,
            AppContainer.transactionRepository,
            AppContainer.authRepository,
            AppContainer.activeSpaceId,
        )
    }
    initializer { AuthViewModel(AppContainer.authRepository) }
    initializer { SpacesViewModel(AppContainer.spaceRepository, AppContainer.activeSpaceId) }
    initializer { MembersViewModel(AppContainer.spaceRepository, AppContainer.authRepository, AppContainer.activeSpaceId) }
    initializer {
        ApprovalsViewModel(
            AppContainer.transactionRepository,
            AppContainer.categoryRepository,
            AppContainer.spaceRepository,
            AppContainer.authRepository,
            AppContainer.activeSpaceId,
        )
    }
}
