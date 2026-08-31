package com.example.domain.util

import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.TimelineBlockEntity
import com.example.domain.model.FocusState

object MarkdownExporter {

    fun generateMarkdown(
        session: FocusSessionEntity,
        timelineBlocks: List<TimelineBlockEntity>,
        goals: List<GoalEntity>,
        deferredTasks: List<DeferredTaskEntity>
    ): String {
        val sb = StringBuilder()

        sb.append("# Focus Session Log\n\n")
        sb.append("**Start Time:** ").append(TimeFormatter.formatDateTime(session.startTime)).append("\n")
        sb.append("**End Time:**   ").append(TimeFormatter.formatDateTime(session.endTime)).append("\n\n")

        // Goals section
        sb.append("## Goals\n\n")
        val completedGoals = goals.filter { it.isCompleted }
        val pendingGoals = goals.filter { !it.isCompleted }

        if (completedGoals.isEmpty() && pendingGoals.isEmpty()) {
            sb.append("_No goals logged for this session._\n\n")
        } else {
            for (goal in completedGoals) {
                val completionTimeStr = goal.completedAtSessionSeconds?.let {
                    " (Completed at ${TimeFormatter.formatDuration(it)})"
                } ?: ""
                sb.append("- [x] ").append(goal.description).append(completionTimeStr).append("\n")
            }
            for (goal in pendingGoals) {
                sb.append("- [ ] ").append(goal.description).append("\n")
            }
            sb.append("\n")
        }

        // Summary section
        sb.append("## Summary\n\n")
        sb.append("- **Total Work:** ").append(TimeFormatter.formatDuration(session.totalWorkSeconds)).append("\n")
        sb.append("- **Total Break:** ").append(TimeFormatter.formatDuration(session.totalBreakSeconds)).append("\n")
        sb.append("- **Total Slacking:** ").append(TimeFormatter.formatDuration(session.totalSlackSeconds)).append("\n")
        sb.append("- **Mind Wanders (Passive):** ").append(session.distractionCount).append("\n")
        sb.append("- **Focus Efficiency:** ").append(String.format(java.util.Locale.getDefault(), "%.1f%%", session.focusEfficiencyRatio)).append("\n\n")

        // Timeline section
        sb.append("## Timeline\n\n")
        val validBlocks = timelineBlocks.filter { it.durationSeconds > 0 && it.state != FocusState.Idle.name }
        if (validBlocks.isEmpty()) {
            sb.append("_No timeline blocks recorded._\n\n")
        } else {
            for (block in validBlocks) {
                val stateEnum = FocusState.fromString(block.state)
                val blockTime = TimeFormatter.formatTimeOnly(block.startTime)
                val durStr = TimeFormatter.formatDuration(block.durationSeconds)
                sb.append("- [").append(blockTime).append("] **")
                    .append(stateEnum.displayName).append("**: ")
                    .append(durStr).append("\n")
            }
            sb.append("\n")
        }

        // Deferred Tasks (Do Later List)
        if (deferredTasks.isNotEmpty()) {
            sb.append("## Deferred Tasks (Distractions)\n\n")
            for (task in deferredTasks) {
                val check = if (task.isCompleted) "- [x] " else "- [ ] "
                sb.append(check).append(task.text).append("\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }
}
