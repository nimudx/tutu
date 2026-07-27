package com.kerpun.tutu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
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
) {
    val colors = LocalTutuColors.current
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.tabBarBg)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 10.dp),
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
                .align(Alignment.TopEnd)
                .padding(end = 22.dp)
                .offset(y = (-56).dp)
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
