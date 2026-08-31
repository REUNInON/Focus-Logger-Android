import re

with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'r') as f:
    content = f.read()

# Need to add imports
imports = """import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.components.SessionSummaryPane"""

if "BoxWithConstraints" not in content:
    content = content.replace("import com.example.ui.components.SessionSummaryDialog", imports + "\nimport com.example.ui.components.SessionSummaryDialog")

# Replace HistoryScreen body
# We want to keep HistorySessionCard as is.
# The body of HistoryScreen is from `val context = LocalContext.current` to the end of the `Box { ... }`.

new_body = """    val context = LocalContext.current

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
"""

# Extract the old body to replace it
match = re.search(r'    val context = LocalContext\.current\n\n    Box\(\n        modifier = modifier\.fillMaxSize\(\),\n        contentAlignment = Alignment\.TopCenter\n    \) \{[\s\S]*?        \}\n    \}\n\}', content)
if match:
    old_body = match.group(0)
    content = content.replace(old_body, new_body)
    with open('app/src/main/java/com/example/ui/screens/HistoryScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Failed to match old body")

