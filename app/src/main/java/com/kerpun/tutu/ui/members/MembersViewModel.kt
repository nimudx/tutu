package com.kerpun.tutu.ui.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.AuthState
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.data.repository.AuthRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MembersViewModel(
    private val spaceRepository: SpaceRepository,
    private val authRepository: AuthRepository,
    private val activeSpaceId: MutableStateFlow<String?>,
) : ViewModel() {

    private var currentSpaceId: String? = null

    private val members = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = members.asStateFlow()

    private val _inviteForm = MutableStateFlow(InviteFormState())
    val inviteForm: StateFlow<InviteFormState> = _inviteForm.asStateFlow()

    init {
        viewModelScope.launch {
            combine(activeSpaceId, spaceRepository.observeSpaces(), authRepository.observeAuthState()) { spaceId, spaces, session ->
                Triple(spaceId, spaces.find { it.id == spaceId }?.role, (session as? AuthState.SignedIn)?.userId)
            }.collect { (spaceId, myRole, myUserId) ->
                members.update { it.copy(isCurrentUserAdmin = myRole == SpaceRole.ADMIN, currentUserId = myUserId) }
                if (spaceId != null) {
                    refresh(spaceId)
                } else {
                    currentSpaceId = null
                    members.update { it.copy(members = emptyList(), isLoading = false) }
                }
            }
        }
    }

    fun refresh() {
        currentSpaceId?.let { spaceId -> viewModelScope.launch { refresh(spaceId) } }
    }

    private suspend fun refresh(spaceId: String) {
        currentSpaceId = spaceId
        members.update { it.copy(isLoading = true) }
        runCatching { spaceRepository.listMembers(spaceId) }
            .onSuccess { list -> members.update { it.copy(members = list, isLoading = false, errorMessage = null) } }
            .onFailure { error -> members.update { it.copy(isLoading = false, errorMessage = error.message) } }
    }

    fun setInviteEmail(value: String) {
        _inviteForm.update { it.copy(email = value, errorMessage = null) }
    }

    fun toggleInviteRole() {
        _inviteForm.update { it.copy(role = if (it.role == SpaceRole.ADMIN) SpaceRole.MEMBER else SpaceRole.ADMIN) }
    }

    fun invite() {
        val spaceId = currentSpaceId ?: return
        val form = _inviteForm.value
        if (!form.canSubmit) return

        viewModelScope.launch {
            _inviteForm.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching { spaceRepository.inviteMember(spaceId, form.email.trim(), form.role) }
                .onSuccess {
                    _inviteForm.value = InviteFormState()
                    refresh(spaceId)
                }
                .onFailure { error -> _inviteForm.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Ocurrió un error") } }
        }
    }

    fun setRole(userId: String, role: SpaceRole) {
        val spaceId = currentSpaceId ?: return
        viewModelScope.launch {
            runCatching { spaceRepository.setMemberRole(spaceId, userId, role) }
                .onSuccess { refresh(spaceId) }
                .onFailure { error -> members.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun removeMember(userId: String) {
        val spaceId = currentSpaceId ?: return
        viewModelScope.launch {
            runCatching { spaceRepository.removeMember(spaceId, userId) }
                .onSuccess { refresh(spaceId) }
                .onFailure { error -> members.update { it.copy(errorMessage = error.message) } }
        }
    }

    /** Leaving the active space clears it, so the app routes back to space selection. */
    fun leaveSpace() {
        val spaceId = currentSpaceId ?: return
        viewModelScope.launch {
            val myUserId = (authRepository.observeAuthState().first() as? AuthState.SignedIn)?.userId ?: return@launch
            runCatching { spaceRepository.removeMember(spaceId, myUserId) }
                .onSuccess { activeSpaceId.value = null }
                .onFailure { error -> members.update { it.copy(errorMessage = error.message) } }
        }
    }
}
