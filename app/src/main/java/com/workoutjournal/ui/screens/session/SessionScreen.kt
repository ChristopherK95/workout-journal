package com.workoutjournal.ui.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.WorkoutJournalApp
import com.workoutjournal.ui.components.AppTextField
import com.workoutjournal.ui.components.GradientTopAppBar
import com.workoutjournal.ui.components.TimerViewModel
import com.workoutjournal.ui.components.ToolsMenu
import com.workoutjournal.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    sessionId: Long,
    onBack: () -> Unit,
    timerVm: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as WorkoutJournalApp
    val viewModel: SessionViewModel = viewModel(
        key = "session_$sessionId",
        factory = SessionViewModel.Factory(app.repository, sessionId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val exerciseNames by viewModel.exerciseNames.collectAsState()

    LaunchedEffect(uiState.sessionDeleted) {
        if (uiState.sessionDeleted) onBack()
    }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showDeleteSessionDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var editingSet by remember { mutableStateOf<SetUi?>(null) }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            suggestions = exerciseNames,
            onAdd = { name ->
                viewModel.addExercise(name)
                showAddExerciseDialog = false
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    editingSet?.let { set ->
        EditSetDialog(
            set = set,
            onSave = { weight, reps ->
                viewModel.updateSet(set.id, weight, reps)
                editingSet = null
            },
            onDismiss = { editingSet = null }
        )
    }

    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            initialName = uiState.name,
            onSave = { name ->
                viewModel.saveAsTemplate(name)
                showSaveTemplateDialog = false
            },
            onDismiss = { showSaveTemplateDialog = false }
        )
    }

    if (showDeleteSessionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSessionDialog = false },
            title = { Text("Delete Session") },
            text = { Text("This will permanently delete this workout session and all its data.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteSession(); showDeleteSessionDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSessionDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            GradientTopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.name.isNotBlank()) {
                            Text(
                                uiState.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.endSession(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.exercises.isNotEmpty()) {
                        IconButton(onClick = { showSaveTemplateDialog = true }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Save as template")
                        }
                    }
                    ToolsMenu(timerVm)
                    IconButton(onClick = { showDeleteSessionDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete session")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add exercise")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 88.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "notes") {
                NotesSection(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.updateNotes(it) },
                    onSaveNotes = { viewModel.saveNotes() }
                )
            }
            if (uiState.exercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No exercises yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap + to add your first exercise",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            items(uiState.exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onAddSet = { viewModel.addSet(exercise.id) },
                    onEditSet = { set -> editingSet = set },
                    onDuplicateSet = { setId -> viewModel.duplicateSet(setId) },
                    onDeleteSet = { setId -> viewModel.deleteSet(setId) },
                    onDeleteExercise = { viewModel.deleteExercise(exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(notes.isNotBlank()) {
        if (notes.isNotBlank()) expanded = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181830)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📝", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Session Notes", style = MaterialTheme.typography.titleSmall)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    AppTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        label = "How did the session go?",
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) onSaveNotes() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseUi,
    onAddSet: () -> Unit,
    onEditSet: (SetUi) -> Unit,
    onDuplicateSet: (Long) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDeleteExercise: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradStart = if (isDark) GradientStartDark else GradientStart
    val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181830)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = listOf(gradStart, gradEnd))
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                    exercise.previousBest?.let { (weight, reps) ->
                        Text(
                            "Previous best: ${weight.toDisplayString()} kg × $reps reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    exercise.estimatedOneRepMax?.let { oneRM ->
                        Text(
                            "~${"%.1f".format(oneRM)} kg est. 1RM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF08FEC0)
                        )
                    }
                }
                IconButton(onClick = onDeleteExercise) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete exercise",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            if (exercise.sets.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(4.dp))
                exercise.sets.forEach { set ->
                    SetRow(
                        set = set,
                        isPr = exercise.allTimeBest?.let { (bestWeight, bestReps) ->
                            set.weightKg > 0f && set.reps > 0 &&
                                set.weightKg >= bestWeight && set.reps >= bestReps
                        } == true,
                        onEdit = { onEditSet(set) },
                        onDuplicate = { onDuplicateSet(set.id) },
                        onDelete = { onDeleteSet(set.id) }
                    )
                }
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Set")
            }
        }
    }
}

@Composable
private fun SetRow(set: SetUi, isPr: Boolean, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Set ${set.setNumber}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp)
        )
        Text(
            text = buildSetSummary(set),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        if (isPr) {
            Text(
                "PR",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF08FEC0),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .border(1.dp, Color(0xFF08FEC0).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit set", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate set", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete set",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
        }
    }
}

private fun buildSetSummary(set: SetUi): String {
    val weight = if (set.weightKg <= 0f) "—" else "${set.weightKg.toDisplayString()} kg"
    val reps = if (set.reps <= 0) "—" else "${set.reps} reps"
    return "$weight × $reps"
}

private fun Float.toDisplayString(): String =
    if (this == kotlin.math.floor(this)) toInt().toString()
    else String.format(java.util.Locale.US, "%.1f", this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseDialog(
    suggestions: List<String>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    val filtered = remember(text, suggestions) {
        if (text.isBlank()) emptyList()
        else suggestions
            .filter { it.contains(text.trim(), ignoreCase = true) }
            .sortedWith(compareBy({ !it.startsWith(text.trim(), ignoreCase = true) }, { it }))
            .take(5)
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
                    text = "Add Exercise",
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = filtered.isNotEmpty(),
                    onExpandedChange = {}
                ) {
                    AppTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = "Exercise name",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (text.isNotBlank()) onAdd(text.trim())
                        }),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = filtered.isNotEmpty(),
                        onDismissRequest = {}
                    ) {
                        filtered.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = { onAdd(suggestion) }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = { if (text.isNotBlank()) onAdd(text.trim()) },
                    enabled = text.isNotBlank()
                ) { Text("Add") }
            }
        }
    }
}

@Composable
private fun SaveTemplateDialog(
    initialName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

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
                    text = "Save as Template",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text("📋", fontSize = 22.sp)
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Template name",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) onSave(name)
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = { if (name.isNotBlank()) onSave(name) },
                    enabled = name.isNotBlank()
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun EditSetDialog(set: SetUi, onSave: (Float, Int) -> Unit, onDismiss: () -> Unit) {
    var weightText by remember(set.id) {
        mutableStateOf(if (set.weightKg <= 0f) "" else set.weightKg.toDisplayString())
    }
    var repsText by remember(set.id) {
        mutableStateOf(if (set.reps <= 0) "" else set.reps.toString())
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
                    text = "Set ${set.setNumber}",
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight (kg)",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = "Reps",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onSave(
                            weightText.trim().replace(',', '.').toFloatOrNull() ?: set.weightKg,
                            repsText.trim().toIntOrNull() ?: set.reps
                        )
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    onSave(
                        weightText.trim().replace(',', '.').toFloatOrNull() ?: set.weightKg,
                        repsText.trim().toIntOrNull() ?: set.reps
                    )
                }) { Text("Save") }
            }
        }
    }
}
