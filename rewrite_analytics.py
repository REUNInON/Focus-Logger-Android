import re

with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.ui.components.SessionSummaryPane"""

if "BoxWithConstraints" not in content:
    content = content.replace("import com.example.ui.components.SessionSummaryDialog", imports + "\nimport com.example.ui.components.SessionSummaryDialog")

new_body = """    var selectedTab by remember { mutableIntStateOf(0) }
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
"""

match = re.search(r'    var selectedTab by remember \{ mutableIntStateOf\(0\) \}\n    val analytics = uiState\.overallAnalytics\n\n    Box\(\n        modifier = modifier\.fillMaxSize\(\),\n        contentAlignment = Alignment\.TopCenter\n    \) \{\n        LazyColumn\(', content)
if match:
    old_start = match.group(0)
    content = content.replace(old_start, new_body)
    
    # We also need to replace the end of the Box in AnalyticsScreen
    # It currently ends with:
    #         // Details Modal if session selected
    #         uiState.selectedSessionForDetail?.let { detail ->
    #             SessionSummaryDialog(
    #                 sessionWithDetails = detail,
    #                 onDismiss = { viewModel.selectSessionForDetail(null) }
    #             )
    #         }
    #     }
    # }
    # 
    # @Composable
    # private fun MetricCard(
    
    end_pattern = r'        // Details Modal if session selected\n        uiState\.selectedSessionForDetail\?\.let \{ detail ->\n            SessionSummaryDialog\(\n                sessionWithDetails = detail,\n                onDismiss = \{ viewModel\.selectSessionForDetail\(null\) \}\n            \)\n        \}\n    \}\n\}\n\n@Composable\nprivate fun MetricCard\('
    
    match_end = re.search(end_pattern, content)
    if match_end:
        new_end = """        }
    }
}

@Composable
private fun MetricCard("""
        content = content.replace(match_end.group(0), new_end)
        
        # Replace selectedTab = 0 with onTabSelected(0) etc in AnalyticsListContent
        content = content.replace("selectedTab = 0", "onTabSelected(0)")
        content = content.replace("selectedTab = 1", "onTabSelected(1)")
        
        with open('app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt', 'w') as f:
            f.write(content)
        print("Success")
    else:
        print("Failed to match end")
else:
    print("Failed to match start")

