package com.kerpun.tutu.data.repository.fake

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import com.kerpun.tutu.data.repository.TransactionRepository
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class FakeTransactionRepository : TransactionRepository {

    private val nextId = AtomicLong(seedTransactions.size + 1L)
    private val transactions = MutableStateFlow(seedTransactions)

    override fun observeTransactions(): Flow<List<Transaction>> = transactions.asStateFlow()

    override suspend fun addTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        description: String?,
        occurredAt: LocalDate,
    ): Transaction {
        val transaction = Transaction(
            id = nextId.getAndIncrement(),
            type = type,
            amount = amount,
            categoryId = categoryId,
            description = description,
            occurredAt = occurredAt,
        )
        transactions.update { listOf(transaction) + it }
        return transaction
    }

    override suspend fun deleteTransaction(id: Long) {
        transactions.update { list -> list.filterNot { it.id == id } }
    }

    companion object {
        // categoryId según FakeCategoryRepository: 1 Comida, 2 Transporte, 3 Hogar, 4 Salud,
        // 5 Ocio, 6 Otros(gasto), 7 Salario, 8 Freelance, 9 Ventas, 10 Otros(ingreso)
        private val seedTransactions = listOf(
            Transaction(1, TransactionType.INCOME, 2800.0, 7, "Pago mensual", LocalDate(2026, 7, 24)),
            Transaction(2, TransactionType.INCOME, 450.0, 8, "Proyecto de diseño", LocalDate(2026, 7, 22)),
            Transaction(3, TransactionType.EXPENSE, 38.5, 1, "Almuerzo", LocalDate(2026, 7, 25)),
            Transaction(4, TransactionType.EXPENSE, 92.0, 1, "Supermercado", LocalDate(2026, 7, 23)),
            Transaction(5, TransactionType.EXPENSE, 15.0, 2, "Taxi", LocalDate(2026, 7, 23)),
            Transaction(6, TransactionType.EXPENSE, 120.0, 3, "Luz y agua", LocalDate(2026, 7, 20)),
            Transaction(7, TransactionType.EXPENSE, 60.0, 5, "Cine", LocalDate(2026, 7, 19)),
            Transaction(8, TransactionType.EXPENSE, 45.0, 4, "Farmacia", LocalDate(2026, 7, 18)),
        )
    }
}
