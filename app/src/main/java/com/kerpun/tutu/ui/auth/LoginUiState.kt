package com.kerpun.tutu.ui.auth

enum class LoginMode { SIGN_IN, SIGN_UP }

data class LoginUiState(
    val mode: LoginMode = LoginMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank() && password.length >= 6
}
