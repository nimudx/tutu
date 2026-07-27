package com.kerpun.tutu.ui.common

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kerpun.tutu.data.AppContainer
import com.kerpun.tutu.ui.addtransaction.AddTransactionViewModel
import com.kerpun.tutu.ui.home.HomeViewModel
import com.kerpun.tutu.ui.movements.MovementsViewModel
import com.kerpun.tutu.ui.settings.SettingsViewModel

val TutuViewModelFactory = viewModelFactory {
    initializer { HomeViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository) }
    initializer { MovementsViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository) }
    initializer { AddTransactionViewModel(AppContainer.transactionRepository, AppContainer.categoryRepository) }
    initializer { SettingsViewModel(AppContainer.categoryRepository) }
}
