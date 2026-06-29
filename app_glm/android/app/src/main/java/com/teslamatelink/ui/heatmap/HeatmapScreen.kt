package com.teslamatelink.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.MateLinkTheme
import com.teslamatelink.ui.theme.LocalCarAccent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalConfiguration

private val NO_DRIVE = Color(0xFFE5E7EB)
private val LOW = Color(0x4000BFFF)
private val MID = Color(0x8000BFFF)
private val HIGH = Color(0xC000BFFF)
private val MAX = Color(0xFF00BFFF)
private val HOURS = listOf(0, 3, 6, 9, 12, 15, 18, 21)
private const val DAYS = 15
private const val CELL_SIZE_DP = 18f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    onBack: () -> Unit
) {
    val gridData = rememberGridData()
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drive Heatmap") },
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
                .padding(16.dp)
        ) {
            Text(
                text = "15 days x 24 hours — GitHub-style activity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val cellSizeDp = CELL_SIZE_DP.dp
            val labelWidth = 24.dp
            val totalWidth = labelWidth + (cellSizeDp * DAYS) + (cellSizeDp * 0.3f * (DAYS - 1))
            val totalHeight = cellSizeDp * 24 + cellSizeDp * 0.3f * 23 + 20.dp

            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(totalHeight)
                    .padding(top = 12.dp)
            ) {
                val cellW = cellSizeDp.toPx()
                val cellH = cellSizeDp.toPx()
                val gap = cellW * 0.3f
                val labelRight = labelWidth.toPx()

                // Hour labels
                val labelStyle = TextStyle(
                    fontSize = 9.sp,
                    color = Color.Gray
                )
                HOURS.forEach { h ->
                    val y = h * (cellH + gap) + cellH / 2 + 6.dp.toPx()
                    val textResult = textMeasurer.measure(
                        text = String.format("%02d", h),
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(labelRight - textResult.size.width - 4.dp.toPx(), y - textResult.size.height / 2)
                    )
                }

                // Grid cells
                val maxVal = gridData.maxOrNull()?.maxOrNull()?.coerceAtLeast(1) ?: 1
                for (h in 0 until 24) {
                    for (d in 0 until DAYS) {
                        val x = labelRight + d * (cellW + gap)
                        val y = h * (cellH + gap) + 6.dp.toPx()
                        val value = gridData.getOrNull(h)?.getOrNull(d) ?: 0f
                        val color = when {
                            value == 0f -> NO_DRIVE
                            value / maxVal < 0.25f -> LOW
                            value / maxVal < 0.5f -> MID
                            value / maxVal < 0.75f -> HIGH
                            else -> MAX
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(NO_DRIVE, LOW, MID, HIGH, MAX).forEach { color ->
                    Canvas(modifier = Modifier.padding(horizontal = 2.dp).width(14.dp).height(14.dp)) {
                        drawRoundRect(
                            color = color,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                    }
                }
                Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun rememberGridData(): List<List<Float>> {
    return androidx.compose.runtime.remember {
        List(24) { h ->
            List(DAYS) { d ->
                val dayFactor = (DAYS - d).toFloat() / DAYS
                val hourFactor = when (h) {
                    in 7..9 -> 1.5f
                    in 16..18 -> 2.0f
                    in 11..13 -> 0.8f
                    in 22..23 -> 1.2f
                    else -> 0.5f
                }
                val noise = listOf(0.2f, 0.5f, 0.8f, 1.2f, 1.5f, 0.9f, 0.3f).random()
                (dayFactor * hourFactor * noise * 15f).coerceAtLeast(0f)
            }
        }
    }
}
