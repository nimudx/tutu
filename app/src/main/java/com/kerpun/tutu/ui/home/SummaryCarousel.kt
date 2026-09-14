package com.kerpun.tutu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.ui.theme.LocalTutuColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTOPLAY_DELAY_MS = 5_200L

/**
 * Single-card-at-a-time carousel — pending approvals, weekly insight, top spending category,
 * vault balance. Autoplays every 5.2s; a manual swipe or dot tap stops autoplay until the set
 * of cards itself changes (matches the source design 1:1).
 */
@Composable
fun SummaryCarousel(cards: List<SummaryCard>, onCardClick: (SummaryCard) -> Unit, modifier: Modifier = Modifier) {
    if (cards.isEmpty()) return
    val colors = LocalTutuColors.current
    val pagerState = rememberPagerState(pageCount = { cards.size })
    val scope = rememberCoroutineScope()
    var autoplayEnabled by remember(cards.size) { mutableStateOf(true) }
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged) {
        if (isDragged) autoplayEnabled = false
    }

    LaunchedEffect(cards.size) {
        if (cards.size < 2) return@LaunchedEffect
        while (true) {
            delay(AUTOPLAY_DELAY_MS)
            if (autoplayEnabled && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % cards.size)
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                val card = cards[page]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .let { m ->
                            if (card.clickable) {
                                m.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onCardClick(card) },
                                )
                            } else {
                                m
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(card.iconBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = card.glyph, color = card.iconFg, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        text = card.body,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f).padding(start = 11.dp),
                    )
                }
            }
        }

        if (cards.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                cards.indices.forEach { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .width(if (active) 16.dp else 5.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (active) colors.accent else colors.dot)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    autoplayEnabled = false
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                            ),
                    )
                }
            }
        }
    }
}
