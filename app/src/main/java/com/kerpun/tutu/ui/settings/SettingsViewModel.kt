package com.kerpun.tutu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val isDarkTheme = MutableStateFlow(true)
    private val notificationsEnabled = MutableStateFlow(true)

    val uiState: StateFlow<SettingsUiState> = combine(
        isDarkTheme,
        notificationsEnabled,
        categoryRepository.observeCategories(),
    ) { dark, notifications, categories ->
        SettingsUiState(
            isDarkTheme = dark,
            categoryCount = categories.size,
            notificationsEnabled = notifications,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setDarkTheme(enabled: Boolean) {
        isDarkTheme.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
    }
}
