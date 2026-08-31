import re

with open('app/src/main/java/com/example/ui/components/SessionSummaryDialog.kt', 'r') as f:
    content = f.read()

# Replace SessionSummaryDialog with SessionSummaryPane and SessionSummaryDialog

new_dialog = """@Composable
fun SessionSummaryDialog(
    sessionWithDetails: SessionWithDetails,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SessionSummaryPane(
            sessionWithDetails = sessionWithDetails,
            onDismiss = onDismiss,
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        )
    }
}

@Composable
fun SessionSummaryPane(
    sessionWithDetails: SessionWithDetails,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session = sessionWithDetails.session
    val markdown = MarkdownExporter.generateMarkdown(
        session = session,
        timelineBlocks = sessionWithDetails.timelineBlocks,
        goals = sessionWithDetails.goals,
        deferredTasks = sessionWithDetails.deferredTasks
    )

    Card(
        modifier = modifier.testTag("session_summary_pane"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {"""

content = re.sub(
    r'@Composable\s*fun SessionSummaryDialog\([\s\S]*?Card\(\s*modifier = modifier[^,]*\n\s*\.fillMaxWidth\(0\.94f\)[^\n]*\n\s*\.fillMaxHeight\(0\.88f\)[^\n]*\n\s*\.testTag\("session_summary_dialog"\),\s*shape = RoundedCornerShape\(28\.dp\),\s*colors = CardDefaults\.cardColors\(containerColor = MaterialTheme\.colorScheme\.surface\),\s*elevation = CardDefaults\.cardElevation\(defaultElevation = 6\.dp\)\s*\)\s*\{',
    new_dialog,
    content
)

# Replace the close button to be conditionally shown
old_close = """                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }"""
new_close = """                    if (onDismiss != null) {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }"""

content = content.replace(old_close, new_close)

with open('app/src/main/java/com/example/ui/components/SessionSummaryDialog.kt', 'w') as f:
    f.write(content)
