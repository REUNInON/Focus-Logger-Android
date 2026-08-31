import re

# 1. Update FocusUiState
with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'r') as f:
    content = f.read()

if "isZenModeEnabled: Boolean = false" not in content:
    content = content.replace("val hideMindDumps: Boolean = false,", "val hideMindDumps: Boolean = false,\n    val isZenModeEnabled: Boolean = false,")

with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'w') as f:
    f.write(content)

# 2. Update FocusViewModel for Zen Mode and SharedPreferences
with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    vm_content = f.read()

# Let's add DataStore / SharedPreferences init
prefs_init = """    private val prefs = application.getSharedPreferences("FocusSettings", android.content.Context.MODE_PRIVATE)

    init {
        // Load settings
        val savedThemePreset = ThemePreset.valueOf(prefs.getString("theme_preset", ThemePreset.MIDNIGHT_OBSIDIAN.name) ?: ThemePreset.MIDNIGHT_OBSIDIAN.name)
        val savedThemeMode = ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        val savedTimerMode = TimerMode.valueOf(prefs.getString("timer_mode", TimerMode.STOPWATCH.name) ?: TimerMode.STOPWATCH.name)
        val savedWorkMin = prefs.getInt("pomodoro_work", 25)
        val savedBreakMin = prefs.getInt("pomodoro_break", 5)

        _uiState.update { it.copy(
            themePreset = savedThemePreset,
            themeMode = savedThemeMode,
            timerMode = savedTimerMode,
            pomodoroWorkMinutes = savedWorkMin,
            pomodoroBreakMinutes = savedBreakMin
        ) }
"""

vm_content = vm_content.replace("    init {", prefs_init)

# Replace setters to save to SharedPreferences
vm_content = vm_content.replace(
"""    fun setThemePreset(themePreset: ThemePreset) {
        _uiState.update { it.copy(themePreset = themePreset) }
    }""",
"""    fun setThemePreset(themePreset: ThemePreset) {
        prefs.edit().putString("theme_preset", themePreset.name).apply()
        _uiState.update { it.copy(themePreset = themePreset) }
    }""")

vm_content = vm_content.replace(
"""    fun setThemeMode(themeMode: ThemeMode) {
        _uiState.update { it.copy(themeMode = themeMode) }
    }""",
"""    fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
        _uiState.update { it.copy(themeMode = themeMode) }
    }""")

vm_content = vm_content.replace(
"""    fun setTimerMode(mode: TimerMode) {
        _uiState.update { it.copy(timerMode = mode) }
    }""",
"""    fun setTimerMode(mode: TimerMode) {
        prefs.edit().putString("timer_mode", mode.name).apply()
        _uiState.update { it.copy(timerMode = mode) }
    }""")

vm_content = vm_content.replace(
"""    fun updatePomodoroSettings(workMinutes: Int, breakMinutes: Int) {
        _uiState.update {
            it.copy(
                pomodoroWorkMinutes = workMinutes,
                pomodoroBreakMinutes = breakMinutes
            )
        }
    }""",
"""    fun updatePomodoroSettings(workMinutes: Int, breakMinutes: Int) {
        prefs.edit().putInt("pomodoro_work", workMinutes).putInt("pomodoro_break", breakMinutes).apply()
        _uiState.update {
            it.copy(
                pomodoroWorkMinutes = workMinutes,
                pomodoroBreakMinutes = breakMinutes
            )
        }
    }""")

# Add Zen Mode toggle
vm_content = vm_content.replace(
"""    fun toggleHideMindDumps() {
        _uiState.update { it.copy(hideMindDumps = !it.hideMindDumps) }
    }""",
"""    fun toggleHideMindDumps() {
        _uiState.update { it.copy(hideMindDumps = !it.hideMindDumps) }
    }

    fun toggleZenMode() {
        _uiState.update { it.copy(isZenModeEnabled = !it.isZenModeEnabled) }
    }""")

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(vm_content)
