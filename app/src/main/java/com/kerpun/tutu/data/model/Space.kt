package com.kerpun.tutu.data.model

enum class SpaceRole {
    ADMIN,
    MEMBER,
    ;

    fun toDb(): String = name.lowercase()

    companion object {
        fun fromDb(value: String): SpaceRole = valueOf(value.uppercase())
    }
}

data class Space(
    val id: String,
    val name: String,
    val color: String,
    val role: SpaceRole,
)

data class SpaceMember(
    val userId: String,
    val email: String,
    val role: SpaceRole,
    val requiresApproval: Boolean = false,
)
