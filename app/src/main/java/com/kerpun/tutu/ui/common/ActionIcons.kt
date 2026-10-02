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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EditPencilIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val sx = size.width / 16f
        val sy = size.height / 16f
        val path = Path().apply {
            moveTo(10.5f * sx, 2.8f * sy)
            lineTo(13.2f * sx, 5.5f * sy)
            lineTo(5.7f * sx, 13.0f * sy)
            lineTo(3f * sx, 13.0f * sy)
            lineTo(3f * sx, 10.0f * sy)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun ChevronRightIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(11.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.375f, size.height * 0.21875f)
            lineTo(size.width * 0.65625f, size.height * 0.5f)
            lineTo(size.width * 0.375f, size.height * 0.78125f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun CheckmarkIcon(color: Color, modifier: Modifier = Modifier, iconSize: Dp = 15.dp, strokeWidth: Dp = 2.2.dp) {
    Canvas(modifier = modifier.size(iconSize)) {
        val path = Path().apply {
            moveTo(size.width * 0.1875f, size.height * 0.53125f)
            lineTo(size.width * 0.3875f, size.height * 0.73125f)
            lineTo(size.width * 0.8125f, size.height * 0.3125f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun TrashIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val sx = size.width / 16f
        val sy = size.height / 16f
        val path = Path().apply {
            moveTo(3.2f * sx, 4.6f * sy)
            lineTo(12.8f * sx, 4.6f * sy)

            moveTo(6.4f * sx, 4.6f * sy)
            lineTo(6.4f * sx, 3.2f * sy)
            lineTo(9.6f * sx, 3.2f * sy)
            lineTo(9.6f * sx, 4.6f * sy)

            moveTo(4.6f * sx, 4.6f * sy)
            lineTo(5.2f * sx, 12.6f * sy)
            lineTo(10.8f * sx, 12.6f * sy)
            lineTo(11.4f * sx, 4.6f * sy)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
