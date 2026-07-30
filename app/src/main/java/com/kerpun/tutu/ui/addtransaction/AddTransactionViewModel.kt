package com.kerpun.tutu.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.model.VAULT_WITHDRAWAL_CATEGORY_NAME
import com.kerpun.tutu.data.model.toBalanceSummary
import com.kerpun.tutu.data.repository.CategoryRepository
import com.kerpun.tutu.data.repository.TransactionRepository
import com.kerpun.tutu.ui.common.formatAmount
import com.kerpun.tutu.ui.common.todayLocalDate
import kotlin.math.roundToLong
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

    private val editingId = MutableStateFlow<Long?>(null)
    private val type = MutableStateFlow(TransactionType.EXPENSE)
    private val amountInput = MutableStateFlow("")
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val description = MutableStateFlow("")
    private val isSaving = MutableStateFlow(false)

    private val savedEventsFlow = MutableSharedFlow<String>()
    val savedEvents: SharedFlow<String> = savedEventsFlow.asSharedFlow()

    private data class FormInputs(
        val editingId: Long?,
        val type: TransactionType,
        val amountInput: String,
        val selectedCategoryId: Long?,
        val description: String,
        val isSaving: Boolean,
    )

    private data class EditingAndType(val editingId: Long?, val type: TransactionType)
    private data class AmountAndCategory(val amountInput: String, val selectedCategoryId: Long?)

    private val formInputs: Flow<FormInputs> = combine(
        combine(editingId, type, ::EditingAndType),
        combine(amountInput, selectedCategoryId, ::AmountAndCategory),
        description,
        isSaving,
    ) { editingAndType, amountAndCategory, currentDescription, currentIsSaving ->
        FormInputs(
            editingId = editingAndType.editingId,
            type = editingAndType.type,
            amountInput = amountAndCategory.amountInput,
            selectedCategoryId = amountAndCategory.selectedCategoryId,
            description = currentDescription,
            isSaving = currentIsSaving,
        )
    }

    val uiState: StateFlow<AddTransactionUiState> = combine(
        formInputs,
        categoryRepository.observeCategories(),
        transactionRepository.observeTransactions(),
    ) { inputs, allCategories, transactions ->
        val categoriesForType = allCategories.filter { it.type == inputs.type }
        val resolvedSelectedId = inputs.selectedCategoryId
            ?.takeIf { id -> categoriesForType.any { it.id == id } }
            ?: categoriesForType.firstOrNull()?.id

        val summary = transactions.toBalanceSummary(allCategories.associateBy { it.id })
        val selectedCategoryName = allCategories.find { it.id == resolvedSelectedId }?.name
        val contextText = if (inputs.type == TransactionType.VAULT && selectedCategoryName == VAULT_WITHDRAWAL_CATEGORY_NAME) {
            "En Vault: ${formatAmount(summary.vaultBalance)}"
        } else {
            "Disponible: ${formatAmount(summary.availableBalance)}"
        }

        AddTransactionUiState(
            type = inputs.type,
            amountInput = inputs.amountInput,
            categories = categoriesForType,
            selectedCategoryId = resolvedSelectedId,
            description = inputs.description,
            canSave = inputs.amountInput.toDoubleOrNull()?.let { it > 0.0 } == true && !inputs.isSaving,
            isSaving = inputs.isSaving,
            editingId = inputs.editingId,
            contextText = contextText,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddTransactionUiState(),
    )

    fun startCreating() {
        editingId.value = null
        type.value = TransactionType.EXPENSE
        amountInput.value = ""
        selectedCategoryId.value = null
        description.value = ""
    }

    fun startEditing(
        id: Long,
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
    ) {
        editingId.value = id
        this.type.value = type
        amountInput.value = amount.toAmountInput()
        selectedCategoryId.value = categoryId
        this.description.value = description ?: ""
    }

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
            val editId = current.editingId
            if (editId != null) {
                transactionRepository.updateTransaction(
                    id = editId,
                    type = current.type,
                    amount = amount,
                    categoryId = current.selectedCategoryId,
                    description = current.description.trim().ifBlank { null },
                )
            } else {
                transactionRepository.addTransaction(
                    type = current.type,
                    amount = amount,
                    categoryId = current.selectedCategoryId,
                    description = current.description.trim().ifBlank { null },
                    occurredAt = todayLocalDate(),
                )
            }
            editingId.value = null
            amountInput.value = ""
            description.value = ""
            isSaving.value = false
            savedEventsFlow.emit(if (editId != null) "Cambios guardados" else "Transacción agregada")
        }
    }
}

private fun Double.toAmountInput(): String {
    val rounded = (this * 100).roundToLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
