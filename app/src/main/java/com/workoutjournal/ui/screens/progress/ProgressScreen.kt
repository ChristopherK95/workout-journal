package com.workoutjournal.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workoutjournal.domain.model.ProgressPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.WorkoutJournalApp
import com.workoutjournal.ui.components.AppTextField
import com.workoutjournal.ui.components.ProgressLineChart
import com.workoutjournal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as WorkoutJournalApp
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(app.repository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF0D0D1F),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D1F))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.exerciseNames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Log some workouts first to see progress charts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ExerciseDropdown(
                    exercises = uiState.exerciseNames,
                    selected = uiState.selectedExercise,
                    onSelect = { viewModel.selectExercise(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.progressPoints.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "No data for this exercise yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    var showVolume by rememberSaveable { mutableStateOf(false) }

                    val chartPoints = if (showVolume)
                        uiState.volumePoints.map { ProgressPoint(it.date, it.totalVolume) }
                    else
                        uiState.progressPoints
                    val chartLabel = if (showVolume) "Volume (kg)" else "Max Weight (kg)"
                    val stat1Label = if (showVolume) "Max Volume" else "Personal Best"
                    val stat1Value = if (showVolume)
                        uiState.volumePoints.maxOfOrNull { it.totalVolume }?.let { "${it.toDisplayString()} kg" } ?: "—"
                    else
                        uiState.personalBestKg?.let { "${it.toDisplayString()} kg" } ?: "—"
                    val stat2Value = if (showVolume)
                        uiState.volumePoints.lastOrNull()?.totalVolume?.let { "${it.toDisplayString()} kg" } ?: "—"
                    else
                        uiState.lastWeightKg?.let { "${it.toDisplayString()} kg" } ?: "—"

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF181830),
                        tonalElevation = 0.dp,
                        shadowElevation = 2.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    chartLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ChartTab(label = "Weight", selected = !showVolume) { showVolume = false }
                                    ChartTab(label = "Volume", selected = showVolume) { showVolume = true }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            ProgressLineChart(
                                points = chartPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = stat1Label,
                            value = stat1Value,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Last Session",
                            value = stat2Value,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Sessions",
                            value = uiState.progressPoints.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF181830),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDropdown(
    exercises: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        AppTextField(
            value = selected,
            onValueChange = {},
            label = "Exercise",
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exercises.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(name); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun ChartTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFF08FEC0).copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) Color(0xFF08FEC0) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color(0xFF08FEC0) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun Float.toDisplayString(): String =
    if (this == kotlin.math.floor(this)) toInt().toString() else "%.1f".format(this)
