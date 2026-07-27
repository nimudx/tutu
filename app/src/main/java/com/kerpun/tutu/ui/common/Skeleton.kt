package com.kerpun.tutu.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kerpun.tutu.ui.theme.LocalTutuColors

private const val SHIMMER_DURATION_MS = 1300
private const val SHIMMER_SPAN = 600f

/** Sliding shimmer gradient, matching the design's `tutuShimmer` keyframe. */
@Composable
fun rememberShimmerBrush(): Brush {
    val colors = LocalTutuColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -SHIMMER_SPAN,
        targetValue = SHIMMER_SPAN,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors = listOf(colors.skeletonBase, colors.skeletonHighlight, colors.skeletonBase),
        start = Offset(translate - SHIMMER_SPAN, 0f),
        end = Offset(translate + SHIMMER_SPAN, 0f),
    )
}

@Composable
fun SkeletonBlock(
    width: Dp,
    height: Dp,
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(brush),
    )
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    brush: Brush,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    Box(modifier = modifier.clip(shape).background(brush))
}

/** Matches the design's balance-card skeleton: two bars, a divider, and two stat columns. */
@Composable
fun BalanceCardSkeleton(brush: Brush, modifier: Modifier = Modifier) {
    val colors = LocalTutuColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surface)
            .padding(24.dp),
    ) {
        SkeletonBlock(width = 100.dp, height = 11.dp, brush = brush, shape = RoundedCornerShape(6.dp))
        Box(modifier = Modifier.padding(top = 16.dp)) {
            SkeletonBlock(width = 180.dp, height = 34.dp, brush = brush, shape = RoundedCornerShape(8.dp))
        }
        Box(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 18.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            repeat(2) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonBlock(width = 50.dp, height = 9.dp, brush = brush, shape = RoundedCornerShape(5.dp))
                    SkeletonBlock(width = 70.dp, height = 14.dp, brush = brush, shape = RoundedCornerShape(6.dp))
                }
            }
        }
    }
}

/** Matches the design's transaction-row skeleton: icon + two text lines + amount. */
@Composable
fun TransactionRowSkeleton(brush: Brush, modifier: Modifier = Modifier) {
    val colors = LocalTutuColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.6f).height(13.dp), brush = brush, shape = RoundedCornerShape(6.dp))
            SkeletonBlock(modifier = Modifier.fillMaxWidth(0.35f).height(10.dp), brush = brush, shape = RoundedCornerShape(5.dp))
        }
        SkeletonBlock(width = 50.dp, height = 14.dp, brush = brush, shape = RoundedCornerShape(6.dp))
    }
}
