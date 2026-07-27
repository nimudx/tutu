package com.kerpun.tutu.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.ui.theme.LocalTutuColors
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val ACTION_BUTTON_WIDTH = 74.dp
private const val SNAP_FRACTION_THRESHOLD = 0.34f // -50px / -148px in the original design

@Composable
fun SwipeActionsTransactionRow(
    transaction: TransactionUi,
    isRevealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTutuColors.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val maxOffsetPx = with(density) { -(2 * ACTION_BUTTON_WIDTH.toPx()) }

    LaunchedEffect(isRevealed, maxOffsetPx) {
        val target = if (isRevealed) maxOffsetPx else 0f
        if (offsetX.value != target) offsetX.animateTo(target)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp)),
            horizontalArrangement = Arrangement.End,
        ) {
            ActionButton(label = "Editar", background = colors.accent, width = ACTION_BUTTON_WIDTH, onClick = onEdit)
            ActionButton(label = "Eliminar", background = colors.expenseStrong, width = ACTION_BUTTON_WIDTH, onClick = onDelete)
        }

        TransactionRowContent(
            transaction = transaction,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val next = (offsetX.value + delta).coerceIn(maxOffsetPx, 0f)
                            offsetX.snapTo(next)
                        }
                    },
                    onDragStopped = {
                        val revealed = offsetX.value < maxOffsetPx * SNAP_FRACTION_THRESHOLD
                        onRevealedChange(revealed)
                        scope.launch { offsetX.animateTo(if (revealed) maxOffsetPx else 0f) }
                    },
                ),
        )
    }
}

@Composable
private fun ActionButton(label: String, background: Color, width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TransactionRowContent(transaction: TransactionUi, modifier: Modifier = Modifier) {
    val colors = LocalTutuColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(transaction.color.toComposeColor()),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = transaction.initial,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = transaction.label,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = transaction.dateLabel,
                color = colors.textTertiary,
                fontSize = 12.sp,
            )
        }
        Text(
            text = transaction.amountText,
            color = transaction.amountColor.toComposeColor(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
