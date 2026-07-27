package com.kerpun.tutu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val MaterialDarkScheme = darkColorScheme(primary = TutuDarkColors.accent)
private val MaterialLightScheme = lightColorScheme(primary = TutuLightColors.accent)

@Composable
fun TutuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tutuColors = if (darkTheme) TutuDarkColors else TutuLightColors
    CompositionLocalProvider(LocalTutuColors provides tutuColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) MaterialDarkScheme else MaterialLightScheme,
            typography = Typography,
            content = content,
        )
    }
}
