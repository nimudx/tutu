package com.kerpun.tutu.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.common.todayLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_AMOUNT_DIGITS = 7
private const val BACKSPACE_KEY = "⌫"
private const val DECIMAL_KEY = "."

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val type = MutableStateFlow(TransactionType.EXPENSE)
    private val amountInput = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val description = MutableStateFlow("")
    private val isSaving = MutableStateFlow(false)

    private val savedEventsFlow = MutableSharedFlow<Unit>()
    val savedEvents: SharedFlow<Unit> = savedEventsFlow.asSharedFlow()

    private data class FormInputs(
        val type: TransactionType,
        val amountInput: String,
        val selectedCategoryId: Long?,
        val description: String,
        val isSaving: Boolean,
    )

    private val formInputs: Flow<FormInputs> = combine(
        type, amountInput, selectedCategoryId, description, isSaving,
    ) { currentType, currentAmount, currentCategoryId, currentDescription, currentIsSaving ->
        FormInputs(currentType, currentAmount, currentCategoryId, currentDescription, currentIsSaving)
    }

    val uiState: StateFlow<AddTransactionUiState> = combine(
        formInputs,
        categoryRepository.observeCategories(),
    ) { inputs, allCategories ->
        val categoriesForType = allCategories.filter { it.type == inputs.type }
        val resolvedSelectedId = inputs.selectedCategoryId
            ?.takeIf { id -> categoriesForType.any { it.id == id } }
            ?: categoriesForType.firstOrNull()?.id

        AddTransactionUiState(
            type = inputs.type,
            amountInput = inputs.amountInput,
            categories = categoriesForType,
            selectedCategoryId = resolvedSelectedId,
            description = inputs.description,
            canSave = inputs.amountInput.toDoubleOrNull()?.let { it > 0.0 } == true && !inputs.isSaving,
            isSaving = inputs.isSaving,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddTransactionUiState(),
    )

    fun setType(newType: TransactionType) {
        type.value = newType
        selectedCategoryId.value = null
    }

    fun selectCategory(categoryId: Long) {
        selectedCategoryId.value = categoryId
    }

    fun setDescription(text: String) {
        description.value = text
    }

    fun pressKey(key: String) {
        amountInput.update { current ->
            when {
                key == BACKSPACE_KEY -> current.dropLast(1)
                key == DECIMAL_KEY -> if (current.contains(DECIMAL_KEY)) current else current + DECIMAL_KEY
                current.replace(DECIMAL_KEY, "").length >= MAX_AMOUNT_DIGITS -> current
                else -> current + key
            }
        }
    }

    fun save() {
        val current = uiState.value
        val amount = current.amountInput.toDoubleOrNull() ?: return
        if (amount <= 0.0 || current.isSaving) return

        isSaving.value = true
        viewModelScope.launch {
            transactionRepository.addTransaction(
                type = current.type,
                amount = amount,
                categoryId = current.selectedCategoryId,
                description = current.description.trim().ifBlank { null },
                occurredAt = todayLocalDate(),
            )
            amountInput.value = ""
            description.value = ""
            isSaving.value = false
            savedEventsFlow.emit(Unit)
        }
    }
}
