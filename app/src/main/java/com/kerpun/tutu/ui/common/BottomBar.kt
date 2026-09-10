package com.kerpun.tutu.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.ui.theme.LocalTutuColors

enum class TutuTab(val label: String) {
    HOME("Inicio"),
    MOVEMENTS("Movimientos"),
    SETTINGS("Ajustes"),
}

@Composable
fun TutuBottomBar(
    selectedTab: TutuTab,
    onTabSelected: (TutuTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    pendingApprovalsCount: Int = 0,
    onPendingApprovalsClick: () -> Unit = {},
) {
    val colors = LocalTutuColors.current
    Box(
        modifier = modifier
            .background(colors.tabBarBg)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 10.dp, end = 8.dp, bottom = 18.dp),
        ) {
            TabItem(
                label = TutuTab.HOME.label,
                selected = selectedTab == TutuTab.HOME,
                onClick = { onTabSelected(TutuTab.HOME) },
                modifier = Modifier.weight(1f),
            ) { color ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            TabItem(
                label = TutuTab.MOVEMENTS.label,
                selected = selectedTab == TutuTab.MOVEMENTS,
                onClick = { onTabSelected(TutuTab.MOVEMENTS) },
                modifier = Modifier.weight(1f),
            ) { color ->
                MovementsIcon(color = color)
            }
            TabItem(
                label = TutuTab.SETTINGS.label,
                selected = selectedTab == TutuTab.SETTINGS,
                onClick = { onTabSelected(TutuTab.SETTINGS) },
                modifier = Modifier.weight(1f),
            ) { color ->
                SettingsIcon(color = color)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-22).dp, y = (-78).dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7FA8FF), colors.accent)))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAddClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(19.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
            )
            Box(
                modifier = Modifier
                    .width(19.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
            )
        }

        if (pendingApprovalsCount > 0) {
            PendingApprovalsBadge(
                count = pendingApprovalsCount,
                onClick = onPendingApprovalsClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-29).dp, y = (-146).dp),
            )
        }
    }
}

@Composable
private fun PendingApprovalsBadge(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTutuColors.current
    // The count pill sticks out past the circle's top-right corner, so it must be a sibling
    // OUTSIDE the clipped circle — a child of it would get its overflowing part cut off by
    // the circle's own clip.
    Box(modifier = modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(colors.toastBg)
                .border(1.dp, PendingAccent.copy(alpha = 0.45f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            ClockIcon(color = PendingAccent)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 3.dp, y = (-3).dp)
                .height(20.dp)
                .defaultMinSize(minWidth = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PendingAccent)
                .border(2.dp, colors.tabBarBg, RoundedCornerShape(10.dp))
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                color = Color(0xFF241505),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
            )
        }
    }
}

@Composable
private fun ClockIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2 * 0.85f, style = Stroke(width = 1.8.dp.toPx()))
        val cx = size.width / 2f
        val cy = size.height / 2f
        val path = Path().apply {
            moveTo(cx, cy - size.height * 0.17f)
            lineTo(cx, cy)
            lineTo(cx + size.width * 0.14f, cy + size.height * 0.1f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (Color) -> Unit,
) {
    val colors = LocalTutuColors.current
    val tint = if (selected) colors.accent else colors.textFaint
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        icon(tint)
        Text(text = label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MovementsIcon(color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(modifier = Modifier.width(16.dp).height(2.dp).background(color, RectangleShape))
        Box(modifier = Modifier.width(16.dp).height(2.dp).background(color, RectangleShape))
        Box(modifier = Modifier.width(9.6.dp).height(2.dp).background(color, RectangleShape))
    }
}

@Composable
private fun SettingsIcon(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(2.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}
