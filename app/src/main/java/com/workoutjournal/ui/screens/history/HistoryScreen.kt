package com.workoutjournal.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.WorkoutJournalApp
import com.workoutjournal.domain.model.SessionSummary
import com.workoutjournal.domain.model.WorkoutTemplate
import com.workoutjournal.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as WorkoutJournalApp
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(app.repository))
    val uiState by viewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTemplateDatePicker by remember { mutableStateOf(false) }
    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }

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

    if (showTemplateDatePicker && selectedTemplateId != null) {
        val templateId = selectedTemplateId!!
        SessionDatePickerDialog(
            onConfirm = { date ->
                showTemplateDatePicker = false
                selectedTemplateId = null
                viewModel.createSessionFromTemplate(date, templateId)
            },
            onDismiss = {
                showTemplateDatePicker = false
                selectedTemplateId = null
            }
        )
    }

    if (showTemplatesSheet) {
        TemplatesSheet(
            templates = uiState.templates,
            onUseTemplate = { templateId ->
                showTemplatesSheet = false
                selectedTemplateId = templateId
                showTemplateDatePicker = true
            },
            onDeleteTemplate = { viewModel.deleteTemplate(it) },
            onDismiss = { showTemplatesSheet = false }
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
            else -> {
                val sessionDates = remember(uiState.sessions) {
                    uiState.sessions.map { it.date }.toHashSet()
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(paddingValues.calculateTopPadding() + 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.currentStreak > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFF7043).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFFFF7043).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "🔥 ${uiState.currentStreak}-week streak",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF7043)
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF08FEC0).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF08FEC0).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .clickable { showTemplatesSheet = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val count = uiState.templates.size
                            Text(
                                if (count > 0) "📋 Templates ($count)" else "📋 Templates",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF08FEC0)
                            )
                        }
                    }

                    if (uiState.sessions.isEmpty()) {
                        EmptyHistoryState(Modifier.fillMaxWidth().weight(1f))
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181830)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            WorkoutCalendar(
                                sessionDates = sessionDates,
                                today = today,
                                onDayClick = { date ->
                                    val index = uiState.sessions.indexOfFirst { it.date == date }
                                    if (index >= 0) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            state = listState,
                            contentPadding = PaddingValues(
                                top = 8.dp,
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
    }
}

@Composable
private fun SessionCard(session: SessionSummary, onClick: () -> Unit) {
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
            Text("💪", fontSize = 28.sp)

            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium
                )
                val subtitle = buildString {
                    if (session.name.isNotBlank()) append("${session.name}  ·  ")
                    append("${session.exerciseCount} exercise${if (session.exerciseCount != 1) "s" else ""}")
                    session.durationSeconds?.let { append("  ·  ${formatDuration(it)}") }
                    if (session.notes.isNotBlank()) append("  📝")
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💪", fontSize = 64.sp)
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
private fun TemplatesSheet(
    templates: List<WorkoutTemplate>,
    onUseTemplate: (Long) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Workout Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Save sessions as templates using the 🔖 icon in a session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            if (templates.isEmpty()) {
                Text(
                    "No templates saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                templates.forEach { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(template.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${template.exerciseCount} exercise${if (template.exerciseCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onUseTemplate(template.id) }) { Text("Use") }
                        IconButton(onClick = { onDeleteTemplate(template.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete template",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun WorkoutCalendar(
    sessionDates: Set<LocalDate>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    var displayMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { displayMonth = displayMonth.minusMonths(1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                displayMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            IconButton(
                onClick = { displayMonth = displayMonth.plusMonths(1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        val startOffset = displayMonth.dayOfWeek.value - 1
        val daysInMonth = displayMonth.lengthOfMonth()
        val rows = (startOffset + daysInMonth + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayOfMonth = row * 7 + col - startOffset + 1
                    val date = if (dayOfMonth in 1..daysInMonth)
                        displayMonth.withDayOfMonth(dayOfMonth) else null
                    val hasSession = date != null && sessionDates.contains(date)
                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(if (hasSession) Modifier.clickable { onDayClick(date!!) } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            if (hasSession) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF08FEC0).copy(alpha = 0.18f))
                                )
                            }
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color(0xFF08FEC0), CircleShape)
                                )
                            }
                            Text(
                                text = dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (hasSession || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    hasSession -> Color(0xFF08FEC0)
                                    isToday   -> Color.White
                                    else      -> Color.White.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m"
        else  -> "< 1m"
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
