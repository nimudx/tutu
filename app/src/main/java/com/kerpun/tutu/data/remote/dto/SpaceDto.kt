package com.kerpun.tutu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceRow(
    val id: String,
    val name: String,
    val color: String,
)

@Serializable
data class SpaceInsert(
    val name: String,
    val color: String,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
data class SpaceMemberRow(
    @SerialName("space_id") val spaceId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
)

@Serializable
data class SpaceMemberInsert(
    @SerialName("space_id") val spaceId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
)

/** Just the gate flag, for the single-row lookup before inserting a transaction. */
@Serializable
data class RequiresApprovalRow(
    @SerialName("requires_approval") val requiresApproval: Boolean,
)

/** Row returned by the `list_space_members` RPC (includes the member's email). */
@Serializable
data class SpaceMemberDetailRow(
    @SerialName("user_id") val userId: String,
    val email: String,
    val role: String,
    @SerialName("requires_approval") val requiresApproval: Boolean = false,
)

@Serializable
data class InviteMemberParams(
    @SerialName("p_space_id") val spaceId: String,
    @SerialName("p_email") val email: String,
    @SerialName("p_role") val role: String,
)

@Serializable
data class ListMembersParams(
    @SerialName("p_space_id") val spaceId: String,
)

@Serializable
data class SetMemberRoleParams(
    @SerialName("p_space_id") val spaceId: String,
    @SerialName("p_user_id") val userId: String,
    @SerialName("p_role") val role: String,
)

@Serializable
data class RemoveMemberParams(
    @SerialName("p_space_id") val spaceId: String,
    @SerialName("p_user_id") val userId: String,
)

@Serializable
data class SetRequiresApprovalParams(
    @SerialName("p_space_id") val spaceId: String,
    @SerialName("p_user_id") val userId: String,
    @SerialName("p_value") val value: Boolean,
)
