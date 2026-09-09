package com.kerpun.tutu.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.repository.SpaceRepository
import com.kerpun.tutu.ui.common.MemberAvatarUi
import com.kerpun.tutu.ui.common.toAvatarUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpacesViewModel(
    private val spaceRepository: SpaceRepository,
    private val activeSpaceId: MutableStateFlow<String?>,
) : ViewModel() {

    private val memberAvatarsBySpaceId = MutableStateFlow<Map<String, List<MemberAvatarUi>>>(emptyMap())

    val uiState: StateFlow<SpacesUiState> = combine(
        spaceRepository.observeSpaces(),
        spaceRepository.observeIsLoading(),
        activeSpaceId,
        memberAvatarsBySpaceId,
    ) { spaces, isLoading, activeId, avatars ->
        SpacesUiState(spaces = spaces, isLoading = isLoading, activeSpaceId = activeId, memberAvatarsBySpaceId = avatars)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpacesUiState(),
    )

    private val _formState = MutableStateFlow(CreateSpaceFormState())
    val formState: StateFlow<CreateSpaceFormState> = _formState.asStateFlow()

    init {
        // Once the user's spaces load, default to the first one if nothing is active yet
        // (e.g. right after login, or after creating the very first space). Also fetches
        // each space's member avatars, shown on its row in the list.
        viewModelScope.launch {
            spaceRepository.observeSpaces().collect { spaces ->
                if (activeSpaceId.value == null && spaces.isNotEmpty()) {
                    activeSpaceId.value = spaces.first().id
                }
                memberAvatarsBySpaceId.value = spaces.associate { space ->
                    val avatars = runCatching { spaceRepository.listMembers(space.id) }
                        .getOrElse { emptyList() }
                        .map { it.toAvatarUi() }
                    space.id to avatars
                }
            }
        }
    }

    fun setNewSpaceName(name: String) {
        _formState.update { it.copy(name = name, errorMessage = null) }
    }

    fun setNewSpaceColor(color: String) {
        _formState.update { it.copy(color = color) }
    }

    fun selectSpace(id: String) {
        activeSpaceId.value = id
    }

    fun createSpace() {
        val form = _formState.value
        if (!form.canSubmit) return

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching { spaceRepository.createSpace(form.name.trim(), form.color) }
                .onSuccess { space ->
                    activeSpaceId.value = space.id
                    _formState.value = CreateSpaceFormState()
                }
                .onFailure { error ->
                    _formState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Ocurrió un error") }
                }
        }
    }
}
