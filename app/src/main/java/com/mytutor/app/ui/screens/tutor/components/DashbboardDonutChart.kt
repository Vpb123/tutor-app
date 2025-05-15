package com.mytutor.app.ui.screens.tutor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytutor.app.data.remote.models.CourseAnalytics
import kotlin.math.roundToInt

enum class DonutMetricType(val label: String) {
    QUIZ_PASS("Quiz Pass Rate"),
    COMPLETION("Course Completion Rate")
}

@Composable
fun DashboardDonutChart(
    analytics: List<CourseAnalytics>,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    if (analytics.isEmpty()) return

    var selectedMetric by remember { mutableStateOf(DonutMetricType.QUIZ_PASS) }

    val (label, percentage, countText) = when (selectedMetric) {
        DonutMetricType.QUIZ_PASS -> {
            val totalPassed = analytics.sumOf { it.passedQuizCount }
            val totalEnrolled = analytics.sumOf { it.enrolledCount }.coerceAtLeast(1)
            Triple(
                "Quiz Pass Rate",
                (totalPassed.toFloat() / totalEnrolled.toFloat()) * 100f,
                "$totalPassed passed out of $totalEnrolled"
            )
        }
        DonutMetricType.COMPLETION -> {
            val totalCompleted = analytics.sumOf { it.completedCount }
            val totalEnrolled = analytics.sumOf { it.enrolledCount }.coerceAtLeast(1)
            Triple(
                "Course Completion Rate",
                (totalCompleted.toFloat() / totalEnrolled.toFloat()) * 100f,
                "$totalCompleted completed out of $totalEnrolled"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutMetricType.entries.forEach { type ->
                FilterChip(
                    selected = selectedMetric == type,
                    onClick = { selectedMetric = type },
                    label = { Text(type.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(150.dp)) {
                drawArc(
                    color = backgroundColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )

                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = (360 * percentage / 100f),
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "${percentage.roundToInt()}%",
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleLarge,
                color = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = countText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
