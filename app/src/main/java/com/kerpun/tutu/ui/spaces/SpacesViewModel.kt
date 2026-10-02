package com.kerpun.tutu.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.repository.SpaceRepository
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

    val uiState: StateFlow<SpacesUiState> = combine(
        spaceRepository.observeSpaces(),
        spaceRepository.observeIsLoading(),
        activeSpaceId,
    ) { spaces, isLoading, activeId ->
        SpacesUiState(spaces = spaces, isLoading = isLoading, activeSpaceId = activeId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpacesUiState(),
    )

    private val _formState = MutableStateFlow(CreateSpaceFormState())
    val formState: StateFlow<CreateSpaceFormState> = _formState.asStateFlow()

    init {
        // Once the user's spaces load, default to the first one if nothing is active yet
        // (e.g. right after login, or after creating the very first space).
        viewModelScope.launch {
            spaceRepository.observeSpaces().collect { spaces ->
                if (activeSpaceId.value == null && spaces.isNotEmpty()) {
                    activeSpaceId.value = spaces.first().id
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
