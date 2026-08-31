package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.util.TimeFormatter
import com.example.ui.components.DayAnalysisCard
import com.example.ui.viewmodel.AnalyticsPeriod
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.IconButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.ui.components.SessionSummaryPane
import com.example.ui.components.SessionSummaryDialog
import com.example.ui.components.SevenDayBreakdownChart
import com.example.ui.viewmodel.FocusUiState
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun AnalyticsScreen(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val analytics = uiState.overallAnalytics

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isExpanded = maxWidth >= 840.dp

        if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AnalyticsListContent(uiState, viewModel, analytics, selectedTab, onTabSelected = { selectedTab = it })
                }
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.selectedSessionForDetail != null) {
                        SessionSummaryPane(
                            sessionWithDetails = uiState.selectedSessionForDetail,
                            onDismiss = { viewModel.selectSessionForDetail(null) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Select a session to view details",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                AnalyticsListContent(uiState, viewModel, analytics, selectedTab, onTabSelected = { selectedTab = it })
                
                // Details Modal for compact screens
                uiState.selectedSessionForDetail?.let { detail ->
                    SessionSummaryDialog(
                        sessionWithDetails = detail,
                        onDismiss = { viewModel.selectSessionForDetail(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsListContent(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    analytics: com.example.domain.model.OverallAnalytics,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Performance & Analysis",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Period Toggle
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.analyticsPeriod == AnalyticsPeriod.WEEK,
                        onClick = { viewModel.setAnalyticsPeriod(AnalyticsPeriod.WEEK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Weekly")
                    }
                    SegmentedButton(
                        selected = uiState.analyticsPeriod == AnalyticsPeriod.MONTH,
                        onClick = { viewModel.setAnalyticsPeriod(AnalyticsPeriod.MONTH) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Monthly")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date Range Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.shiftAnalyticsOffset(-1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    
                    val cal = Calendar.getInstance()
                    val dateLabel = if (uiState.analyticsPeriod == AnalyticsPeriod.WEEK) {
                        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                        cal.add(Calendar.WEEK_OF_YEAR, uiState.analyticsOffset)
                        val startFormat = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
                        cal.add(Calendar.DAY_OF_YEAR, 6)
                        val endFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
                        "$startFormat - $endFormat"
                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.add(Calendar.MONTH, uiState.analyticsOffset)
                        val endM = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                        cal.add(Calendar.MONTH, -3)
                        val startM = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                        "$startM - $endM"
                    }
                    
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = { viewModel.shiftAnalyticsOffset(1) },
                        enabled = uiState.analyticsOffset < 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = if (uiState.analyticsOffset < 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // 7-Day Interactive Breakdown Chart (Work vs Distractions)
            item {
                SevenDayBreakdownChart(dailySummaries = analytics.dailySummaries, period = uiState.analyticsPeriod)
            }

            // Overview Metric Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Work Time",
                            value = TimeFormatter.formatShortDuration(analytics.totalWorkSeconds),
                            subtitle = "Across all sessions",
                            color = FocusState.Working.color(),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Overall Efficiency",
                            value = String.format(java.util.Locale.getDefault(), "%.1f%%", analytics.overallEfficiency),
                            subtitle = "Work vs break/slack ratio",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Sessions",
                            value = "${analytics.totalSessions}",
                            subtitle = "Total logged",
                            color = FocusState.Break.color(),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Distractions",
                            value = "${analytics.totalDistractions}",
                            subtitle = "Mind wanders",
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // View Selector Tabs: Day by Day vs Session by Session
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { onTabSelected(0) },
                        text = { Text("Day by Day (${analytics.dailySummaries.size} days)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text("Session by Session (${uiState.pastSessions.size})", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Day by Day View
                if (analytics.dailySummaries.isEmpty()) {
                    item {
                        EmptyAnalyticsPlaceholder()
                    }
                } else {
                    items(analytics.dailySummaries, key = { it.dateString }) { dailySummary ->
                        DayAnalysisCard(
                            summary = dailySummary,
                            onSelectSession = { viewModel.selectSessionForDetail(it) }
                        )
                    }
                }
            } else {
                // Session by Session View
                if (uiState.pastSessions.isEmpty()) {
                    item {
                        EmptyAnalyticsPlaceholder()
                    }
                } else {
                    items(uiState.pastSessions, key = { it.session.id }) { sessionWithDetails ->
                        val s = sessionWithDetails.session
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            onClick = { viewModel.selectSessionForDetail(sessionWithDetails) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${TimeFormatter.formatDisplayDate(s.startTime)} • ${TimeFormatter.formatTimeOnly(s.startTime)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.getDefault(), "%.1f%% Efficiency", s.focusEfficiencyRatio),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Work: ${TimeFormatter.formatShortDuration(s.totalWorkSeconds)} | Break: ${TimeFormatter.formatShortDuration(s.totalBreakSeconds)} | Slacking: ${TimeFormatter.formatShortDuration(s.totalSlackSeconds)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EmptyAnalyticsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No analytics data yet. Start focus sessions to see trends!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
