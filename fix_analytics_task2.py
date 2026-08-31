with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'r') as f:
    content = f.read()

content = content.replace("    val startTime: Long\n)", "    val startTime: Long,\n    val relatedGoalId: Long? = null\n)")

with open('app/src/main/java/com/example/ui/viewmodel/FocusUiState.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/entity/TimelineBlockEntity.kt', 'r') as f:
    content = f.read()

content = content.replace("    val durationSeconds: Long,\n    val orderIndex: Int\n)", "    val durationSeconds: Long,\n    val orderIndex: Int,\n    val relatedGoalId: Long? = null\n)")

with open('app/src/main/java/com/example/data/entity/TimelineBlockEntity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/database/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace("version = 2", "version = 3")

with open('app/src/main/java/com/example/data/database/AppDatabase.kt', 'w') as f:
    f.write(content)
