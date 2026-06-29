package com.teslamatelink.ui.vampire

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

private data class DrainEvent(val day: Int, val kwh: Float, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VampireScreen(
    onBack: () -> Unit
) {
    val events = rememberDrainEvents()
    val totalKwh = events.sumOf { it.kwh.toDouble() }.toFloat()
    val totalKm = (totalKwh * 6.9f).roundToInt()
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vampire Drain") },
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
                text = "Estimated battery loss during parking periods",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Total Drain", "${String.format("%.1f", totalKwh)} kWh", modifier = Modifier.weight(1f))
                StatCard("Range Loss", "${totalKm} km", modifier = Modifier.weight(1f), valueColor = Color(0xFFEF4444))
                StatCard("Events", "${events.size}", modifier = Modifier.weight(1f))
            }

            // Trend chart
            if (events.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Daily Drain Trend",
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Canvas(modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 4.dp)) {
                        val padL = 40.dp.toPx()
                        val padR = 8.dp.toPx()
                        val padT = 20.dp.toPx()
                        val padB = 20.dp.toPx()
                        val chartW = size.width - padL - padR
                        val chartH = size.height - padT - padB

                        val maxKwh = events.maxOf { it.kwh }.coerceAtLeast(0.1f)
                        val axisStyle = TextStyle(fontSize = 9.sp, color = Color.Gray)

                        // Y labels
                        for (i in 0..4) {
                            val v = maxKwh * i / 4
                            val y = padT + chartH * (1f - i / 4f)
                            val label = textMeasurer.measure(String.format("%.1f", v), axisStyle)
                            drawText(label, topLeft = Offset(2.dp.toPx(), y - label.size.height / 2))
                            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(padL, y), Offset(size.width - padR, y), strokeWidth = 0.5f)
                        }

                        // Line path
                        if (events.size >= 2) {
                            val path = Path()
                            events.forEachIndexed { index, e ->
                                val x = padL + chartW * index / (events.size - 1).coerceAtLeast(1)
                                val y = padT + chartH * (1f - e.kwh / maxKwh)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, color = Color(0xFFEF4444), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                            // Dots
                            events.forEach { e ->
                                val x = padL + chartW * events.indexOf(e) / (events.size - 1).coerceAtLeast(1)
                                val y = padT + chartH * (1f - e.kwh / maxKwh)
                                drawCircle(Color(0xFFEF4444), radius = 3.dp.toPx(), center = Offset(x, y))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDrainEvents(): List<DrainEvent> {
    return androidx.compose.runtime.remember {
        (1..14).map { day ->
            DrainEvent(
                day = day,
                kwh = (1.5f + kotlin.random.Random.nextFloat() * 4f),
                label = "Day $day"
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
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

private fun Float.roundToInt(): Int = kotlin.math.roundToInt(this)
