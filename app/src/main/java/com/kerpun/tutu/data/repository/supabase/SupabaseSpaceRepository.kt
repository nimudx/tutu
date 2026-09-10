package com.kerpun.tutu.data.repository.supabase

import com.kerpun.tutu.data.model.Space
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.data.remote.dto.InviteMemberParams
import com.kerpun.tutu.data.remote.dto.ListMembersParams
import com.kerpun.tutu.data.remote.dto.RemoveMemberParams
import com.kerpun.tutu.data.remote.dto.SetMemberRoleParams
import com.kerpun.tutu.data.remote.dto.SetRequiresApprovalParams
import com.kerpun.tutu.data.remote.dto.SpaceInsert
import com.kerpun.tutu.data.remote.dto.SpaceMemberDetailRow
import com.kerpun.tutu.data.remote.dto.SpaceMemberInsert
import com.kerpun.tutu.data.remote.dto.SpaceMemberRow
import com.kerpun.tutu.data.remote.dto.SpaceRow
import com.kerpun.tutu.data.repository.SpaceRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val SPACES_TABLE = "spaces"
private const val SPACE_MEMBERS_TABLE = "space_members"
private const val ADMIN_ROLE = "admin"

class SupabaseSpaceRepository(
    private val postgrest: Postgrest,
    private val auth: Auth,
) : SpaceRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val spaces = MutableStateFlow<List<Space>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> refresh(status.session.user?.id)
                    is SessionStatus.NotAuthenticated -> {
                        spaces.value = emptyList()
                        isLoading.value = false
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun observeSpaces(): Flow<List<Space>> = spaces.asStateFlow()

    override fun observeIsLoading(): Flow<Boolean> = isLoading.asStateFlow()

    override suspend fun createSpace(name: String, color: String): Space {
        val userId = requireUserId()
        val spaceRow = postgrest.from(SPACES_TABLE)
            .insert(SpaceInsert(name = name, color = color, createdBy = userId)) {
                select()
            }
            .decodeSingle<SpaceRow>()

        postgrest.from(SPACE_MEMBERS_TABLE)
            .insert(SpaceMemberInsert(spaceId = spaceRow.id, userId = userId, role = ADMIN_ROLE))

        refresh(userId)
        return Space(id = spaceRow.id, name = spaceRow.name, color = spaceRow.color, role = SpaceRole.ADMIN)
    }

    private suspend fun refresh(userId: String?) {
        if (userId == null) {
            spaces.value = emptyList()
            isLoading.value = false
            return
        }

        runCatching {
            val memberships = postgrest.from(SPACE_MEMBERS_TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList<SpaceMemberRow>()

            val spaceIds = memberships.map { it.spaceId }
            val rows = if (spaceIds.isEmpty()) {
                emptyList()
            } else {
                postgrest.from(SPACES_TABLE)
                    .select {
                        filter { isIn("id", spaceIds) }
                        order(column = "created_at", order = Order.ASCENDING)
                    }
                    .decodeList<SpaceRow>()
            }

            val roleBySpaceId = memberships.associate { it.spaceId to it.role }
            rows.map { row ->
                Space(
                    id = row.id,
                    name = row.name,
                    color = row.color,
                    role = if (roleBySpaceId[row.id] == ADMIN_ROLE) SpaceRole.ADMIN else SpaceRole.MEMBER,
                )
            }
        }.onSuccess { spaces.value = it }
        // On failure (network hiccup, RLS/schema not set up yet, etc.) keep whatever
        // spaces were last known instead of crashing the app.
        isLoading.value = false
    }

    override suspend fun listMembers(spaceId: String): List<SpaceMember> {
        return postgrest.rpc("list_space_members", ListMembersParams(spaceId = spaceId))
            .decodeList<SpaceMemberDetailRow>()
            .map {
                SpaceMember(
                    userId = it.userId,
                    email = it.email,
                    role = SpaceRole.fromDb(it.role),
                    requiresApproval = it.requiresApproval,
                )
            }
    }

    override suspend fun inviteMember(spaceId: String, email: String, role: SpaceRole) {
        postgrest.rpc("invite_member", InviteMemberParams(spaceId = spaceId, email = email, role = role.toDb()))
    }

    override suspend fun setMemberRole(spaceId: String, userId: String, role: SpaceRole) {
        postgrest.rpc("set_member_role", SetMemberRoleParams(spaceId = spaceId, userId = userId, role = role.toDb()))
    }

    override suspend fun removeMember(spaceId: String, userId: String) {
        postgrest.rpc("remove_member", RemoveMemberParams(spaceId = spaceId, userId = userId))
    }

    override suspend fun setRequiresApproval(spaceId: String, userId: String, value: Boolean) {
        postgrest.rpc("set_requires_approval", SetRequiresApprovalParams(spaceId = spaceId, userId = userId, value = value))
    }

    private fun requireUserId(): String = auth.currentUserOrNull()?.id
        ?: error("No hay sesión activa")
}
