package com.kerpun.tutu.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TutuColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textFaint: Color,
    val border: Color,
    val tabBarBg: Color,
    val toastBg: Color,
    val toastBorder: Color,
    val accent: Color = Color(0xFF4E8CFF),
    val income: Color = Color(0xFF3ECF7A),
    val expense: Color = Color(0xFFFF6B6B),
    val expenseStrong: Color = Color(0xFFF0555F),
)

val TutuDarkColors = TutuColors(
    bg = Color(0xFF111214),
    surface = Color(0xFF1B1C1F),
    surface2 = Color(0xFF20222B),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0x99F5F5F7),
    textTertiary = Color(0x73F5F5F7),
    textFaint = Color(0x59F5F5F7),
    border = Color(0x12FFFFFF),
    tabBarBg = Color(0xEB141518),
    toastBg = Color(0xFF2A2C30),
    toastBorder = Color(0x1AFFFFFF),
)

val TutuLightColors = TutuColors(
    bg = Color(0xFFF3F4F7),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFE7E8EC),
    textPrimary = Color(0xFF16171A),
    textSecondary = Color(0x8C16171A),
    textTertiary = Color(0x6B16171A),
    textFaint = Color(0x5216171A),
    border = Color(0x12000000),
    tabBarBg = Color(0xD9FFFFFF),
    toastBg = Color(0xFFFFFFFF),
    toastBorder = Color(0x14000000),
)

val LocalTutuColors = staticCompositionLocalOf { TutuDarkColors }
