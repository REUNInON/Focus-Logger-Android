import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    vm = f.read()

old_combine = """        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                Triple(sessions, period, offset)
            }.collect { (sessions, period, offset) ->
                val filteredSessions = filterSessionsByPeriod(sessions, period, offset)"""

new_combine = """        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                repository.allSessionsWithDetails,
                _analyticsPeriod,
                _analyticsOffset
            ) { sessions, period, offset ->
                com.example.ui.viewmodel.AnalyticsData(sessions, period, offset)
            }.collect { data ->
                val sessions = data.sessions
                val period = data.period
                val offset = data.offset
                val filteredSessions = filterSessionsByPeriod(sessions, period, offset)"""

vm = vm.replace(old_combine, new_combine)
vm += "\ndata class AnalyticsData(val sessions: List<com.example.data.entity.SessionWithDetails>, val period: AnalyticsPeriod, val offset: Int)\n"

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(vm)

