import re

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'r') as f:
    content = f.read()

old_loop = """        for (item in sessions) {
            val s = item.session
            totalWork += s.totalWorkSeconds
            totalBreak += s.totalBreakSeconds
            totalSlack += s.totalSlackSeconds
            totalDistractions += s.distractionCount
            totalGoals += s.completedGoalCount

            val dateKey = TimeFormatter.formatDateOnly(s.startTime)"""

new_loop = """        val overallTimePerTask = mutableMapOf<String, Long>()
        for (item in sessions) {
            val s = item.session
            totalWork += s.totalWorkSeconds
            totalBreak += s.totalBreakSeconds
            totalSlack += s.totalSlackSeconds
            totalDistractions += s.distractionCount
            totalGoals += s.completedGoalCount
            
            val goalMap = item.goals.associateBy { it.id }
            for (block in item.timelineBlocks) {
                if (block.blockType == "FOCUS" && block.relatedGoalId != null) {
                    val goalName = goalMap[block.relatedGoalId]?.title ?: "Unknown Task"
                    overallTimePerTask[goalName] = overallTimePerTask.getOrDefault(goalName, 0L) + block.durationSeconds
                }
            }

            val dateKey = TimeFormatter.formatDateOnly(s.startTime)"""

content = content.replace(old_loop, new_loop)

old_daily_loop = """            for (sessionWithDetails in list) {
                val s = sessionWithDetails.session
                dWork += s.totalWorkSeconds
                dBreak += s.totalBreakSeconds
                dSlack += s.totalSlackSeconds
                dDistractions += s.distractionCount
                dGoals += s.completedGoalCount
            }

            val firstTimestamp = list.firstOrNull()?.session?.startTime ?: System.currentTimeMillis()"""

new_daily_loop = """            val dailyTimePerTask = mutableMapOf<String, Long>()
            for (sessionWithDetails in list) {
                val s = sessionWithDetails.session
                dWork += s.totalWorkSeconds
                dBreak += s.totalBreakSeconds
                dSlack += s.totalSlackSeconds
                dDistractions += s.distractionCount
                dGoals += s.completedGoalCount
                
                val goalMap = sessionWithDetails.goals.associateBy { it.id }
                for (block in sessionWithDetails.timelineBlocks) {
                    if (block.blockType == "FOCUS" && block.relatedGoalId != null) {
                        val goalName = goalMap[block.relatedGoalId]?.title ?: "Unknown Task"
                        dailyTimePerTask[goalName] = dailyTimePerTask.getOrDefault(goalName, 0L) + block.durationSeconds
                    }
                }
            }

            val firstTimestamp = list.firstOrNull()?.session?.startTime ?: System.currentTimeMillis()"""

content = content.replace(old_daily_loop, new_daily_loop)

old_daily_ret = """                completedGoalsCount = dGoals,
                sessions = list
            )
        }"""

new_daily_ret = """                completedGoalsCount = dGoals,
                sessions = list,
                timePerTask = dailyTimePerTask
            )
        }"""

content = content.replace(old_daily_ret, new_daily_ret)

old_overall_ret = """            overallEfficiency = overallEfficiency,
            currentStreak = currentStreak,
            dailySummaries = dailySummaries
        )
    }"""

new_overall_ret = """            overallEfficiency = overallEfficiency,
            currentStreak = currentStreak,
            dailySummaries = dailySummaries,
            overallTimePerTask = overallTimePerTask
        )
    }"""

content = content.replace(old_overall_ret, new_overall_ret)

with open('app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt', 'w') as f:
    f.write(content)

