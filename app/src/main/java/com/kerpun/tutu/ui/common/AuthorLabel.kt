package com.kerpun.tutu.ui.common

import com.kerpun.tutu.data.model.SpaceMember

/**
 * Null in a solo space (attribution is pointless when there's only one possible author) or
 * when the creator can't be resolved (e.g. they left the space since).
 */
fun authorLabelFor(createdBy: String?, members: List<SpaceMember>, currentUserId: String?): String? {
    if (createdBy == null || members.size <= 1) return null
    if (createdBy == currentUserId) return "Vos"
    val member = members.find { it.userId == createdBy } ?: return null
    return member.email.substringBefore("@").replaceFirstChar { it.uppercase() }
}
