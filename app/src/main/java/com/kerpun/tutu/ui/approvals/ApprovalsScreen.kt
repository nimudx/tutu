package com.kerpun.tutu.ui.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 30.dp),
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
                    onApprove = { viewModel.approve(batch) },
                    onReject = { viewModel.reject(batch) },
                    colors = colors,
                )
                Box(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ApprovalBatchCard(
    batch: ApprovalBatch,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    colors: TutuColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(batch.color.toComposeColor()),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = batch.initial, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                Text(
                    text = batch.memberLabel,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = batch.metaText, color = colors.textTertiary, fontSize = 12.sp)
            }
            Text(
                text = batch.totalText,
                color = batch.totalColor.toComposeColor(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).height(1.dp).background(colors.border))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            batch.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.label,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = item.amountText,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (batch.canApprove) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface2)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onReject,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Rechazar todo", color = colors.expense, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onApprove,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Aprobar todo", color = colors.bg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (batch.statusNote != null) {
            Text(
                text = batch.statusNote,
                color = batch.statusColor?.toComposeColor() ?: colors.pending,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}
