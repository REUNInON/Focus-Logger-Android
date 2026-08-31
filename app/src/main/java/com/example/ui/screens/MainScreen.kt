package com.example.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.MarkdownImportDialog
import com.example.ui.components.SessionSummaryDialog
import com.example.ui.viewmodel.FocusUiState
import com.example.ui.viewmodel.FocusViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Focus("Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    History("History", Icons.Filled.History, Icons.Outlined.History),
    Analytics("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights),
    Settings("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    var backPressedOnce by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }

    BackHandler {
        if (selectedTabIndex != 0) {
            selectedTabIndex = 0
        } else {
            if (backPressedOnce) {
                (context as? Activity)?.moveTaskToBack(true)
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit (Timer will keep running)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val tabs = MainTab.entries

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Layout for Tablets / Foldables / Wide Screens (Navigation Rail)
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        Text(
                            text = "Focus",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationRailItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            icon = {
                                Icon(
                                    imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                        )
                    }
                }

                // Main Content for Wide Screen
                ScreenContent(
                    selectedTabIndex = selectedTabIndex,
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Standard Layout for Mobile Screens (Bottom Navigation Bar)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar"),
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                ScreenContent(
                    selectedTabIndex = selectedTabIndex,
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }

        // Just Finished Session Dialog (Shows automatically on session completion with Markdown Export)
        uiState.justFinishedSession?.let { finishedSession ->
            SessionSummaryDialog(
                sessionWithDetails = finishedSession,
                onDismiss = { viewModel.dismissFinishedSessionDialog() }
            )
        }

        // Markdown Import Dialog (For PC log uploads or pasting)
        if (uiState.showMarkdownImportDialog) {
            MarkdownImportDialog(
                onDismiss = { viewModel.showMarkdownImportDialog(false) },
                onImportConfirmed = { parsedSession ->
                    viewModel.importMarkdownSession(parsedSession)
                }
            )
        }
    }
}

@Composable
private fun ScreenContent(
    selectedTabIndex: Int,
    uiState: FocusUiState,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    when (selectedTabIndex) {
        0 -> FocusScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
        1 -> HistoryScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
        2 -> AnalyticsScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
        3 -> SettingsScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
    }
}
