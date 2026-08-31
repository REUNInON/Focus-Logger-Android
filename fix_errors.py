import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    vm = f.read()

# Fix 'pomodoroBreakMinutes' -> 'pomodoroShortBreakMinutes'
vm = vm.replace("pomodoroBreakMinutes = savedBreakMin", "pomodoroShortBreakMinutes = savedBreakMin")

# Fix combine
old_combine = """        viewModelScope.launch {
            combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                Triple(sessions, period, offset)
            }.collect { (sessions, period, offset) ->"""

new_combine = """        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                Triple(sessions, period, offset)
            }.collect { (sessions, period, offset) ->"""

vm = vm.replace(old_combine, new_combine)

# Fix keyFormat unresolved reference
# Move keyFormat definition outside the if-else so it's accessible.
old_keyFormat = """        // Pre-fill days
        val cal = java.util.Calendar.getInstance()
        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        if (period == AnalyticsPeriod.WEEK) {"""

new_keyFormat = """        val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        // Pre-fill days
        val cal = java.util.Calendar.getInstance()
        
        if (period == AnalyticsPeriod.WEEK) {"""

vm = vm.replace(old_keyFormat, new_keyFormat)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(vm)

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'r') as f:
    screen = f.read()

# Fix androidx unresolved reference
screen = screen.replace("androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)", "Modifier") # Will just remove clip, wait, let's use proper imports or standard clip. 
# wait, replacing with "clip(CircleShape)" requires import. Let's just use "Modifier" and drop clip since it's wrapped in Box.
# Actually Box has no background by default, so we can just do Modifier.background(..., shape = CircleShape)
old_box = """                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                        .androidx.compose.foundation.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {"""

new_box = """                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {"""

screen = screen.replace(old_box, new_box)
screen = screen.replace("androidx.compose.material.icons.Icons.Default.Check", "androidx.compose.material.icons.Icons.Default.Check")

with open('app/src/main/java/com/example/ui/screens/FocusScreen.kt', 'w') as f:
    f.write(screen)
