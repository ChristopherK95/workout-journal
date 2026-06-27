package com.workoutjournal.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DumbbellIcon: ImageVector by lazy {
    ImageVector.Builder("Dumbbell", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(6f, 10.5f); lineTo(18f, 10.5f); lineTo(18f, 13.5f); lineTo(6f, 13.5f); close()
        }
        path(fill = SolidColor(Color(0xFF7D59FA))) {
            moveTo(2f, 7.5f); lineTo(5.5f, 7.5f); lineTo(5.5f, 16.5f); lineTo(2f, 16.5f); close()
        }
        path(fill = SolidColor(Color(0xFF7D59FA))) {
            moveTo(18.5f, 7.5f); lineTo(22f, 7.5f); lineTo(22f, 16.5f); lineTo(18.5f, 16.5f); close()
        }
        path(fill = SolidColor(Color(0xFF5B3AEF))) {
            moveTo(2f, 10.5f); lineTo(5.5f, 10.5f); lineTo(5.5f, 13.5f); lineTo(2f, 13.5f); close()
        }
        path(fill = SolidColor(Color(0xFF5B3AEF))) {
            moveTo(18.5f, 10.5f); lineTo(22f, 10.5f); lineTo(22f, 13.5f); lineTo(18.5f, 13.5f); close()
        }
    }.build()
}

val StopwatchIcon: ImageVector by lazy {
    ImageVector.Builder("Stopwatch", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF3B82F6))) {
            moveTo(12f, 6f); arcTo(8f, 8f, 0f, false, true, 12f, 22f); arcTo(8f, 8f, 0f, false, true, 12f, 6f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(10f, 2f); lineTo(14f, 2f); lineTo(14f, 4f); lineTo(10f, 4f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(11f, 4f); lineTo(13f, 4f); lineTo(13f, 6f); lineTo(11f, 6f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(12f, 14f); lineTo(15.5f, 9f); lineTo(13f, 14f); close()
        }
        path(fill = SolidColor(Color(0xFF1E40AF))) {
            moveTo(12f, 12.5f); arcTo(1.5f, 1.5f, 0f, false, true, 12f, 15.5f); arcTo(1.5f, 1.5f, 0f, false, true, 12f, 12.5f); close()
        }
    }.build()
}

val CompassIcon: ImageVector by lazy {
    ImageVector.Builder("Compass", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF7D59FA)), pathFillType = PathFillType.EvenOdd) {
            moveTo(12f, 1.5f); arcTo(10.5f, 10.5f, 0f, false, true, 12f, 22.5f); arcTo(10.5f, 10.5f, 0f, false, true, 12f, 1.5f); close()
            moveTo(12f, 4.5f); arcTo(7.5f, 7.5f, 0f, false, true, 12f, 19.5f); arcTo(7.5f, 7.5f, 0f, false, true, 12f, 4.5f); close()
        }
        path(fill = SolidColor(Color(0xFFFF6B6B))) {
            moveTo(12f, 5f); lineTo(14f, 12f); lineTo(12f, 11f); lineTo(10f, 12f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(12f, 19f); lineTo(14f, 12f); lineTo(12f, 13f); lineTo(10f, 12f); close()
        }
        path(fill = SolidColor(Color(0xFFD4D4F0))) {
            moveTo(12f, 11f); arcTo(1f, 1f, 0f, false, true, 12f, 13f); arcTo(1f, 1f, 0f, false, true, 12f, 11f); close()
        }
    }.build()
}

val HammerIcon: ImageVector by lazy {
    ImageVector.Builder("Hammer", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF7D59FA))) {
            moveTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 10f); lineTo(5f, 10f); close()
        }
        path(fill = SolidColor(Color(0xFF5B3AEF))) {
            moveTo(5f, 7f); lineTo(19f, 7f); lineTo(19f, 10f); lineTo(5f, 10f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(10.5f, 10f); lineTo(13.5f, 10f); lineTo(13.5f, 22f); lineTo(10.5f, 22f); close()
        }
    }.build()
}

val BarChartIcon: ImageVector by lazy {
    ImageVector.Builder("BarChart", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF7D59FA))) {
            moveTo(2f, 5f); lineTo(8f, 5f); lineTo(8f, 21f); lineTo(2f, 21f); close()
        }
        path(fill = SolidColor(Color(0xFF08FEC0))) {
            moveTo(9f, 10f); lineTo(15f, 10f); lineTo(15f, 21f); lineTo(9f, 21f); close()
        }
        path(fill = SolidColor(Color(0xFF3B82F6))) {
            moveTo(16f, 14f); lineTo(22f, 14f); lineTo(22f, 21f); lineTo(16f, 21f); close()
        }
    }.build()
}
