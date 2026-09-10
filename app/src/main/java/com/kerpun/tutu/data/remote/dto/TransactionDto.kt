package com.kerpun.tutu.data.remote.dto

import com.kerpun.tutu.data.model.Transaction
import com.kerpun.tutu.data.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRow(
    val id: Long,
    val type: String,
    val amount: Double,
    @SerialName("category_id") val categoryId: Long? = null,
    val description: String? = null,
    @SerialName("occurred_at") val occurredAt: LocalDate,
    @SerialName("space_id") val spaceId: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
) {
    fun toDomain() = Transaction(
        id = id,
        type = TransactionType.fromDb(type),
        amount = amount,
        categoryId = categoryId,
        description = description,
        occurredAt = occurredAt,
        createdBy = createdBy,
    )
}

@Serializable
data class TransactionInsert(
    val type: String,
    val amount: Double,
    @SerialName("category_id") val categoryId: Long?,
    val description: String?,
    @SerialName("occurred_at") val occurredAt: LocalDate,
    @SerialName("space_id") val spaceId: String,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class TransactionUpdate(
    val type: String,
    val amount: Double,
    @SerialName("category_id") val categoryId: Long?,
    val description: String?,
)
