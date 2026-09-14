package com.kerpun.tutu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kerpun.tutu.ui.theme.LocalTutuColors

/**
 * Overlapping avatar circles with the initials of the first [maxVisible] members. Any
 * remaining members are collapsed into a trailing "+N" bubble instead of a text count,
 * so the whole "who and how many" story fits in one compact element.
 */
@Composable
fun AvatarStack(
    avatars: List<MemberAvatarUi>,
    size: Dp,
    borderColor: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 2,
) {
    val colors = LocalTutuColors.current
    val visible = avatars.take(maxVisible)
    val remaining = avatars.size - visible.size

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-7).dp)) {
        visible.forEach { avatar ->
            AvatarBubble(
                modifier = Modifier,
                size = size,
                background = avatar.color.toComposeColor(),
                borderColor = borderColor,
            ) {
                Text(text = avatar.initial, color = Color.White, fontSize = fontSize, fontWeight = FontWeight.Bold)
            }
        }
        if (remaining > 0) {
            AvatarBubble(
                modifier = Modifier,
                size = size,
                background = colors.surface2,
                borderColor = borderColor,
            ) {
                Text(
                    text = "+$remaining",
                    color = colors.textSecondary,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AvatarBubble(
    modifier: Modifier,
    size: Dp,
    background: Color,
    borderColor: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
