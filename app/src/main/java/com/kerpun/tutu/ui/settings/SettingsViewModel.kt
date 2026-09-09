package com.kerpun.tutu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    categoryRepository: CategoryRepository,
    spaceRepository: SpaceRepository,
    activeSpaceId: StateFlow<String?>,
) : ViewModel() {

    private val isDarkTheme = MutableStateFlow(true)
    private val notificationsEnabled = MutableStateFlow(true)

    private val activeSpace = combine(spaceRepository.observeSpaces(), activeSpaceId) { spaces, id ->
        spaces.find { it.id == id }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        isDarkTheme,
        notificationsEnabled,
        categoryRepository.observeCategories(),
        activeSpace,
    ) { dark, notifications, categories, space ->
        SettingsUiState(
            isDarkTheme = dark,
            categoryCount = categories.size,
            notificationsEnabled = notifications,
            spaceName = space?.name ?: "",
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
