package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SessionWithDetails
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayChartData(
    val dateString: String,
    val dayLabel: String,
    val dateLabel: String,
    val workSeconds: Long,
    val breakSeconds: Long,
    val slackSeconds: Long,
    val distractionCount: Int,
    val sessionCount: Int,
    val isToday: Boolean
) {
    val totalSeconds: Long get() = workSeconds + breakSeconds + slackSeconds
    val nonWorkSeconds: Long get() = breakSeconds + slackSeconds
    val focusEfficiencyRatio: Double
        get() = if (totalSeconds > 0) (workSeconds.toDouble() / totalSeconds.toDouble()) * 100.0 else 0.0
}

@Composable
fun SevenDayBreakdownChart(
    dailySummaries: List<com.example.domain.model.DailySummary>,
    period: com.example.ui.viewmodel.AnalyticsPeriod = com.example.ui.viewmodel.AnalyticsPeriod.WEEK,
    modifier: Modifier = Modifier
) {
    val chartData = remember(dailySummaries) {
        dailySummaries.map { ds ->
            DayChartData(
                dateString = ds.dateString,
                dayLabel = ds.displayDate,
                dateLabel = ds.displayDate,
                workSeconds = ds.totalWorkSeconds,
                breakSeconds = ds.totalBreakSeconds,
                slackSeconds = ds.totalSlackSeconds,
                distractionCount = ds.totalDistractions,
                sessionCount = ds.sessionCount,
                isToday = false // simplification
            )
        }.sortedBy { it.dateString }
    }

    var selectedDayIndex by remember { mutableStateOf(chartData.size - 1) }
    val selectedDay = chartData.getOrNull(selectedDayIndex)

    val total7DayWork = chartData.sumOf { it.workSeconds }
    val total7DayNonWork = chartData.sumOf { it.nonWorkSeconds }
    val total7DayDistractions = chartData.sumOf { it.distractionCount }
    val avgDailyWorkSec = if (chartData.isNotEmpty()) total7DayWork / chartData.size else 0

    val workColor = FocusState.Working.color()
    val distractionColor = Color(0xFFF59E0B) // Amber / Distraction
    val slackColor = FocusState.Procrastination.color()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("seven_day_breakdown_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val titleText = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "7-Day Focus vs Distraction" else "4-Month Focus vs Distraction"
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily active work vs. breaks & distractions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val workLabel = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "7-Day Work" else "4-Mon Work"
                ChartKpi(workLabel, TimeFormatter.formatShortDuration(total7DayWork), workColor)
                val avgLabel = if (period == com.example.ui.viewmodel.AnalyticsPeriod.WEEK) "Daily Avg" else "Month Avg"
                ChartKpi(avgLabel, TimeFormatter.formatShortDuration(avgDailyWorkSec), MaterialTheme.colorScheme.primary)
                ChartKpi("Wanders", "$total7DayDistractions", distractionColor)
                val total7DayAll = total7DayWork + total7DayNonWork
                val overallEff = if (total7DayAll > 0) (total7DayWork.toDouble() / total7DayAll.toDouble()) * 100.0 else 0.0
                ChartKpi("Efficiency", String.format(Locale.getDefault(), "%.0f%%", overallEff), workColor)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = workColor, label = "Active Work")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = distractionColor, label = "Break / Distraction")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bar Chart Area
            val maxSeconds = maxOf(3600L, chartData.maxOfOrNull { it.totalSeconds } ?: 3600L)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.forEachIndexed { index, day ->
                    val isSelected = index == selectedDayIndex
                    val workFraction = (day.workSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
                    val nonWorkFraction = (day.nonWorkSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)

                    val animWorkFraction by animateFloatAsState(
                        targetValue = workFraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "work_anim"
                    )
                    val animNonWorkFraction by animateFloatAsState(
                        targetValue = nonWorkFraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "non_work_anim"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedDayIndex = index
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Distraction badge if present
                        if (day.distractionCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = distractionColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                Text(
                                    text = "${day.distractionCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = distractionColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Bar Column (Stacked or Dual Bar)
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 24.dp else 20.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Distraction / Break bar on top
                                if (animNonWorkFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animNonWorkFraction / (animWorkFraction + animNonWorkFraction).coerceAtLeast(0.001f))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(distractionColor.copy(alpha = 0.85f))
                                    )
                                }

                                // Work bar on bottom
                                if (animWorkFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animWorkFraction / (animWorkFraction + animNonWorkFraction).coerceAtLeast(0.001f))
                                            .clip(
                                                if (animNonWorkFraction <= 0f) RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                else RoundedCornerShape(0.dp)
                                            )
                                            .background(workColor)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // X-axis Day Label
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else if (day.isToday) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = day.dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Tooltip / Selected Day Detail Card
            selectedDay?.let { day ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${day.dateString} ${if (day.isToday) "(Today)" else ""}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%% Focus", day.focusEfficiencyRatio),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = workColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Work: ${TimeFormatter.formatShortDuration(day.workSeconds)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = workColor
                            )
                            Text(
                                text = "Break: ${TimeFormatter.formatShortDuration(day.breakSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusState.Break.color()
                            )
                            Text(
                                text = "Slacking: ${TimeFormatter.formatShortDuration(day.slackSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = slackColor
                            )
                            Text(
                                text = "${day.distractionCount} wanders",
                                style = MaterialTheme.typography.bodySmall,
                                color = distractionColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartKpi(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateSevenDaysData(sessions: List<SessionWithDetails>): List<DayChartData> {
    val result = mutableListOf<DayChartData>()
    val cal = Calendar.getInstance()

    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d/M", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val todayKey = keyFormat.format(Date())

    for (i in 6 downTo 0) {
        val targetCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -i)
        }
        val targetDate = targetCal.time
        val key = keyFormat.format(targetDate)

        // Find sessions on this day
        val daySessions = sessions.filter {
            keyFormat.format(Date(it.session.startTime)) == key
        }

        val workSec = daySessions.sumOf { it.session.totalWorkSeconds }
        val breakSec = daySessions.sumOf { it.session.totalBreakSeconds }
        val slackSec = daySessions.sumOf { it.session.totalSlackSeconds }
        val distCount = daySessions.sumOf { it.session.distractionCount }

        result.add(
            DayChartData(
                dateString = fullDateFormat.format(targetDate),
                dayLabel = if (key == todayKey) "Today" else dayFormat.format(targetDate),
                dateLabel = dateFormat.format(targetDate),
                workSeconds = workSec,
                breakSeconds = breakSec,
                slackSeconds = slackSec,
                distractionCount = distCount,
                sessionCount = daySessions.size,
                isToday = (key == todayKey)
            )
        )
    }

    return result
}
