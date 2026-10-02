package com.kerpun.tutu.ui.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.data.model.SpaceMember
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.ui.common.ChevronDownIcon
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.spaces.colorForMemberId
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun MembersScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MembersViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val inviteForm by viewModel.inviteForm.collectAsState()
    val colors = LocalTutuColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 30.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(text = "Miembros", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(state.spaceColor.toComposeColor()),
                        )
                        Text(
                            text = "${state.spaceName} · ${state.members.size} ${if (state.members.size == 1) "miembro" else "miembros"}",
                            color = colors.textTertiary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "✕", color = colors.textSecondary, fontSize = 14.sp)
                }
            }
        }

        state.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = colors.expense,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        item {
            var openMemberId by remember { mutableStateOf<String?>(null) }
            Column {
                state.members.forEach { member ->
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    val isSelf = member.userId == state.currentUserId
                    val canGate = state.isCurrentUserAdmin && !isSelf
                    MemberRow(
                        member = member,
                        isSelf = isSelf,
                        canManage = state.isCurrentUserAdmin,
                        canGate = canGate,
                        isOpen = canGate && openMemberId == member.userId,
                        onToggleRole = {
                            viewModel.setRole(
                                member.userId,
                                if (member.role == SpaceRole.ADMIN) SpaceRole.MEMBER else SpaceRole.ADMIN,
                            )
                        },
                        onToggleOpen = { openMemberId = if (openMemberId == member.userId) null else member.userId },
                        onToggleGate = { viewModel.setRequiresApproval(member.userId, !member.requiresApproval) },
                        onRemove = {
                            openMemberId = null
                            viewModel.removeMember(member.userId)
                        },
                        colors = colors,
                    )
                }
            }
        }

        if (state.isCurrentUserAdmin) {
            item {
                Column(modifier = Modifier.padding(top = 30.dp)) {
                    Text(
                        text = "INVITAR MIEMBRO",
                        color = colors.textFaint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )

                    TextField(
                        value = inviteForm.email,
                        onValueChange = viewModel::setInviteEmail,
                        placeholder = { Text("correo@ejemplo.com", color = colors.textTertiary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(text = "Entra como admin", color = colors.textPrimary, fontSize = 14.5.sp)
                            Text(
                                text = "Podrá invitar y aprobar movimientos",
                                color = colors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Switch(
                            checked = inviteForm.role == SpaceRole.ADMIN,
                            onCheckedChange = { viewModel.toggleInviteRole() },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.accent),
                        )
                    }

                    inviteForm.errorMessage?.let { message ->
                        Text(text = message, color = colors.expense, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .alpha(if (inviteForm.canSubmit) 1f else 0.5f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.ink)
                            .clickable(
                                enabled = inviteForm.canSubmit,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = viewModel::invite,
                            )
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Enviar invitación", color = colors.inkForeground, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 30.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Text(
                    text = "Salir del espacio",
                    color = colors.expense,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = viewModel::leaveSpace,
                        )
                        .padding(vertical = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: SpaceMember,
    isSelf: Boolean,
    canManage: Boolean,
    canGate: Boolean,
    isOpen: Boolean,
    onToggleRole: () -> Unit,
    onToggleOpen: () -> Unit,
    onToggleGate: () -> Unit,
    onRemove: () -> Unit,
    colors: TutuColors,
) {
    val avatarColor = colorForMemberId(member.userId)
    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(avatarColor.toComposeColor()),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = member.email.take(1).uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
            ) {
                Text(
                    text = if (isSelf) "${member.email} (vos)" else member.email,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = if (member.role == SpaceRole.ADMIN) "Admin" else "Miembro",
                color = if (member.role == SpaceRole.ADMIN) colors.accent else colors.textTertiary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .let { base ->
                        if (canManage && !isSelf) {
                            base.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleRole,
                            )
                        } else {
                            base
                        }
                    },
            )

            if (canGate) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleOpen,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ChevronDownIcon(
                        color = colors.textTertiary,
                        modifier = Modifier.rotate(if (isOpen) 180f else 0f),
                    )
                }
            }
        }

        if (isOpen) {
            Column(modifier = Modifier.padding(start = 46.dp, end = 4.dp, bottom = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (member.requiresApproval) "Requiere aprobación" else "Anota libre",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = member.requiresApproval,
                        onCheckedChange = { onToggleGate() },
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.pending),
                    )
                }
                Text(
                    text = "Quitar del espacio",
                    color = colors.expense,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRemove,
                        ),
                )
            }
        }
    }
}
