import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime, durationSeconds = workSec / 2, orderIndex = 0),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Break.name, startTime = startTime + (workSec / 2) * 1000L, durationSeconds = breakSec, orderIndex = 1),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime + (workSec / 2 + breakSec) * 1000L, durationSeconds = workSec / 2, orderIndex = 2),""",
"""                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime, durationSeconds = workSec / 2, orderIndex = 0, relatedGoalId = 1L),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Break.name, startTime = startTime + (workSec / 2) * 1000L, durationSeconds = breakSec, orderIndex = 1),
                    TimelineBlockEntity(sessionId = 0, state = FocusState.Working.name, startTime = startTime + (workSec / 2 + breakSec) * 1000L, durationSeconds = workSec / 2, orderIndex = 2, relatedGoalId = 1L),""")

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
