package com.workoutjournal.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.WorkoutJournalApp
import com.workoutjournal.domain.model.SessionSummary
import com.workoutjournal.ui.theme.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as WorkoutJournalApp
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(app.repository))
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateToSessionId) {
        uiState.navigateToSessionId?.let { id ->
            onSessionClick(id)
            viewModel.onNavigatedToSession()
        }
    }

    if (showDatePicker) {
        SessionDatePickerDialog(
            onConfirm = { date ->
                showDatePicker = false
                viewModel.createNewSession(date)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        floatingActionButton = {
            val fabShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .shadow(elevation = 6.dp, shape = fabShape)
                    .clip(fabShape)
                    .background(Brush.verticalGradient(colors = listOf(ButtonTop, ButtonBottom)))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("New Session", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.sessions.isEmpty() -> {
                EmptyHistoryState(Modifier.fillMaxSize().padding(paddingValues))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 88.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sessions, key = { it.id }) { session ->
                        SessionCard(session = session, onClick = { onSessionClick(session.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: SessionSummary, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient circle icon with specular highlight
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(gradStart, gradEnd),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(size.width * 0.28f, size.height * 0.22f),
                                radius = size.minDimension * 0.48f
                            ),
                            radius = size.minDimension * 0.48f,
                            center = Offset(size.width * 0.28f, size.height * 0.22f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium
                )
                val subtitle = buildString {
                    if (session.name.isNotBlank()) append("${session.name}  ·  ")
                    append("${session.exerciseCount} exercise${if (session.exerciseCount != 1) "s" else ""}")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(gradStart.copy(alpha = 0.15f), gradEnd.copy(alpha = 0.15f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No workouts yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap 'New Session' to log your first workout",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDatePickerDialog(
    onConfirm: (java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = java.time.LocalDate.now().toEpochDay() * 86_400_000L
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= todayMillis + 86_400_000L - 1L
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    onConfirm(Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
