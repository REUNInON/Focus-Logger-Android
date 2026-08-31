import re

with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'r') as f:
    content = f.read()

# We look for:
#                 // Passive Distraction (Mind Wander) Pill
#                 Surface(
# ...
#                     }
#                 }
# And we wrap it.

old_wander = """                // Passive Distraction (Mind Wander) Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    onClick = onLogDistraction,
                    modifier = Modifier.testTag("distraction_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = "Mind wander",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$distractionCount Wanders (+1)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }"""

new_wander = """                // Passive Distraction (Mind Wander) Pill
                androidx.compose.animation.AnimatedVisibility(visible = currentState == FocusState.Working) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        onClick = onLogDistraction,
                        modifier = Modifier.testTag("distraction_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = "Mind wander",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$distractionCount Wanders (+1)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }"""

if old_wander in content:
    content = content.replace(old_wander, new_wander)
else:
    print("WARNING: Could not find old wander pill block in TimerCard.kt")

with open('app/src/main/java/com/example/ui/components/TimerCard.kt', 'w') as f:
    f.write(content)

