package com.kerpun.tutu.data.repository

import com.kerpun.tutu.data.model.Space
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {
    fun observeSpaces(): Flow<List<Space>>

    /** True until the first successful load completes; false from then on. */
    fun observeIsLoading(): Flow<Boolean>

    suspend fun createSpace(name: String, color: String): Space

    suspend fun listMembers(spaceId: String): List<SpaceMember>

    suspend fun inviteMember(spaceId: String, email: String, role: SpaceRole)

    suspend fun setMemberRole(spaceId: String, userId: String, role: SpaceRole)

    suspend fun removeMember(spaceId: String, userId: String)

    suspend fun setRequiresApproval(spaceId: String, userId: String, value: Boolean)
}
