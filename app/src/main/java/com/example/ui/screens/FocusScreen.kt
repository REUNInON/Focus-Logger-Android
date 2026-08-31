package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.util.TimeFormatter
import com.example.ui.components.GoalSection
import com.example.ui.components.MindDumpSection
import com.example.ui.components.TimerCard
import com.example.ui.components.TimerNotificationPanel
import com.example.ui.viewmodel.FocusUiState
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun FocusScreen(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGoalObj = uiState.activeGoals.find { it.id == uiState.selectedGoalId }
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.overallAnalytics.currentStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("🔥", modifier = Modifier.padding(end = 4.dp))
                                Text(
                                    "${uiState.overallAnalytics.currentStreak} Day Streak",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.isZenModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.toggleZenMode() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "禅", 
                                style = MaterialTheme.typography.titleMedium, 
                                modifier = Modifier.padding(end = 6.dp),
                                color = if (uiState.isZenModeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Zen", 
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.isZenModeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Timer Live Floating Notification Panel
                TimerNotificationPanel(
                    isSessionActive = uiState.isSessionActive && uiState.showNotificationPanel && !uiState.isZenModeEnabled,
                    currentState = uiState.currentState,
                    timerMode = uiState.timerMode,
                    pomodoroPhase = uiState.pomodoroPhase,
                    pomodoroRemainingSeconds = uiState.pomodoroRemainingSeconds,
                    blockElapsedSeconds = uiState.currentBlockElapsedSeconds,
                    totalSessionElapsedSeconds = uiState.totalSessionElapsedSeconds,
                    distractionCount = uiState.distractionCount,
                    selectedGoal = selectedGoalObj,
                    onStateChange = { viewModel.changeState(it) },
                    onLogDistraction = { 
                        viewModel.logDistraction()
                        Toast.makeText(context, "Mind Wander Logged! (Total: ${uiState.distractionCount + 1})", Toast.LENGTH_SHORT).show()
                    },
                    onEndSession = { viewModel.endSession() },
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Timer & State Control Cockpit
                TimerCard(
                    currentState = uiState.currentState,
                    isSessionActive = uiState.isSessionActive,
                    blockElapsedSeconds = uiState.currentBlockElapsedSeconds,
                    totalSessionElapsedSeconds = uiState.totalSessionElapsedSeconds,
                    distractionCount = uiState.distractionCount,
                    timerMode = uiState.timerMode,
                    pomodoroPhase = uiState.pomodoroPhase,
                    pomodoroRemainingSeconds = uiState.pomodoroRemainingSeconds,
                    pomodoroCurrentCycle = uiState.pomodoroCurrentCycle,
                    pomodoroTargetCycles = uiState.pomodoroTargetCycles,
                    pomodoroWorkMinutes = uiState.pomodoroWorkMinutes,
                    pomodoroShortBreakMinutes = uiState.pomodoroShortBreakMinutes,
                    onTimerModeChange = { viewModel.setTimerMode(it) },
                    onPomodoroPresetSelect = { work, brk -> viewModel.setPomodoroIntervals(work, brk) },
                    onCustomPomodoroClick = { viewModel.setShowPomodoroCustomDialog(true) },
                    onSkipPomodoroPhase = { viewModel.skipPomodoroPhase() },
                    onResetPomodoro = { viewModel.resetPomodoro() },
                    onStateChange = { viewModel.changeState(it) },
                    onStartSession = { viewModel.startSession(it) },
                    onEndSession = { viewModel.endSession() },
                    onLogDistraction = { 
                        viewModel.logDistraction()
                        Toast.makeText(context, "Mind Wander Logged! (Total: ${uiState.distractionCount + 1})", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Live Session Timeline (if in active session with logged blocks)
            if (uiState.isSessionActive && uiState.currentSessionTimeline.isNotEmpty()) {
                item {
                    LiveTimelinePillCard(uiState = uiState)
                }
            }

            // Active Goal Zen Display
            if (uiState.isZenModeEnabled && selectedGoalObj != null && !selectedGoalObj.isCompleted) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { viewModel.toggleGoal(selectedGoalObj) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Focus",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedGoalObj.description,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Complete",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Session Goals Section (with Selection Highlighting, Time Spent Tracking & Grouping)
            item {
                AnimatedVisibility(
                    visible = !uiState.isZenModeEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    GoalSection(
                    goals = uiState.activeGoals,
                    newGoalInput = uiState.newGoalInput,
                    onNewGoalChange = { viewModel.onNewGoalInputChange(it) },
                    onAddGoal = { viewModel.addNewGoal() },
                    onToggleGoal = { viewModel.toggleGoal(it) },
                    onCompleteTopGoal = { viewModel.completeTopGoal() },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    selectedGoalId = uiState.selectedGoalId,
                    onSelectGoal = { viewModel.selectGoal(it) },
                    onLongPressGoal = { viewModel.setGoalCategoryDialog(it) },
                    selectedGroup = uiState.selectedGoalGroup,
                    onSelectGroup = { viewModel.setSelectedGoalGroup(it) },
                    availableGroups = uiState.availableGoalGroups
                )
                }
            }

            // Mind Dump / Do Later Section (with Hide Mind Dumps Option & Grouping, [P] removed)
            item {
                AnimatedVisibility(
                    visible = !uiState.isZenModeEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    MindDumpSection(
                    tasks = uiState.deferredTasks,
                    dumpText = uiState.quickDumpText,
                    onDumpTextChange = { viewModel.onQuickDumpTextChange(it) },
                    onAddDumpTask = { viewModel.addQuickDumpTask() },
                    onToggleTask = { viewModel.toggleDeferredTask(it) },
                    onDeleteTask = { viewModel.deleteDeferredTask(it) },
                    onLongPressTask = { viewModel.setMindDumpCategoryDialog(it) },
                    hideMindDumps = uiState.hideMindDumps,
                    onToggleHideMindDumps = { viewModel.toggleHideMindDumps() },
                    selectedGroup = uiState.selectedMindDumpGroup,
                    onSelectGroup = { viewModel.setSelectedMindDumpGroup(it) },
                    availableGroups = uiState.availableMindDumpGroups
                )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        if (uiState.goalCategoryDialogFor != null) {
            CategoryAssignDialog(
                onDismiss = { viewModel.setGoalCategoryDialog(null) },
                onAssign = { 
                    viewModel.updateGoalCategory(uiState.goalCategoryDialogFor, it)
                    viewModel.setGoalCategoryDialog(null)
                }
            )
        }
        
        if (uiState.mindDumpCategoryDialogFor != null) {
            CategoryAssignDialog(
                onDismiss = { viewModel.setMindDumpCategoryDialog(null) },
                onAssign = { 
                    viewModel.updateMindDumpCategory(uiState.mindDumpCategoryDialogFor, it)
                    viewModel.setMindDumpCategoryDialog(null)
                }
            )
        }
        
        if (uiState.showPomodoroCustomDialog) {
            PomodoroCustomDialog(
                initialWork = uiState.pomodoroWorkMinutes,
                initialBreak = uiState.pomodoroShortBreakMinutes,
                initialCycles = uiState.pomodoroTargetCycles,
                onDismiss = { viewModel.setShowPomodoroCustomDialog(false) },
                onSave = { w, b, c -> 
                    viewModel.setPomodoroIntervals(workMin = w, shortBreakMin = b, targetCycles = c)
                    viewModel.setShowPomodoroCustomDialog(false)
                }
            )
        }
    }
}

@Composable
fun CategoryAssignDialog(
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    var categoryText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Category") },
        text = {
            Column {
                Text("Enter a category name for this item. If you want to remove the category, leave it blank.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { categoryText = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAssign(categoryText.trim()) }) {
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PomodoroCustomDialog(
    initialWork: Int,
    initialBreak: Int,
    initialCycles: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int) -> Unit
) {
    var workText by remember { mutableStateOf(initialWork.toString()) }
    var breakText by remember { mutableStateOf(initialBreak.toString()) }
    var cyclesText by remember { mutableStateOf(initialCycles.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Pomodoro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = workText,
                    onValueChange = { workText = it },
                    label = { Text("Work Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = breakText,
                    onValueChange = { breakText = it },
                    label = { Text("Short Break Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cyclesText,
                    onValueChange = { cyclesText = it },
                    label = { Text("Total Cycles") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val w = workText.toIntOrNull() ?: 25
                val b = breakText.toIntOrNull() ?: 5
                val c = cyclesText.toIntOrNull() ?: 4
                onSave(w, b, c)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LiveTimelinePillCard(uiState: FocusUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_timeline_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Current Session Timeline (${uiState.currentSessionTimeline.size} blocks)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uiState.currentSessionTimeline.forEach { block ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(block.state.color())
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${TimeFormatter.formatTimeOnly(block.startTime)}  ${block.state.displayName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = TimeFormatter.formatDuration(block.durationSeconds),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
