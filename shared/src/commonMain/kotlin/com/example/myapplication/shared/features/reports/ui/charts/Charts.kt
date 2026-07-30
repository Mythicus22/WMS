package com.example.myapplication.shared.features.reports.ui.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.shared.features.reports.model.*

// ---------------------------------------------------------------------------
// CHART CONTAINER (empty + loading states)
// ---------------------------------------------------------------------------
@Composable
fun ChartCard(
    title: String,
    subtitle: String = "",
    isLoading: Boolean = false,
    isEmpty: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(12.dp))
            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
                isEmpty -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("No data available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
                else -> content()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// LINE CHART — multi-series with animation
// ---------------------------------------------------------------------------
@Composable
fun LineChart(series: List<ChartSeries>, modifier: Modifier = Modifier.fillMaxWidth().height(180.dp)) {
    if (series.isEmpty() || series.all { it.points.isEmpty() }) return

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(durationMillis = 800, easing = EaseOut))

    val allPoints = series.flatMap { it.points }
    val maxVal = allPoints.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val minVal = allPoints.minOf { it.value }
    val range = (maxVal - minVal).takeIf { it > 0f } ?: 1f
    val surfaceVar = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val padL = 40.dp.toPx(); val padB = 24.dp.toPx(); val padT = 8.dp.toPx()
        val chartW = w - padL; val chartH = h - padB - padT

        // Grid
        repeat(4) { i ->
            val y = padT + chartH * (1f - i / 3f)
            drawLine(surfaceVar, Offset(padL, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }

        series.forEach { s ->
            if (s.points.isEmpty()) return@forEach
            val color = Color(s.color)
            val n = s.points.size
            val path = Path()
            s.points.forEachIndexed { i, pt ->
                val x = padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartW
                val y = padT + chartH * (1f - ((pt.value - minVal) / range))
                val animX = padL + (x - padL) * animProgress
                if (i == 0) path.moveTo(animX, y) else path.lineTo(animX, y)
            }
            drawPath(path, color = color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // Dots
            s.points.forEachIndexed { i, pt ->
                val x = padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartW * animProgress
                val y = padT + chartH * (1f - ((pt.value - minVal) / range))
                drawCircle(color, 4.dp.toPx(), Offset(padL + (x - padL + (i.toFloat() / (n - 1).coerceAtLeast(1)) * chartW * animProgress - padL), y))
            }
        }
    }
    // Legend
    if (series.size > 1) {
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            series.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Color(s.color), CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(s.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BAR CHART — single or grouped
// ---------------------------------------------------------------------------
@Composable
fun BarChart(series: List<ChartSeries>, modifier: Modifier = Modifier.fillMaxWidth().height(180.dp)) {
    if (series.isEmpty() || series.all { it.points.isEmpty() }) return

    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(durationMillis = 700, easing = EaseOut))
    val maxVal = series.flatMap { it.points }.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
    val labelCount = series.firstOrNull()?.points?.size ?: 0

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val padL = 40.dp.toPx(); val padB = 28.dp.toPx(); val padT = 8.dp.toPx()
        val chartW = w - padL; val chartH = h - padB - padT
        val groupW = if (labelCount > 0) chartW / labelCount else chartW
        val barW = (groupW / (series.size + 1)).coerceAtMost(28.dp.toPx())

        repeat(4) { i ->
            val y = padT + chartH * (1f - i / 3f)
            drawLine(outline, Offset(padL, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }

        series.forEachIndexed { si, s ->
            s.points.forEachIndexed { i, pt ->
                val groupX = padL + i * groupW
                val barX = groupX + (si * barW) + barW / 2f
                val barH = (pt.value / maxVal) * chartH * animProgress
                drawRoundRect(
                    color = Color(s.color),
                    topLeft = Offset(barX, padT + chartH - barH),
                    size = Size(barW * 0.8f, barH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
    // Legend
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        series.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Color(s.color), RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(4.dp))
                Text(s.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PIE CHART — animated with legend
// ---------------------------------------------------------------------------
@Composable
fun PieChart(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    if (slices.isEmpty()) return
    val total = slices.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(durationMillis = 900, easing = EaseOut))

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f * animProgress
                drawArc(
                    color = Color(slice.color),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                // Gap
                drawArc(Color.Transparent, startAngle + sweep - 1f, 2f, true,
                    Offset(center.x - radius, center.y - radius), Size(radius * 2, radius * 2))
                startAngle += sweep
            }
            // Center hole
            drawCircle(Color.White.copy(alpha = 0.9f), radius * 0.5f, center)
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            slices.forEach { slice ->
                val pct = (slice.value / total * 100).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Color(slice.color), CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(slice.label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text("$pct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(slice.color))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DUAL BAR CHART (Store vs Retrieve)
// ---------------------------------------------------------------------------
@Composable
fun DualBarChart(
    seriesA: List<DataPoint>, seriesB: List<DataPoint>,
    labelA: String, labelB: String,
    colorA: Color, colorB: Color,
    modifier: Modifier = Modifier.fillMaxWidth().height(180.dp)
) {
    BarChart(
        series = listOf(
            ChartSeries(labelA, colorA.value.toLong(), seriesA),
            ChartSeries(labelB, colorB.value.toLong(), seriesB)
        ),
        modifier = modifier
    )
}
