package com.example.domain.util

import com.example.data.entity.DeferredTaskEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.GoalEntity
import com.example.data.entity.TimelineBlockEntity
import com.example.domain.model.FocusState
import java.text.SimpleDateFormat
import java.util.Locale

data class ParsedMarkdownSession(
    val session: FocusSessionEntity,
    val timelineBlocks: List<TimelineBlockEntity>,
    val goals: List<GoalEntity>,
    val deferredTasks: List<DeferredTaskEntity>,
    val rawText: String
)

object MarkdownImporter {

    private val dateTimeFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
        SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    )

    fun parseMarkdown(content: String): Result<ParsedMarkdownSession> {
        return try {
            val lines = content.lines()
            var startTime: Long = System.currentTimeMillis() - 3600_000L
            var endTime: Long = System.currentTimeMillis()

            var totalWorkSec: Long = 0
            var totalBreakSec: Long = 0
            var totalSlackSec: Long = 0
            var distractionCount: Int = 0

            val goals = mutableListOf<GoalEntity>()
            val deferredTasks = mutableListOf<DeferredTaskEntity>()
            val timelineBlocks = mutableListOf<TimelineBlockEntity>()

            var currentSection = ""

            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                // Check section headers
                if (line.startsWith("#")) {
                    currentSection = line.lowercase(Locale.getDefault())
                    continue
                }

                // Header dates
                if (line.startsWith("**Start Time:**", ignoreCase = true) || line.startsWith("Start Time:", ignoreCase = true)) {
                    val dateStr = line.substringAfter(":").replace("*", "").trim()
                    parseDate(dateStr)?.let { startTime = it }
                    continue
                }
                if (line.startsWith("**End Time:**", ignoreCase = true) || line.startsWith("End Time:", ignoreCase = true)) {
                    val dateStr = line.substringAfter(":").replace("*", "").trim()
                    parseDate(dateStr)?.let { endTime = it }
                    continue
                }

                // Summary items
                if (currentSection.contains("summary") || line.contains("Total Work", ignoreCase = true) || line.contains("Total Break", ignoreCase = true)) {
                    if (line.contains("Total Work", ignoreCase = true)) {
                        totalWorkSec = parseDurationToSeconds(line.substringAfter(":").replace("*", "").trim())
                    } else if (line.contains("Total Break", ignoreCase = true)) {
                        totalBreakSec = parseDurationToSeconds(line.substringAfter(":").replace("*", "").trim())
                    } else if (line.contains("Total Slack", ignoreCase = true) || line.contains("Total Procrastination", ignoreCase = true)) {
                        totalSlackSec = parseDurationToSeconds(line.substringAfter(":").replace("*", "").trim())
                    } else if (line.contains("Mind Wander", ignoreCase = true) || line.contains("Distraction", ignoreCase = true)) {
                        val numStr = line.substringAfter(":").replace("*", "").trim().filter { it.isDigit() }
                        distractionCount = numStr.toIntOrNull() ?: 0
                    }
                }

                // Goals section
                if (currentSection.contains("goal") && (line.startsWith("- [x]") || line.startsWith("- [ ]") || line.startsWith("- "))) {
                    val isCompleted = line.startsWith("- [x]", ignoreCase = true)
                    var desc = line.removePrefix("- [x]").removePrefix("- [X]").removePrefix("- [ ]").removePrefix("- ").trim()
                    var completedAtSec: Long? = null

                    if (desc.contains("(Completed at", ignoreCase = true)) {
                        val compTimeStr = desc.substringAfter("(Completed at").substringBefore(")").trim()
                        completedAtSec = parseDurationToSeconds(compTimeStr)
                        desc = desc.substringBefore("(Completed at").trim()
                    }

                    if (desc.isNotBlank()) {
                        goals.add(
                            GoalEntity(
                                description = desc,
                                isCompleted = isCompleted,
                                isCarriedOver = false,
                                completedAtSessionSeconds = completedAtSec,
                                createdAtTimestamp = startTime
                            )
                        )
                    }
                    continue
                }

                // Timeline section: e.g. - [14:30] **Working**: 25m 00s
                if (currentSection.contains("timeline") && line.startsWith("-")) {
                    val timelineLine = line.removePrefix("-").trim()
                    val state = when {
                        timelineLine.contains("Work", ignoreCase = true) -> FocusState.Working
                        timelineLine.contains("Break", ignoreCase = true) -> FocusState.Break
                        timelineLine.contains("Slack", ignoreCase = true) || timelineLine.contains("Procrastin", ignoreCase = true) -> FocusState.Procrastination
                        else -> FocusState.Working
                    }

                    val durStr = timelineLine.substringAfter(":").trim()
                    val durSec = parseDurationToSeconds(durStr)

                    timelineBlocks.add(
                        TimelineBlockEntity(
                            sessionId = 0,
                            state = state.name,
                            startTime = startTime + (timelineBlocks.size * 60_000L),
                            durationSeconds = durSec,
                            orderIndex = timelineBlocks.size
                        )
                    )
                    continue
                }

                // Deferred tasks (Do later list)
                if ((currentSection.contains("deferred") || currentSection.contains("distraction") || currentSection.contains("do later") || currentSection.contains("mind dump")) && line.startsWith("-")) {
                    val isCompleted = line.startsWith("- [x]", ignoreCase = true)
                    val text = line.removePrefix("- [x]").removePrefix("- [X]").removePrefix("- [ ]").removePrefix("- ").trim()
                    if (text.isNotBlank()) {
                        deferredTasks.add(
                            DeferredTaskEntity(
                                text = text,
                                isCompleted = isCompleted,
                                createdAtTimestamp = startTime
                            )
                        )
                    }
                }
            }

            // If summary totals were 0 but timeline blocks exist, calculate from timeline
            if (totalWorkSec == 0L && timelineBlocks.isNotEmpty()) {
                totalWorkSec = timelineBlocks.filter { it.state == FocusState.Working.name }.sumOf { it.durationSeconds }
                totalBreakSec = timelineBlocks.filter { it.state == FocusState.Break.name }.sumOf { it.durationSeconds }
                totalSlackSec = timelineBlocks.filter { it.state == FocusState.Procrastination.name }.sumOf { it.durationSeconds }
            }

            val completedGoalCount = goals.count { it.isCompleted }
            val pendingGoalCount = goals.count { !it.isCompleted }

            val sessionEntity = FocusSessionEntity(
                startTime = startTime,
                endTime = endTime,
                totalWorkSeconds = totalWorkSec,
                totalBreakSeconds = totalBreakSec,
                totalSlackSeconds = totalSlackSec,
                distractionCount = distractionCount,
                completedGoalCount = completedGoalCount,
                pendingGoalCount = pendingGoalCount
            )

            Result.success(
                ParsedMarkdownSession(
                    session = sessionEntity,
                    timelineBlocks = timelineBlocks,
                    goals = goals,
                    deferredTasks = deferredTasks,
                    rawText = content
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDate(str: String): Long? {
        for (format in dateTimeFormats) {
            try {
                val date = format.parse(str)
                if (date != null) return date.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseDurationToSeconds(durStr: String): Long {
        var totalSec = 0L
        val clean = durStr.lowercase(Locale.getDefault())

        // Check for formats like "01h 30m 15s" or "25m 00s" or "01:30:15" or "25:00"
        if (clean.contains("h") || clean.contains("m") || clean.contains("s")) {
            val hMatch = Regex("(\\d+)\\s*h").find(clean)
            val mMatch = Regex("(\\d+)\\s*m").find(clean)
            val sMatch = Regex("(\\d+)\\s*s").find(clean)

            val hours = hMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val minutes = mMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val seconds = sMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L

            totalSec = (hours * 3600L) + (minutes * 60L) + seconds
        } else if (clean.contains(":")) {
            val parts = clean.split(":").mapNotNull { it.trim().toLongOrNull() }
            when (parts.size) {
                3 -> totalSec = (parts[0] * 3600L) + (parts[1] * 60L) + parts[2]
                2 -> totalSec = (parts[0] * 60L) + parts[1]
                1 -> totalSec = parts[0]
            }
        } else {
            totalSec = clean.filter { it.isDigit() }.toLongOrNull() ?: 0L
        }
        return totalSec
    }
}
