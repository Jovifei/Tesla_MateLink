package com.teslamatelink.ui.efficiency

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private data class EffPoint(val speed: Float, val eff: Float, val temp: Float)
private data class SpeedZone(val label: String, val min: Int, val max: Int, val count: Int, val avgEff: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfficiencyScreen(
    onBack: () -> Unit
) {
    val points = rememberEffPoints()
    val zones = rememberSpeedZones(points)
    val textMeasurer = rememberTextMeasurer()
    val goldenFootScore = remember(points) {
        if (points.isEmpty()) 0.0
        else {
            val avg = points.map { it.eff }.average()
            if (avg <= 0.0) 0.0
            else min(100.0, max(0.0, (150.0 / avg) * 100.0))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Efficiency Curve") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Speed vs Efficiency — colored by outside temperature",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Scatter chart canvas
            Card(
                modifier = Modifier.fillMaxWidth().height(280.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    val padL = 50.dp.toPx()
                    val padB = 30.dp.toPx()
                    val padT = 10.dp.toPx()
                    val padR = 10.dp.toPx()
                    val chartW = size.width - padL - padR
                    val chartH = size.height - padT - padB

                    val maxSpeed = 160f
                    val maxEff = 300f
                    val axisStyle = TextStyle(fontSize = 9.sp, color = Color.Gray)

                    // Y axis labels
                    for (eff in 0..300 step 50) {
                        val y = padT + chartH * (1 - eff / maxEff)
                        val label = textMeasurer.measure("$eff", axisStyle)
                        drawText(label, topLeft = Offset(2.dp.toPx(), y - label.size.height / 2))
                        drawLine(Color.LightGray, Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 0.5f)
                    }
                    // X axis labels
                    for (speed in 0..160 step 40) {
                        val x = padL + chartW * (speed / maxSpeed)
                        val label = textMeasurer.measure("$speed", axisStyle)
                        drawText(label, topLeft = Offset(x - label.size.width / 2, size.height - padB + 4.dp.toPx()))
                        drawLine(Color.LightGray, Offset(x, padT), Offset(x, size.height - padB), strokeWidth = 0.5f)
                    }

                    // Axis labels
                    val xLabel = textMeasurer.measure("km/h", TextStyle(fontSize = 10.sp, color = Color.Gray))
                    drawText(xLabel, topLeft = Offset(size.width / 2 - xLabel.size.width / 2, size.height - 2.dp.toPx()))
                    val yLabel = textMeasurer.measure("Wh/km", TextStyle(fontSize = 10.sp, color = Color.Gray))
                    drawText(yLabel, topLeft = Offset(2.dp.toPx(), padT))

                    // Plot points
                    val tempColor: (Float) -> Color = { t ->
                        when {
                            t < 0f -> Color(0xFF3B82F6)
                            t < 15f -> Color(0xFF10B981)
                            t < 25f -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                    }
                    points.forEach { p ->
                        val x = padL + chartW * (p.speed / maxSpeed)
                        val y = padT + chartH * (1f - p.eff / maxEff)
                        if (p.speed <= maxSpeed && p.eff <= maxEff && p.eff >= 0) {
                            drawCircle(
                                color = tempColor(p.temp).copy(alpha = 0.6f),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("<0 C", Color(0xFF3B82F6)),
                    Pair("0-15 C", Color(0xFF10B981)),
                    Pair("15-25 C", Color(0xFFF59E0B)),
                    Pair(">25 C", Color(0xFFEF4444))
                ).forEach { (label, color) ->
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.padding(end = 3.dp).height(10.dp).width(10.dp)) {
                            drawCircle(color = color)
                        }
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Golden Foot Score
            if (goldenFootScore > 0) {
                val (icon, label, color) = when {
                    goldenFootScore >= 90 -> Triple("🥇", "Excellent", Color(0xFF10B981))
                    goldenFootScore >= 70 -> Triple("🥈", "Good", Color(0xFF3B82F6))
                    goldenFootScore >= 50 -> Triple("🥉", "Fair", Color(0xFFF59E0B))
                    else -> Triple("", "Needs Improvement", Color(0xFFEF4444))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(icon, fontSize = 36.sp)
                        Column {
                            Text("Golden Foot Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${goldenFootScore.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            modifier = Modifier
                                .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Speed zone table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Speed Zone",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Drives",
                            modifier = Modifier.weight(0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Avg Eff",
                            modifier = Modifier.weight(0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    zones.forEach { zone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                "${zone.label} km/h",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${zone.count}",
                                modifier = Modifier.weight(0.5f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${zone.avgEff} Wh/km",
                                modifier = Modifier.weight(0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberEffPoints(): List<EffPoint> {
    return androidx.compose.runtime.remember {
        List(80) {
            val speed = (20f + kotlin.random.Random.nextFloat() * 130f)
            val baseEff = 120f + speed * 0.6f
            val tempNoise = (kotlin.random.Random.nextFloat() - 0.5f) * 40f
            val eff = (baseEff + tempNoise).coerceIn(80f, 280f)
            val temp = (kotlin.random.Random.nextFloat() * 35f - 5f)
            EffPoint(speed, eff, temp)
        }
    }
}

@Composable
private fun rememberSpeedZones(points: List<EffPoint>): List<SpeedZone> {
    return androidx.compose.runtime.remember(points) {
        val zoneDefs = listOf(
            Pair("0-30", 0..30),
            Pair("30-60", 30..60),
            Pair("60-90", 60..90),
            Pair("90-120", 90..120),
            Pair("120+", 120..250)
        )
        zoneDefs.map { (label, range) ->
            val filtered = points.filter { it.speed.toInt() in range }
            SpeedZone(
                label = label,
                min = range.first,
                max = if (range.last == 250) 999 else range.last,
                count = filtered.size,
                avgEff = if (filtered.isNotEmpty()) filtered.map { it.eff }.average().roundToInt() else 0
            )
        }
    }
}
