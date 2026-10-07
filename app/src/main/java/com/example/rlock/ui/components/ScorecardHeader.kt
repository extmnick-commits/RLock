package com.example.rlock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rlock.model.MetricGoal
import com.example.rlock.ui.theme.CrimsonAccent
import com.example.rlock.ui.theme.CrimsonBorder
import com.example.rlock.ui.theme.CrimsonGaze
import com.example.rlock.ui.theme.CrimsonTextPrimary
import com.example.rlock.ui.theme.CrimsonTextSecondary
import com.example.rlock.ui.theme.GlassCrimson
import com.example.rlock.ui.theme.GlassCrimsonHighlight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScorecardHeader(
    metrics: List<MetricGoal>,
    onIncrementMetric: (String) -> Unit,
    onDecrementMetric: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Text(
            text = "DAILY SCORECARD GOALS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = CrimsonTextSecondary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            metrics.forEach { metric ->
                MetricCard(
                    metric = metric,
                    onIncrement = { onIncrementMetric(metric.id) },
                    onDecrement = { onDecrementMetric(metric.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MetricCard(
    metric: MetricGoal,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassCrimson,
        border = BorderStroke(1.dp, CrimsonBorder),
        modifier = Modifier.combinedClickable(
            onClick = onIncrement,
            onLongClick = onDecrement
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = metric.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = CrimsonTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularGauge(
                    current = metric.current,
                    target = metric.target
                )

                // +1 Glass Pill Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlassCrimsonHighlight,
                    border = BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.combinedClickable(
                        onClick = onIncrement,
                        onLongClick = onDecrement
                    )
                ) {
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularGauge(
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier.size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.5.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track Ring
            drawArc(
                color = Color(0x22FF5252),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = CrimsonGaze,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CrimsonTextPrimary
            )
            Text(
                text = "/$target",
                style = MaterialTheme.typography.labelSmall,
                color = CrimsonTextSecondary
            )
        }
    }
}
