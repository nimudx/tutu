package com.kerpun.tutu.ui.common

import androidx.compose.ui.graphics.Color

fun String.toComposeColor(): Color = Color(android.graphics.Color.parseColor(this))
