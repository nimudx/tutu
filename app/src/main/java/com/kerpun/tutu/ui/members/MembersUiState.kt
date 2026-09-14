package com.kerpun.tutu.ui.members

import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole

data class MembersUiState(
    val members: List<SpaceMember> = emptyList(),
    val isLoading: Boolean = true,
    val isCurrentUserAdmin: Boolean = false,
    val currentUserId: String? = null,
    val errorMessage: String? = null,
)

data class InviteFormState(
    val email: String = "",
    val role: SpaceRole = SpaceRole.MEMBER,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank()
}
