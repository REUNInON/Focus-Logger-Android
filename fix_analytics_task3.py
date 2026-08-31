with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""                val completedBlock = LiveBlockItem(
                    state = currentState,
                    durationSeconds = elapsedSeconds,
                    startTime = blockStart
                )""",
"""                val completedBlock = LiveBlockItem(
                    state = currentState,
                    durationSeconds = elapsedSeconds,
                    startTime = blockStart,
                    relatedGoalId = _uiState.value.selectedGoalId
                )""")

content = content.replace(
"""            TimelineBlockEntity(
                sessionId = 0,
                state = live.state.name,
                startTime = live.startTime,
                durationSeconds = live.durationSeconds,
                orderIndex = index
            )""",
"""            TimelineBlockEntity(
                sessionId = 0,
                state = live.state.name,
                startTime = live.startTime,
                durationSeconds = live.durationSeconds,
                orderIndex = index,
                relatedGoalId = live.relatedGoalId
            )""")

content = content.replace('if (block.blockType == "FOCUS"', 'if (block.state == FocusState.Working.name')

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)
