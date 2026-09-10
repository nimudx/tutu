package com.kerpun.tutu.ui.common

import androidx.compose.ui.graphics.Color

/**
 * Fixed regardless of theme — matches the design's literal palette constants (not the
 * theme-varying `--tt-pending-ink` CSS var). Used where an element draws on its own chip
 * background rather than directly on the app bg, so it never needs to darken for contrast.
 */
val PendingAccent = Color(0xFFF0A048)
val AccentFixed = Color(0xFF4E8CFF)
