package com.kerpun.tutu.ui.common

import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.ui.spaces.colorForMemberId

data class MemberAvatarUi(
    val initial: String,
    val color: String,
)

fun SpaceMember.toAvatarUi() = MemberAvatarUi(
    initial = email.take(1).uppercase(),
    color = colorForMemberId(userId),
)
