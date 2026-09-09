package com.kerpun.tutu.ui.spaces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.data.model.Space
import com.kerpun.tutu.data.model.SpaceRole
import com.kerpun.tutu.ui.common.AvatarStack
import com.kerpun.tutu.ui.common.MemberAvatarUi
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun SpacesScreen(
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
    signedInEmail: String? = null,
    onSignOut: () -> Unit = {},
    viewModel: SpacesViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val colors = LocalTutuColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 30.dp,
        ),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = "Espacios", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (state.spaces.isEmpty()) "Creá tu primer espacio para empezar" else "Elegí en qué espacio querés trabajar",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (onClose != null) {
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
        }

        if (onClose == null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Conectado como ${signedInEmail ?: "..."}",
                        color = colors.textTertiary,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "Cerrar sesión",
                        color = colors.expense,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSignOut,
                        ),
                    )
                }
            }
        }

        if (state.spaces.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(18.dp)) }
            items(state.spaces, key = { it.id }) { space ->
                SpaceRow(
                    space = space,
                    isActive = space.id == state.activeSpaceId,
                    avatars = state.memberAvatarsBySpaceId[space.id] ?: emptyList(),
                    onClick = {
                        viewModel.selectSpace(space.id)
                        onClose?.invoke()
                    },
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Text(text = "Crear espacio", color = colors.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))

                TextField(
                    value = formState.name,
                    onValueChange = viewModel::setNewSpaceName,
                    placeholder = { Text("Nombre del espacio", color = colors.textTertiary) },
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
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SpaceColorPalette.forEach { hex ->
                        val selected = hex == formState.color
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (selected) colors.textPrimary else Color.Transparent, CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(hex.toComposeColor())
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.setNewSpaceColor(hex) },
                                ),
                        )
                    }
                }

                formState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = colors.expense,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .alpha(if (formState.canSubmit) 1f else 0.5f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accent)
                        .clickable(
                            enabled = formState.canSubmit,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = viewModel::createSpace,
                        )
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Crear espacio", color = Color(0xFF04122E), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SpaceRow(
    space: Space,
    isActive: Boolean,
    avatars: List<MemberAvatarUi>,
    onClick: () -> Unit,
    colors: TutuColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(space.color.toComposeColor()),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = space.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = space.name, color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (space.role == SpaceRole.ADMIN) "Admin" else "Miembro",
                color = colors.textTertiary,
                fontSize = 12.sp,
            )
        }
        if (avatars.isNotEmpty()) {
            AvatarStack(
                avatars = avatars,
                size = 24.dp,
                borderColor = colors.surface,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 7.dp),
            )
        }
        if (isActive) {
            CheckmarkIcon(color = colors.accent, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun CheckmarkIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.1875f, size.height * 0.53125f)
            lineTo(size.width * 0.3875f, size.height * 0.73125f)
            lineTo(size.width * 0.8125f, size.height * 0.3125f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
