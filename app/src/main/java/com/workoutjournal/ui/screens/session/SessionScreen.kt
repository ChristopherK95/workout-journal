package com.workoutjournal.ui.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workoutjournal.WorkoutJournalApp
import com.workoutjournal.ui.components.AppTextField
import com.workoutjournal.ui.components.GradientTopAppBar
import com.workoutjournal.ui.components.ToolsMenu
import com.workoutjournal.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    sessionId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as WorkoutJournalApp
    val viewModel: SessionViewModel = viewModel(
        key = "session_$sessionId",
        factory = SessionViewModel.Factory(app.repository, sessionId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.sessionDeleted) {
        if (uiState.sessionDeleted) onBack()
    }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showDeleteSessionDialog by remember { mutableStateOf(false) }
    var editingSet by remember { mutableStateOf<SetUi?>(null) }

    if (showAddExerciseDialog) {
        AddExerciseDialog(
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ToolsMenu()
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
        if (uiState.exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
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
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 88.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
private fun SetRow(set: SetUi, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
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
    if (this == kotlin.math.floor(this)) toInt().toString() else "%.1f".format(this)

@Composable
private fun AddExerciseDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(ButtonTop, ButtonBottom))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        DumbbellIcon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    onClick = { if (text.isNotBlank()) onAdd(text.trim()) },
                    enabled = text.isNotBlank()
                ) { Text("Add") }
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(ButtonTop, ButtonBottom))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        DumbbellIcon,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
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
                        onSave(weightText.toFloatOrNull() ?: 0f, repsText.toIntOrNull() ?: 0)
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
                    onSave(weightText.toFloatOrNull() ?: 0f, repsText.toIntOrNull() ?: 0)
                }) { Text("Save") }
            }
        }
    }
}
