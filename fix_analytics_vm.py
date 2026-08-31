import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

imports = """import com.example.ui.viewmodel.AnalyticsPeriod
import kotlinx.coroutines.flow.combine"""

if "import com.example.ui.viewmodel.AnalyticsPeriod" not in content:
    content = content.replace("import com.example.ui.viewmodel.FocusUiState", imports + "\nimport com.example.ui.viewmodel.FocusUiState")

# We have `viewModelScope.launch { repository.allSessionsWithDetails.collect { ... } }`
# We need to change that to observe a combined flow of sessions, period, and offset.
# But wait, uiState itself has period and offset? If we just use combine, it's easier to create StateFlows.
# Let's add them to the ViewModel.
props = """    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    private val _analyticsOffset = MutableStateFlow(0)
"""

content = content.replace("    private var timerJob: Job? = null", props + "\n    private var timerJob: Job? = null")

old_collect = """        // Observe sessions and calculate day-by-day & session-by-session analytics
        viewModelScope.launch {
            repository.allSessionsWithDetails.collect { sessionsWithDetails ->
                val analytics = calculateAnalytics(sessionsWithDetails)
                _uiState.update {
                    it.copy(
                        pastSessions = sessionsWithDetails,
                        overallAnalytics = analytics
                    )
                }
            }
        }"""

new_collect = """        // Observe sessions and calculate analytics based on selected period
        viewModelScope.launch {
            combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                Triple(sessions, period, offset)
            }.collect { (sessions, period, offset) ->
                val filteredSessions = filterSessionsByPeriod(sessions, period, offset)
                val analytics = calculateAnalytics(filteredSessions) // Compute analytics for the filtered range
                
                // We still want currentStreak to be global, so let's compute streak from ALL sessions
                val globalAnalytics = calculateAnalytics(sessions)
                
                _uiState.update {
                    it.copy(
                        pastSessions = sessions, // keep all for history, or just filtered?
                        overallAnalytics = analytics.copy(currentStreak = globalAnalytics.currentStreak),
                        analyticsPeriod = period,
                        analyticsOffset = offset
                    )
                }
            }
        }"""

content = content.replace(old_collect, new_collect)

# Add the filter function and setters
filter_fun = """
    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
        _analyticsOffset.value = 0
    }

    fun shiftAnalyticsOffset(delta: Int) {
        _analyticsOffset.value += delta
    }

    private fun filterSessionsByPeriod(
        sessions: List<com.example.data.entity.SessionWithDetails>,
        period: AnalyticsPeriod,
        offset: Int
    ): List<com.example.data.entity.SessionWithDetails> {
        val cal = java.util.Calendar.getInstance()
        
        when (period) {
            AnalyticsPeriod.WEEK -> {
                // start of week
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.WEEK_OF_YEAR, offset)
                val start = cal.timeInMillis
                cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                val end = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }
            AnalyticsPeriod.MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.add(java.util.Calendar.MONTH, offset)
                val start = cal.timeInMillis
                cal.add(java.util.Calendar.MONTH, 1)
                val end = cal.timeInMillis
                return sessions.filter { it.session.startTime in start until end }
            }
        }
    }
"""

content = content.replace("    private fun calculateAnalytics", filter_fun + "\n    private fun calculateAnalytics")

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)

