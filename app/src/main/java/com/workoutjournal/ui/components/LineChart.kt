package com.workoutjournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workoutjournal.domain.model.ProgressPoint
import com.workoutjournal.ui.theme.GradientEnd
import com.workoutjournal.ui.theme.GradientEndDark
import com.workoutjournal.ui.theme.GradientStart
import com.workoutjournal.ui.theme.GradientStartDark
import java.time.format.DateTimeFormatter

@Composable
fun ProgressLineChart(
    points: List<ProgressPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = onSurfaceVariant)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM")

    val xValues = points.map { it.date.toEpochDay().toFloat() }
    val yValues = points.map { it.maxWeightKg }

    val xMin = xValues.min()
    val xMax = xValues.max()
    val yMin = (yValues.min() * 0.9f).coerceAtLeast(0f)
    val yMax = yValues.max() * 1.1f
    val xRange = (xMax - xMin).coerceAtLeast(1f)
    val yRange = (yMax - yMin).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val leftPadPx   = 52.dp.toPx()
        val rightPadPx  = 16.dp.toPx()
        val topPadPx    = 16.dp.toPx()
        val bottomPadPx = 40.dp.toPx()

        val chartLeft   = leftPadPx
        val chartRight  = size.width - rightPadPx
        val chartTop    = topPadPx
        val chartBottom = size.height - bottomPadPx
        val chartWidth  = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        fun xPx(x: Float) = chartLeft + (x - xMin) / xRange * chartWidth
        fun yPx(y: Float) = chartBottom - (y - yMin) / yRange * chartHeight

        // Grid lines
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = yMin + yRange * i / gridCount
            val yPixel = yPx(y)
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.15f),
                start = Offset(chartLeft, yPixel),
                end = Offset(chartRight, yPixel),
                strokeWidth = 1.dp.toPx()
            )
            val label = "%.1f".format(y)
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(0f, yPixel - measured.size.height / 2f),
                style = labelStyle
            )
        }

        // Axes
        drawLine(
            color = onSurfaceVariant.copy(alpha = 0.35f),
            start = Offset(chartLeft, chartTop),
            end = Offset(chartLeft, chartBottom),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = onSurfaceVariant.copy(alpha = 0.35f),
            start = Offset(chartLeft, chartBottom),
            end = Offset(chartRight, chartBottom),
            strokeWidth = 1.5.dp.toPx()
        )

        if (points.size > 1) {
            // Gradient fill under the line
            val fillPath = Path().apply {
                moveTo(xPx(xValues[0]), yPx(yValues[0]))
                for (i in 1 until points.size) lineTo(xPx(xValues[i]), yPx(yValues[i]))
                lineTo(xPx(xValues.last()), chartBottom)
                lineTo(xPx(xValues.first()), chartBottom)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradStart.copy(alpha = 0.40f), Color.Transparent),
                    startY = chartTop,
                    endY = chartBottom
                )
            )

            // Gradient line
            val linePath = Path().apply {
                moveTo(xPx(xValues[0]), yPx(yValues[0]))
                for (i in 1 until points.size) lineTo(xPx(xValues[i]), yPx(yValues[i]))
            }
            drawPath(
                linePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(gradStart, gradEnd),
                    startX = chartLeft,
                    endX = chartRight
                ),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Dots and x-axis labels
        val step = ((points.size / 5) + 1).coerceAtLeast(1)
        points.forEachIndexed { i, point ->
            val x = xPx(xValues[i])
            val y = yPx(yValues[i])
            // Outer white ring
            drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
            // Gradient filled dot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gradEnd, gradStart),
                    center = Offset(x, y),
                    radius = 5.dp.toPx()
                ),
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )

            if (i % step == 0 || i == points.lastIndex) {
                val label = point.date.format(dateFormatter)
                val measured = textMeasurer.measure(label, labelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(
                        (x - measured.size.width / 2f).coerceIn(chartLeft, chartRight - measured.size.width),
                        chartBottom + 6.dp.toPx()
                    ),
                    style = labelStyle
                )
            }
        }
    }
}
