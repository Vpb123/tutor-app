package com.mytutor.app.ui.screens.tutor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mytutor.app.data.remote.models.CourseAnalytics

@Composable
fun DashboardChart(
    analytics: List<CourseAnalytics>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    maxBarWidth: Dp = 250.dp
) {
    if (analytics.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
//            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "📊 Enrolled Students per Course",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        analytics.forEach { course ->
            CourseBarItem(
                title = course.courseTitle,
                value = course.enrolledCount,
                max = analytics.maxOfOrNull { it.enrolledCount } ?: 1,
                color = barColor,
                maxBarWidth = maxBarWidth
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CourseBarItem(
    title: String,
    value: Int,
    max: Int,
    color: Color,
    maxBarWidth: Dp
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val barWidthFraction = value.toFloat() / max.toFloat()
        val actualBarWidth = maxBarWidth * barWidthFraction

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            Canvas(
                modifier = Modifier
                    .width(actualBarWidth)
                    .fillMaxHeight()
            ) {
                drawRoundRect(
                    color = color,
                    size = size,
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }

        Text(
            text = "$value enrolled",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
