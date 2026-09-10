package com.kerpun.tutu.ui.home

import androidx.compose.ui.graphics.Color

data class SummaryCard(
    val key: String,
    val glyph: String,
    val iconBg: Color,
    val iconFg: Color,
    val body: String,
    val clickable: Boolean = false,
)
