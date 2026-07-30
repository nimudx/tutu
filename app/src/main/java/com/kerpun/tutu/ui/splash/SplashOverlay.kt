package com.kerpun.tutu.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerpun.tutu.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Must match android:windowSplashScreenBackground in themes.xml exactly — no gradient,
 * so there's zero perceptible seam handing off from the native splash to this overlay. */
private val SplashBackground = Color(0xFF3D75E0)
private const val ENTRANCE_DURATION_MS = 700
private const val TEXT_DELAY_MS = 120L
private const val ENTRANCE_SCALE_FROM = 0.82f

private val SplashEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** Matches the Claude Design splash: radial blue gradient, white isotype, staggered scale+fade entrance. */
@Composable
fun SplashOverlay(modifier: Modifier = Modifier) {
    val iconProgress = remember { Animatable(0f) }
    val textProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { iconProgress.animateTo(1f, tween(ENTRANCE_DURATION_MS, easing = SplashEasing)) }
        launch {
            delay(TEXT_DELAY_MS)
            textProgress.animateTo(1f, tween(ENTRANCE_DURATION_MS, easing = SplashEasing))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tutu_isotype),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        val scale = ENTRANCE_SCALE_FROM + (1f - ENTRANCE_SCALE_FROM) * iconProgress.value
                        scaleX = scale
                        scaleY = scale
                        alpha = iconProgress.value
                    },
            )
            Text(
                text = "Tutu",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
                modifier = Modifier.graphicsLayer {
                    val scale = ENTRANCE_SCALE_FROM + (1f - ENTRANCE_SCALE_FROM) * textProgress.value
                    scaleX = scale
                    scaleY = scale
                    alpha = textProgress.value
                },
            )
        }
    }
}
