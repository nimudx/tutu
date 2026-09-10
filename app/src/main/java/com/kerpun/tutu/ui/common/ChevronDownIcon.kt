package com.kerpun.tutu.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ChevronDownIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(11.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.40625f)
            lineTo(size.width * 0.5f, size.height * 0.65625f)
            lineTo(size.width * 0.75f, size.height * 0.40625f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
