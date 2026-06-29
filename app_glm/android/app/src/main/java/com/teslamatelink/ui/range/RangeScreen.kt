package com.teslamatelink.ui.range

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

private data class TripPoint(
    val date: String,
    val est: Float,
    val actual: Float,
    val diff: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeScreen(
    onBack: () -> Unit
) {
    val trips = rememberTripData()
    val avgDiff = if (trips.isNotEmpty()) trips.map { abs(it.diff) }.average().toFloat() else 0f
    val accuracy = if (trips.isNotEmpty()) {
        val totalEstDiff = trips.sumOf { abs(it.est - it.actual).toDouble() }
        val totalAbsDiff = trips.sumOf { abs(it.diff).toDouble() }
        if (totalEstDiff > 0) ((1 - totalAbsDiff / totalEstDiff) * 100).roundToInt() else 0
    } else 0
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projected Range") },
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
                text = "Estimated vs actual battery consumption per trip",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStat("Avg Diff", "${String.format("%.1f", avgDiff)}%", Modifier.weight(1f))
                SummaryStat("Trips", "${trips.size}", Modifier.weight(1f))
                SummaryStat("Accuracy", "${accuracy}%", Modifier.weight(1f))
            }

            // Chart
            if (trips.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Est vs Actual per Trip",
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Canvas(modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 4.dp)) {
                        val padL = 36.dp.toPx()
                        val padR = 8.dp.toPx()
                        val padT = 24.dp.toPx()
                        val padB = 24.dp.toPx()
                        val chartW = size.width - padL - padR
                        val chartH = size.height - padT - padB

                        val maxPct = 100f
                        val axisStyle = TextStyle(fontSize = 9.sp, color = Color.Gray)

                        // Y labels
                        for (pct in 0..100 step 20) {
                            val y = padT + chartH * (1f - pct / maxPct)
                            val label = textMeasurer.measure("$pct%", axisStyle)
                            drawText(label, topLeft = Offset(2.dp.toPx(), y - label.size.height / 2))
                            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(padL, y), Offset(size.width - padR, y), 0.5f)
                        }

                        // Est line (blue)
                        if (trips.size >= 2) {
                            val estPath = Path()
                            val actualPath = Path()

                            trips.forEachIndexed { index, t ->
                                val x = padL + chartW * index / (trips.size - 1).coerceAtLeast(1)
                                val estY = padT + chartH * (1f - t.est / maxPct)
                                val actY = padT + chartH * (1f - t.actual / maxPct)

                                if (index == 0) {
                                    estPath.moveTo(x, estY)
                                    actualPath.moveTo(x, actY)
                                } else {
                                    estPath.lineTo(x, estY)
                                    actualPath.lineTo(x, actY)
                                }
                            }

                            drawPath(estPath, Color(0xFF3B82F6), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                            drawPath(actualPath, Color(0xFF10B981), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                            // Dots
                            trips.forEach { t ->
                                val x = padL + chartW * trips.indexOf(t) / (trips.size - 1).coerceAtLeast(1)
                                drawCircle(Color(0xFF3B82F6), 2.5.dp.toPx(), Offset(x, padT + chartH * (1f - t.est / maxPct)))
                                drawCircle(Color(0xFF10B981), 2.5.dp.toPx(), Offset(x, padT + chartH * (1f - t.actual / maxPct)))
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(
                        Pair("Est %", Color(0xFF3B82F6)),
                        Pair("Actual %", Color(0xFF10B981))
                    ).forEach { (label, color) ->
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.padding(end = 4.dp).height(10.dp).width(16.dp)) {
                                drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2.dp.toPx(), StrokeCap.Round)
                                drawCircle(color, 2.dp.toPx(), Offset(size.width / 2, size.height / 2))
                            }
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberTripData(): List<TripPoint> {
    return androidx.compose.runtime.remember {
        listOf(
            TripPoint("Jun 1", 85f, 82f, 3f),
            TripPoint("Jun 2", 78f, 74f, 4f),
            TripPoint("Jun 3", 72f, 70f, 2f),
            TripPoint("Jun 4", 68f, 65f, 3f),
            TripPoint("Jun 5", 75f, 73f, 2f),
            TripPoint("Jun 6", 70f, 72f, -2f),
            TripPoint("Jun 7", 82f, 78f, 4f),
            TripPoint("Jun 8", 90f, 85f, 5f),
            TripPoint("Jun 9", 65f, 63f, 2f),
            TripPoint("Jun 10", 60f, 58f, 2f)
        )
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
