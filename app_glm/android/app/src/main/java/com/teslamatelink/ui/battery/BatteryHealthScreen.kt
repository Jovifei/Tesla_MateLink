package com.teslamatelink.ui.battery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Stitch design tokens (battery-specific) ──────────────────────────────────
private val AccentGold = Color(0xFFA16207)
private val RingTrack = Color(0xFFF5F5F5)
private val RingProgress = Color(0xFF171717)
private val GridLine = Color(0xFFF5F5F5)
private val BaselineGray = Color(0xFFD1D5DB)
private val EmeraldBg = Color(0xFFD1FAE5)
private val EmeraldText = Color(0xFF059669)
private val TempLow = Color(0xFFDBEAFE)      // blue-100
private val TempLowText = Color(0xFF1D4ED8)  // blue-700
private val TempNormal = Color(0xFF10B981)   // emerald-500
private val TempHigh = Color(0xFFFFEDD5)     // orange-100
private val TempHighText = Color(0xFFC2410C) // orange-700

// ── Mock data (matches Stitch design spec; data layer unchanged) ──────────────
// TODO(data): wire to BatteryHealth domain model via DelegatingCarRepository.
private const val HEALTH_PERCENT = 95.8f
private const val NOMINAL_CAPACITY = "82.0"
private const val CURRENT_CAPACITY = "78.5"
private const val DEGRADATION = "4.2%"

// Monthly SOH samples for the current car (Jan..Dec, normalized 0..1 for curve).
private val currentCarCurve = listOf(1.00f, 0.995f, 0.99f, 0.985f, 0.98f, 0.975f, 0.97f, 0.966f, 0.963f, 0.96f, 0.959f, 0.958f)
private val fleetBaselineCurve = listOf(1.00f, 0.99f, 0.982f, 0.975f, 0.968f, 0.962f, 0.955f, 0.95f, 0.945f, 0.94f, 0.936f, 0.933f)

private data class CycleStat(val label: String, val value: String, val unit: String)
private val cycleStats = listOf(
    CycleStat("总循环", "486", "次"),
    CycleStat("本月循环", "42", "次"),
    CycleStat("日均循环", "1.4", "次")
)

private data class TempSegment(val percent: Float, val color: Color, val textColor: Color, val legend: String)
private val tempSegments = listOf(
    TempSegment(0.15f, TempLow, TempLowText, "<10°C (Low)"),
    TempSegment(0.75f, TempNormal, Color.White, "10-35°C (Normal)"),
    TempSegment(0.10f, TempHigh, TempHighText, ">35°C (High)")
)

private data class Suggestion(val icon: ImageVector, val title: String, val desc: String)
private val suggestions = listOf(
    Suggestion(Icons.Filled.Bolt, "避免频繁快充至100%", "建议在非长途旅行时将限额设为80% - 90%"),
    Suggestion(Icons.Filled.BatteryChargingFull, "保持电量在20-80%区间", "在此区间内循环充放电可显著延长电芯寿命"),
    Suggestion(Icons.Filled.Thermostat, "高温环境减少快充", "极端高温下快充会加速电解液老化")
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "电池健康",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 电池健康说明 */ }) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "说明",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            HealthRingCard()
            StatsGrid()
            DegradationTrendCard()
            CycleStatsCard()
            TemperatureCard()
            MaintenanceCard()
        }
    }
}

// ── Main Health Card ──────────────────────────────────────────────────────────

@Composable
private fun HealthRingCard() {
    StitchCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(192.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(192.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = 160.dp.toPx()
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)
                    // Track (full circle)
                    drawArc(
                        color = RingTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress (starts at top, -90deg)
                    drawArc(
                        color = RingProgress,
                        startAngle = -90f,
                        sweepAngle = 360f * (HEALTH_PERCENT / 100f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f%%".format(HEALTH_PERCENT),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "优秀",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = EmeraldText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "电池健康度",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

// ── Stats Grid (3 columns) ────────────────────────────────────────────────────

@Composable
private fun StatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCell("标称容量", NOMINAL_CAPACITY, "kWh", Modifier.weight(1f))
        StatCell("当前容量", CURRENT_CAPACITY, "kWh", Modifier.weight(1f))
        StatCell("衰减", DEGRADATION, null, Modifier.weight(1f), valueColor = AccentGold)
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    valueColor: Color = StitchColors.OnSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.Surface)
            .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = valueColor
                )
                if (unit != null) {
                    Text(
                        text = " $unit",
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFamily,
                        color = valueColor
                    )
                }
            }
        }
    }
}

// ── Degradation Trend Card ─────────────────────────────────────────────────────

@Composable
private fun DegradationTrendCard() {
    StitchCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "容量衰减趋势",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = StitchColors.OnSurface
            )
            Text(
                text = "高于平均",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = EmeraldText,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
        ) {
            val w = size.width
            val h = size.height
            // Grid lines (horizontal, quarters)
            for (i in 1..3) {
                val y = h * i / 4f
                drawLine(GridLine, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }
            // Axis (left + bottom)
            drawLine(StitchColors.OutlineVariant, Offset(0f, 0f), Offset(0f, h), strokeWidth = 1.dp.toPx())
            drawLine(StitchColors.OutlineVariant, Offset(0f, h), Offset(w, h), strokeWidth = 1.dp.toPx())

            fun toPath(points: List<Float>): Path {
                val path = Path()
                // Map SOH range 0.90..1.00 across full height for visible slope.
                val minV = 0.90f
                val maxV = 1.00f
                points.forEachIndexed { idx, v ->
                    val x = w * idx / (points.size - 1).toFloat()
                    val norm = ((v - minV) / (maxV - minV)).coerceIn(0f, 1f)
                    val y = h - norm * h
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                return path
            }

            // Fleet baseline (dashed gray)
            drawPath(
                path = toPath(fleetBaselineCurve),
                color = BaselineGray,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            )
            // Current car (solid gold #A16207)
            drawPath(
                path = toPath(currentCarCurve),
                color = AccentGold,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1月", "4月", "7月", "10月", "12月").forEach {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchColors.OnSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Legend
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .background(AccentGold)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "本车",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .background(BaselineGray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "车队基准",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

// ── Cycle Stats Card ──────────────────────────────────────────────────────────

@Composable
private fun CycleStatsCard() {
    StitchCard {
        Text(
            text = "循环统计",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = StitchColors.OnSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            cycleStats.forEach { stat ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stat.value,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stat.unit,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── Temperature Distribution Card ─────────────────────────────────────────────

@Composable
private fun TemperatureCard() {
    StitchCard {
        Text(
            text = "电池温度分布",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = StitchColors.OnSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Stacked bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            tempSegments.forEach { seg ->
                Box(
                    modifier = Modifier
                        .weight(seg.percent)
                        .fillMaxSize()
                        .background(seg.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(seg.percent * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = seg.textColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Legend (3 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tempSegments.forEach { seg ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(seg.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = seg.legend,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Maintenance Suggestions Card ──────────────────────────────────────────────

@Composable
private fun MaintenanceCard() {
    StitchCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "维护建议",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = StitchColors.OnSurface
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        suggestions.forEachIndexed { idx, s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchColors.SurfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = s.icon,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = s.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = s.desc,
                        fontSize = 14.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }
            if (idx < suggestions.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
