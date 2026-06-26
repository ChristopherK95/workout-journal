package com.workoutjournal.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workoutjournal.ui.theme.GradientEnd
import com.workoutjournal.ui.theme.GradientEndDark
import com.workoutjournal.ui.theme.GradientStart
import com.workoutjournal.ui.theme.GradientStartDark

@Composable
fun GradientTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(gradStart, gradEnd),
                            startX = 0f,
                            endX = size.width
                        )
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.55f
                        )
                    )
                }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    title()
                }
            }
            actions()
        }
    }
}
