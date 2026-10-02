package com.kerpun.tutu.ui.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kerpun.tutu.ui.common.CheckmarkIcon
import com.kerpun.tutu.ui.common.ConfirmDeleteDialog
import com.kerpun.tutu.ui.common.TutuViewModelFactory
import com.kerpun.tutu.ui.common.toComposeColor
import com.kerpun.tutu.ui.theme.LocalTutuColors
import com.kerpun.tutu.ui.theme.TutuColors

@Composable
fun ApprovalsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApprovalsViewModel = viewModel(factory = TutuViewModelFactory),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalTutuColors.current
    var rejectConfirmOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = if (state.approveBarVisible) 110.dp else 30.dp,
            ),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(text = "Aprobaciones", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                                text = "${state.spaceName} · ${state.subtitle}",
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

            item { Box(modifier = Modifier.height(18.dp)) }

            if (state.batches.isEmpty()) {
                state.emptyText?.let { text ->
                    item { Text(text = text, color = colors.textTertiary, fontSize = 13.sp) }
                }
            } else {
                items(state.batches, key = { it.key }) { batch ->
                    ApprovalBatchCard(
                        batch = batch,
                        onToggleItem = viewModel::toggleItem,
                        colors = colors,
                    )
                    Box(modifier = Modifier.height(26.dp))
                }
            }
        }

        if (state.approveBarVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.tabBarBg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Rechazar",
                    color = colors.expense,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { rejectConfirmOpen = true },
                        ),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.ink)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = viewModel::approveSelected,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Aprobar (${state.selectedCount})", color = colors.inkForeground, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        ConfirmDeleteDialog(
            title = if (state.selectedCount == 1) "¿Rechazar este movimiento?" else "¿Rechazar ${state.selectedCount} movimientos?",
            subtitle = "Quien los anotó verá que fueron rechazados. No afectarán el saldo.",
            visible = rejectConfirmOpen,
            confirmLabel = "Rechazar",
            onDismiss = { rejectConfirmOpen = false },
            onConfirm = {
                rejectConfirmOpen = false
                viewModel.rejectSelected()
            },
        )
    }
}

@Composable
private fun ApprovalBatchCard(
    batch: ApprovalBatch,
    onToggleItem: (Long) -> Unit,
    colors: TutuColors,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(batch.color.toComposeColor()),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = batch.initial, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = batch.memberLabel,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 11.dp, end = 8.dp),
            )
            Text(text = batch.metaText, color = colors.textTertiary, fontSize = 12.sp)
        }

        batch.items.forEach { item ->
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = item.canToggle,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onToggleItem(item.id) },
                    )
                    .padding(vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(21.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (item.checked) colors.accent else Color.Transparent)
                        .then(
                            if (item.checked) Modifier else Modifier.border(1.5.dp, colors.border, RoundedCornerShape(11.dp)),
                        )
                        .alpha(if (item.canToggle) 1f else 0.35f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.checked) {
                        CheckmarkIcon(color = Color(0xFF04122E), iconSize = 11.dp, strokeWidth = 1.8.dp)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                    Text(
                        text = item.label,
                        color = colors.textPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(text = item.subLabel, color = colors.textTertiary, fontSize = 12.sp)
                }
                Text(
                    text = item.amountText,
                    color = item.amountColor.toComposeColor(),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Total", color = colors.textTertiary, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
            Text(
                text = batch.totalText,
                color = batch.totalColor.toComposeColor(),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        batch.statusNote?.let { note ->
            Text(
                text = note,
                color = batch.statusColor?.toComposeColor() ?: colors.pending,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
