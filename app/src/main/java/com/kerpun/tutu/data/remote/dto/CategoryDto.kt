package com.kerpun.tutu.data.remote.dto

import com.kerpun.tutu.data.model.Category
import com.kerpun.tutu.data.model.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryRow(
    val id: Long,
    val name: String,
    val type: String,
    val color: String,
    val icon: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
) {
    fun toDomain() = Category(
        id = id,
        name = name,
        type = TransactionType.fromDb(type),
        color = color,
        icon = icon,
        isDefault = isDefault,
    )
}

@Serializable
data class CategoryInsert(
    val name: String,
    val type: String,
    val color: String,
    val icon: String?,
)

@Serializable
data class CategoryUpdate(
    val name: String,
    val type: String,
    val color: String,
    val icon: String?,
)
