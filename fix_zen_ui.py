import re

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    content = f.read()

# Replace the top Row with Streak and Zen Mode
old_streak = """                if (uiState.overallAnalytics.currentStreak > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }"""

new_streak_and_zen = """                Row(
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
                Spacer(modifier = Modifier.height(8.dp))"""

content = content.replace(old_streak, new_streak_and_zen)

# Active Goal in Zen Mode - insert before GoalSection item
old_goal_section = """            // Session Goals Section (with Selection Highlighting, Time Spent Tracking & Grouping)
            item {
                AnimatedVisibility(
                    visible = uiState.currentState != FocusState.Working && uiState.currentState != FocusState.Break,"""

new_goal_section = """            // Active Goal Zen Display
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
                                        .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                        .androidx.compose.foundation.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
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
                    visible = !uiState.isZenModeEnabled,"""

content = content.replace(old_goal_section, new_goal_section)

# Same for MindDumpSection
old_mind_dump = """            // Mind Dump / Do Later Section (with Hide Mind Dumps Option & Grouping, [P] removed)
            item {
                AnimatedVisibility(
                    visible = uiState.currentState != FocusState.Working && uiState.currentState != FocusState.Break,"""

new_mind_dump = """            // Mind Dump / Do Later Section (with Hide Mind Dumps Option & Grouping, [P] removed)
            item {
                AnimatedVisibility(
                    visible = !uiState.isZenModeEnabled,"""

content = content.replace(old_mind_dump, new_mind_dump)

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(content)
