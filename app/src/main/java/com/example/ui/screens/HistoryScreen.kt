package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SessionWithDetails
import com.example.domain.model.FocusState
import com.example.domain.model.color
import com.example.domain.util.MarkdownExporter
import com.example.domain.util.TimeFormatter
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.components.SessionSummaryPane
import com.example.ui.components.SessionSummaryDialog
import com.example.ui.viewmodel.FocusUiState
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun HistoryScreen(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isExpanded = maxWidth >= 840.dp

        if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    HistoryListContent(uiState, viewModel, context)
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
                HistoryListContent(uiState, viewModel, context)
                
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
private fun HistoryListContent(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    context: Context
) {
    if (uiState.pastSessions.isEmpty()) {
        // Empty State
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Saved Sessions Yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Complete your first focus session, upload PC Markdown logs, or load sample sessions to preview timeline logs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { viewModel.showMarkdownImportDialog(true) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Markdown")
                }

                Button(
                    onClick = { viewModel.populateSampleData() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Load Sample")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session History (${uiState.pastSessions.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilledTonalButton(
                        onClick = { viewModel.showMarkdownImportDialog(true) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_import_log_history")
                    ) {
                        Icon(imageVector = Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Log", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Text(
                    text = "Tap any session to view its detailed timeline, goals, and export as Markdown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                )
            }

            items(uiState.pastSessions, key = { it.session.id }) { sessionWithDetails ->
                HistorySessionCard(
                    sessionWithDetails = sessionWithDetails,
                    onOpenDetail = { viewModel.selectSessionForDetail(sessionWithDetails) },
                    onShare = {
                        val md = MarkdownExporter.generateMarkdown(
                            sessionWithDetails.session,
                            sessionWithDetails.timelineBlocks,
                            sessionWithDetails.goals,
                            sessionWithDetails.deferredTasks
                        )
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, md)
                            putExtra(Intent.EXTRA_TITLE, "Focus Session Log")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Markdown"))
                    },
                    onDelete = { viewModel.deleteSession(sessionWithDetails.session.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    sessionWithDetails: SessionWithDetails,
    onOpenDetail: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val session = sessionWithDetails.session

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("history_session_${session.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = TimeFormatter.formatDisplayDate(session.startTime),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${TimeFormatter.formatTimeOnly(session.startTime)} - ${TimeFormatter.formatTimeOnly(session.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%.1f%% Focus", session.focusEfficiencyRatio),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration Proportion Bar
            val totalSec = session.totalDurationSeconds
            if (totalSec > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                ) {
                    val workW = session.totalWorkSeconds.toFloat() / totalSec.toFloat()
                    val breakW = session.totalBreakSeconds.toFloat() / totalSec.toFloat()
                    val slackW = session.totalSlackSeconds.toFloat() / totalSec.toFloat()

                    if (workW > 0f) Box(modifier = Modifier.weight(workW).fillMaxHeight().background(FocusState.Working.color()))
                    if (breakW > 0f) Box(modifier = Modifier.weight(breakW).fillMaxHeight().background(FocusState.Break.color()))
                    if (slackW > 0f) Box(modifier = Modifier.weight(slackW).fillMaxHeight().background(FocusState.Procrastination.color()))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Work", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(TimeFormatter.formatShortDuration(session.totalWorkSeconds), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = FocusState.Working.color())
                    }
                    Column {
                        Text("Break", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(TimeFormatter.formatShortDuration(session.totalBreakSeconds), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = FocusState.Break.color())
                    }
                    Column {
                        Text("Slack", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(TimeFormatter.formatShortDuration(session.totalSlackSeconds), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = FocusState.Procrastination.color())
                    }
                    Column {
                        Text("Wanders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${session.distractionCount}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF59E0B))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
