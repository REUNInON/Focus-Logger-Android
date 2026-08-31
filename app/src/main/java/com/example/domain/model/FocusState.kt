package com.example.domain.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class FocusState(
    val displayName: String,
    val description: String,
    val shortcutKey: String
) {
    Idle(
        displayName = "IDLE",
        description = "Ready to start session",
        shortcutKey = "Q"
    ),
    Working(
        displayName = "WORKING",
        description = "Deep Focus / Productive Time",
        shortcutKey = "W"
    ),
    Break(
        displayName = "BREAK",
        description = "Conscious Rest & Recharge",
        shortcutKey = "B"
    ),
    Procrastination(
        displayName = "SLACKING",
        description = "Distracted / Procrastinating",
        shortcutKey = "S"
    ),
    Prompting(
        displayName = "MIND DUMP",
        description = "Quick thought capture (Do-Later)",
        shortcutKey = "P"
    );

    companion object {
        fun fromString(value: String): FocusState {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: Idle
        }
    }
}

@Composable
fun FocusState.color(): Color {
    return when (this) {
        FocusState.Idle -> MaterialTheme.colorScheme.outline
        FocusState.Working -> MaterialTheme.colorScheme.primary
        FocusState.Break -> MaterialTheme.colorScheme.secondary
        FocusState.Procrastination -> MaterialTheme.colorScheme.error
        FocusState.Prompting -> MaterialTheme.colorScheme.tertiary
    }
}
