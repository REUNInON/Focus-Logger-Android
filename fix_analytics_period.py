import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

# I will add an Enum for AnalyticsPeriod to FocusUiState.kt first
with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'r') as f:
    uistate = f.read()

enum_def = """enum class AnalyticsPeriod { WEEK, MONTH }

data class LiveBlockItem("""

uistate = uistate.replace("data class LiveBlockItem(", enum_def)

if "analyticsPeriod: AnalyticsPeriod" not in uistate:
    uistate = uistate.replace(
        "val overallAnalytics: OverallAnalytics = OverallAnalytics(),",
        "val overallAnalytics: OverallAnalytics = OverallAnalytics(),\n    val analyticsPeriod: AnalyticsPeriod = AnalyticsPeriod.WEEK,\n    val analyticsOffset: Int = 0,"
    )

with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'w') as f:
    f.write(uistate)

