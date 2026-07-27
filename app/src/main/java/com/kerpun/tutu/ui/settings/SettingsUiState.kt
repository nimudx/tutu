package com.kerpun.tutu.ui.settings

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val categoryCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val currencyLabel: String = "Soles (S/)",
)
