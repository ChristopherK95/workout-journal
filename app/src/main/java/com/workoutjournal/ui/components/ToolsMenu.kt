package com.workoutjournal.ui.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.ui.theme.ButtonBottom
import com.workoutjournal.ui.theme.ButtonTop
import com.workoutjournal.ui.theme.GradientEnd
import com.workoutjournal.ui.theme.GradientEndDark
import com.workoutjournal.ui.theme.GradientStart
import com.workoutjournal.ui.theme.GradientStartDark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ToolsMenu(timerVm: TimerViewModel = viewModel()) {
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

    var expanded by remember { mutableStateOf(false) }
    var showAngle by remember { mutableStateOf(false) }
    var showPlateCalc by remember { mutableStateOf(false) }

    if (showAngle) {
        AngleMeasurementDialog(
            gradStart = gradStart,
            gradEnd = gradEnd,
            onDismiss = { showAngle = false }
        )
    }

    if (showPlateCalc) {
        PlateCalculatorDialog(onDismiss = { showPlateCalc = false })
    }

    Box {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text("🔧", fontSize = 16.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text("Rest Timer")
                        if (timerVm.running || timerVm.seconds > 0) {
                            Text(
                                text = "%02d:%02d".format(timerVm.seconds / 60, timerVm.seconds % 60),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                leadingIcon = { Text("⏱️", fontSize = 20.sp) },
                onClick = { expanded = false; timerVm.openDialog() }
            )
            DropdownMenuItem(
                text = { Text("Bench Angle") },
                leadingIcon = { Text("📐", fontSize = 20.sp) },
                onClick = { expanded = false; showAngle = true }
            )
            DropdownMenuItem(
                text = { Text("Plate Calculator") },
                leadingIcon = { Text("🏋️", fontSize = 20.sp) },
                onClick = { expanded = false; showPlateCalc = true }
            )
        }
    }
}

@Composable
internal fun RestTimerDialog(
    seconds: Int,
    running: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0D0D1F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181830))
                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rest Timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text("⏱️", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "%02d:%02d".format(seconds / 60, seconds % 60),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (running) Color(0xFF08FEC0) else Color.White
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.verticalGradient(colors = listOf(ButtonTop, ButtonBottom)))
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (running) "Pause" else "Start",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
private fun AngleMeasurementDialog(
    gradStart: Color,
    gradEnd: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var angle by remember { mutableStateOf(0f) }
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE)
                as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val g = sqrt(x * x + y * y + z * z)
                if (g > 0f) {
                    angle = Math.toDegrees(
                        acos((abs(z) / g).toDouble().coerceIn(0.0, 1.0))
                    ).toFloat()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0D0D1F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181830))
                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bench Angle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text("📐", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val r = size.minDimension / 2 - 8.dp.toPx()

                    drawCircle(
                        color = onSurfaceVariant.copy(alpha = 0.2f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.2f),
                        start = Offset(cx - r, cy),
                        end = Offset(cx + r, cy),
                        strokeWidth = 1.dp.toPx()
                    )
                    val rad = Math.toRadians(angle.toDouble())
                    val lineR = r * 0.78f
                    val sx = cx - (lineR * cos(rad)).toFloat()
                    val sy = cy + (lineR * sin(rad)).toFloat()
                    val ex = cx + (lineR * cos(rad)).toFloat()
                    val ey = cy - (lineR * sin(rad)).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(gradStart, gradEnd),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey)
                        ),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(gradEnd, gradStart),
                            center = Offset(cx, cy),
                            radius = 6.dp.toPx()
                        ),
                        radius = 6.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "%.1f°".format(angle),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF08FEC0)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Place phone face-up on the bench",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlateCalculatorDialog(onDismiss: () -> Unit) {
    var targetInput by remember { mutableStateOf("") }
    var barKg by remember { mutableStateOf(20f) }

    val targetKg = targetInput.toFloatOrNull() ?: 0f
    val plates = remember(targetKg, barKg) { calcPlatesPerSide(targetKg, barKg) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0D0D1F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181830))
                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Plate Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text("🏋️", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { new ->
                        if (new.isEmpty() || new.matches(Regex("\\d*\\.?\\d*"))) targetInput = new
                    },
                    label = { Text("Target weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bar weight",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10f, 15f, 20f).forEach { kg ->
                            BarWeightChip(
                                label = "${kg.toInt()} kg",
                                selected = barKg == kg,
                                onClick = { barKg = kg }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                when {
                    targetKg <= 0f -> Text(
                        "Enter a target weight above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    targetKg < barKg -> Text(
                        "Target is lighter than the bar (${barKg.toInt()} kg)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    plates.isEmpty() -> Text(
                        "Bar only — ${barKg.toInt()} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF08FEC0)
                    )
                    else -> {
                        val totalLoaded = barKg + plates.sum() * 2
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Per side:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                plates.groupBy { it }.forEach { (kg, list) ->
                                    PlateChip(kg = kg, count = list.size)
                                }
                            }
                            if (abs(totalLoaded - targetKg) > 0.01f) {
                                Text(
                                    "Closest loadable: ${"%.2f".format(totalLoaded)} kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

@Composable
private fun BarWeightChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF08FEC0).copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) Color(0xFF08FEC0) else Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF08FEC0) else Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PlateChip(kg: Float, count: Int) {
    val color = when {
        kg >= 20f  -> Color(0xFF42A5F5)
        kg >= 15f  -> Color(0xFFFFEE58)
        kg >= 10f  -> Color(0xFF66BB6A)
        kg >= 5f   -> Color(0xFFBDBDBD)
        else       -> Color(0xFFFF7043)
    }
    val label = buildString {
        if (count > 1) append("${count}×")
        append(if (kg % 1f == 0f) kg.toInt().toString() else kg.toString())
        append(" kg")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun calcPlatesPerSide(targetKg: Float, barKg: Float): List<Float> {
    if (targetKg <= barKg) return emptyList()
    val sizes = listOf(20f, 15f, 10f, 5f, 2.5f)
    var remaining = ((targetKg - barKg) / 2f * 100).roundToInt()
    val plates = mutableListOf<Float>()
    for (plate in sizes) {
        val units = (plate * 100).roundToInt()
        while (remaining >= units) {
            plates.add(plate)
            remaining -= units
        }
    }
    return plates
}
