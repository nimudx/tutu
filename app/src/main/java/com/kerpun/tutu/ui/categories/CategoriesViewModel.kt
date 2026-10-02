package com.kerpun.tutu.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.SpaceRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.spaces.SpaceColorPalette
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val spaceRepository: SpaceRepository,
    activeSpaceId: StateFlow<String?>,
) : ViewModel() {

    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)

    /** Raw categories, kept alongside the UI-mapped list so edits can preserve icon/isDefault. */
    private var latestCategories: List<Category> = emptyList()

    init {
        categoryRepository.observeCategories()
            .onEach { latestCategories = it }
            .launchIn(viewModelScope)
    }

    private val activeSpace = combine(spaceRepository.observeSpaces(), activeSpaceId) { spaces, id ->
        spaces.find { it.id == id }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeCategories(),
        categoryRepository.observeIsLoading(),
        transactionRepository.observeTransactions(),
        activeSpace,
        selectedType,
    ) { categories, isLoading, transactions, space, type ->
        val usageByCategory = transactions.groupingBy { it.categoryId }.eachCount()
        CategoriesUiState(
            spaceName = space?.name ?: "",
            spaceColor = space?.color ?: "#4E8CFF",
            rows = categories.map { category ->
                CategoryRowUi(
                    id = category.id,
                    name = category.name,
                    color = category.color,
                    type = category.type,
                    usageCount = usageByCategory[category.id] ?: 0,
                )
            },
            selectedType = type,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    private val _formState = MutableStateFlow(CategoryEditFormState())
    val formState: StateFlow<CategoryEditFormState> = _formState.asStateFlow()

    private val categoryCreatedFlow = MutableSharedFlow<Category>()
    /** Emits the newly created category so a caller (e.g. the add-transaction sheet) can auto-select it. */
    val categoryCreatedEvents: SharedFlow<Category> = categoryCreatedFlow.asSharedFlow()

    fun selectType(type: TransactionType) {
        selectedType.value = type
    }

    fun startCreating(type: TransactionType = selectedType.value) {
        _formState.value = CategoryEditFormState(isOpen = true, type = type, color = SpaceColorPalette.first())
    }

    fun startEditing(row: CategoryRowUi) {
        _formState.value = CategoryEditFormState(
            isOpen = true,
            editingId = row.id,
            name = row.name,
            type = row.type,
            color = row.color,
        )
    }

    fun closeForm() {
        _formState.update { it.copy(isOpen = false) }
    }

    fun setName(value: String) {
        _formState.update { it.copy(name = value, errorMessage = null) }
    }

    fun setType(type: TransactionType) {
        _formState.update { it.copy(type = type) }
    }

    fun setColor(color: String) {
        _formState.update { it.copy(color = color) }
    }

    fun save() {
        val form = _formState.value
        if (!form.canSubmit) return
        val name = form.name.trim()

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = runCatching {
                val editingId = form.editingId
                if (editingId != null) {
                    val existing = latestCategories.find { it.id == editingId }
                    categoryRepository.updateCategory(
                        Category(
                            id = editingId,
                            name = name,
                            type = form.type,
                            color = form.color,
                            icon = existing?.icon,
                            isDefault = existing?.isDefault ?: false,
                        ),
                    )
                    null
                } else {
                    categoryRepository.addCategory(name = name, type = form.type, color = form.color, icon = null)
                }
            }
            result
                .onSuccess { created ->
                    _formState.value = CategoryEditFormState(isOpen = false)
                    if (created != null) categoryCreatedFlow.emit(created)
                }
                .onFailure { error -> _formState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Ocurrió un error") } }
        }
    }

    fun delete() {
        val id = _formState.value.editingId ?: return
        val usageCount = uiState.value.rows.find { it.id == id }?.usageCount ?: 0
        if (usageCount > 0) {
            _formState.update { it.copy(errorMessage = "No se puede: hay movimientos con esta categoría") }
            return
        }
        viewModelScope.launch {
            runCatching { categoryRepository.deleteCategory(id) }
                .onSuccess { _formState.value = CategoryEditFormState(isOpen = false) }
                .onFailure { error -> _formState.update { it.copy(errorMessage = error.message ?: "Ocurrió un error") } }
        }
    }
}
