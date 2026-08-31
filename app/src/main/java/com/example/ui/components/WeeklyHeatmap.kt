package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailySummary
import java.util.Calendar

@Composable
fun WeeklyHeatmap(dailySummaries: List<DailySummary>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Activity Heatmap",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Generate last 7 weeks * 7 days (49 days)
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -48)
            
            val days = mutableListOf<String>()
            for (i in 0..48) {
                val month = cal.get(Calendar.MONTH) + 1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val year = cal.get(Calendar.YEAR)
                days.add(String.format("%04d-%02d-%02d", year, month, day))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val summaryMap = dailySummaries.associateBy { it.dateString }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (week in 0 until 7) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val index = week * 7 + dayOfWeek
                            if (index < days.size) {
                                val dateStr = days[index]
                                val summary = summaryMap[dateStr]
                                val color = getHeatmapColor(summary?.totalWorkSeconds ?: 0L)
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(Modifier.width(2.dp))
                Box(Modifier.size(10.dp).background(Color(0xFFC8E6C9)))
                Spacer(Modifier.width(2.dp))
                Box(Modifier.size(10.dp).background(Color(0xFF81C784)))
                Spacer(Modifier.width(2.dp))
                Box(Modifier.size(10.dp).background(Color(0xFF4CAF50)))
                Spacer(Modifier.width(2.dp))
                Box(Modifier.size(10.dp).background(Color(0xFF2E7D32)))
                Spacer(Modifier.width(4.dp))
                Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun getHeatmapColor(workSeconds: Long): Color {
    val mins = workSeconds / 60
    return when {
        mins == 0L -> MaterialTheme.colorScheme.surfaceVariant
        mins < 30 -> Color(0xFFC8E6C9) // Light green
        mins < 60 -> Color(0xFF81C784)
        mins < 120 -> Color(0xFF4CAF50)
        else -> Color(0xFF2E7D32) // Dark green
    }
}
