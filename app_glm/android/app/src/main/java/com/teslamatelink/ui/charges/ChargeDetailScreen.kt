package com.teslamatelink.ui.charges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// Stitch-aligned local colors (charge_detail.html)
private val DcOrange = Color(0xFFF59E0B)
private val AcBlue = Color(0xFF3B82F6)

private val curveTabs = listOf("功率", "电量", "电压", "温度")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeDetailScreen(
    chargeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ChargeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(chargeId) { viewModel.loadChargeDetail(chargeId) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "充电详情",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface,
                        letterSpacing = (-0.2).sp
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
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "分享",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background,
                    scrolledContainerColor = StitchColors.Background
                )
            )
        }
    ) { padding ->
        val charge = state.selectedCharge
        if (state.isLoading || charge == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(color = StitchColors.OnSurface) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OverviewCard(charge)
            BatteryChangeCard(charge)
            ChargingCurveCard(selectedTab, charge, onTabSelect = { selectedTab = it })
            StatsGrid(charge)
            ChargingStagesCard(charge)
            ExportButton()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// MARK: - Overview Card ---------------------------------------------------

@Composable
private fun OverviewCard(charge: ChargeItem) {
    StitchOutlineCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = charge.address.ifBlank { if (charge.isDc) "超充站" else "家充" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchColors.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatLongDate(charge.date),
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DcAcTag(isDc = charge.isDc)
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "+%.1f".format(charge.energyKwh),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.Accent
                    )
                    Text(
                        text = "kWh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                }
            }
        }
    }
}

// MARK: - Battery Change Card ---------------------------------------------

@Composable
private fun BatteryChangeCard(charge: ChargeItem) {
    StitchOutlineCard {
        SectionTitle("电量变化")
        Spacer(modifier = Modifier.height(24.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchColors.SurfaceContainerLow)
        ) {
            val startFraction = (charge.startBattery / 100f).coerceIn(0f, 1f)
            val rangeFraction = ((charge.endBattery - charge.startBattery) / 100f).coerceIn(0f, 1f)
            val trackWidth = maxWidth
            // gold faint full track
            Box(
                Modifier.fillMaxWidth().fillMaxHeight()
                    .background(StitchColors.Accent.copy(alpha = 0.10f))
            )
            // gold filled range
            Box(
                Modifier.fillMaxHeight()
                    .width(trackWidth * rangeFraction)
                    .offset(x = trackWidth * startFraction)
                    .background(StitchColors.Accent.copy(alpha = 0.35f))
            )
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${charge.startBattery}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = "+${charge.endBattery - charge.startBattery}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.Accent
                )
                Text(
                    text = "${charge.endBattery}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
            }
        }
    }
}

// MARK: - Charging Curve Card ---------------------------------------------

@Composable
private fun ChargingCurveCard(
    selectedTab: Int,
    charge: ChargeItem,
    onTabSelect: (Int) -> Unit
) {
    StitchOutlineCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("充电曲线")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                curveTabs.forEachIndexed { i, label ->
                    val active = selectedTab == i
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTabSelect(i) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) StitchColors.OnSurface else StitchColors.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(if (active) StitchColors.OnSurface else Color.Transparent)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CurveChart(
            values = generateCurveData(selectedTab, charge),
            peakLabel = curvePeakLabel(selectedTab, charge)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            curveTimeAxis(charge).forEach { t ->
                Text(
                    text = t,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = StitchColors.OnSurfaceVariant,
                    fontFamily = JetBrainsMonoFamily
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "模拟数据 — 基于充电摘要",
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            color = StitchColors.OnSurfaceVariant
        )
    }
}

/** Swiss-minimal single-line curve (gold #A16207) with peak dot + label + baseline. */
@Composable
private fun CurveChart(values: List<Float>, peakLabel: String) {
    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.size < 2) return@Canvas
            val w = size.width
            val h = size.height
            val baseLineY = h - 8f
            val maxV = values.max()
            val minV = values.min()
            val range = (maxV - minV).takeIf { it > 0f } ?: 1f
            val topPad = 24f
            val usableH = baseLineY - topPad

            fun px(i: Int) = w * i / (values.size - 1)
            fun py(v: Float) = baseLineY - ((v - minV) / range) * usableH

            // baseline
            drawLine(
                color = StitchColors.Border,
                start = Offset(0f, baseLineY),
                end = Offset(w, baseLineY),
                strokeWidth = 1f
            )
            // smooth path
            val path = Path().apply {
                moveTo(px(0), py(values[0]))
                for (i in 1 until values.size) {
                    val prevX = px(i - 1)
                    val prevY = py(values[i - 1])
                    val curX = px(i)
                    val curY = py(values[i])
                    val midX = (prevX + curX) / 2
                    cubicTo(midX, prevY, midX, curY, curX, curY)
                }
            }
            drawPath(
                path = path,
                color = StitchColors.Accent,
                style = Stroke(width = 2.2f)
            )
            // peak dot
            val peakIdx = values.indexOf(maxV)
            drawCircle(
                color = StitchColors.Accent,
                radius = 4f,
                center = Offset(px(peakIdx), py(maxV))
            )
        }
        Text(
            text = peakLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.Accent,
            fontFamily = JetBrainsMonoFamily,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
        )
    }
}

// MARK: - Stats Grid (2x2) ------------------------------------------------

@Composable
private fun StatsGrid(charge: ChargeItem) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCell(Modifier.weight(1f), "充入电量", "%.1f".format(charge.energyKwh), "kWh")
            StatCell(Modifier.weight(1f), "费用", "¥%.2f".format(charge.cost), "")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCell(Modifier.weight(1f), "平均功率", "%.1f".format(charge.avgPower), "kW")
            StatCell(Modifier.weight(1f), "用时", formatDuration(charge.durationMinutes), "")
        }
    }
}

@Composable
private fun StatCell(modifier: Modifier, label: String, value: String, unit: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).height(64.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                }
            }
        }
    }
}

// MARK: - Charging Stages Card --------------------------------------------

private data class Stage(val name: String, val range: String, val minutes: Int, val weight: Float, val done: Boolean)

@Composable
private fun ChargingStagesCard(charge: ChargeItem) {
    val total = charge.durationMinutes.takeIf { it > 0 } ?: 135
    // Derived stage split (恒流 ~78% / 恒压 ~15% / 涓流 ~7%). Simulated when no telemetry.
    val ccMin = (total * 0.78f).toInt()
    val cvMin = (total * 0.15f).toInt()
    val trickleMin = (total - ccMin - cvMin).coerceAtLeast(0)
    val stages = listOf(
        Stage("恒流段", "0-80%", ccMin, 0.80f, true),
        Stage("恒压段", "80-95%", cvMin, 0.15f, false),
        Stage("涓流段", "95-100%", trickleMin, 0.05f, false)
    )
    StitchOutlineCard {
        SectionTitle("充电阶段")
        Spacer(modifier = Modifier.height(24.dp))
        // segmented bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(StitchColors.SurfaceContainerLow)
        ) {
            Box(Modifier.weight(stages[0].weight).fillMaxHeight().background(StitchColors.OnSurface))
            Box(Modifier.weight(stages[1].weight).fillMaxHeight().background(StitchColors.SurfaceContainerHigh))
            Box(Modifier.weight(stages[2].weight).fillMaxHeight().background(StitchColors.SurfaceContainerHigh))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stages.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (s.done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (s.done) StitchColors.OnSurface else StitchColors.OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${s.name} (${s.range})",
                            fontSize = 14.sp,
                            color = if (s.done) StitchColors.OnSurface else StitchColors.OnSurfaceVariant
                        )
                    }
                    Text(
                        text = formatDuration(s.minutes),
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// MARK: - Export Button ---------------------------------------------------

@Composable
private fun ExportButton() {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.Background,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "导出此充电记录",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurface
            )
        }
    }
}

// MARK: - Shared small pieces ---------------------------------------------

@Composable
private fun StitchOutlineCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = StitchColors.OnSurface
    )
}

@Composable
private fun DcAcTag(isDc: Boolean) {
    val color = if (isDc) DcOrange else AcBlue
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isDc) "DC" else "AC",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = color
        )
    }
}

// MARK: - Mock curve helpers ----------------------------------------------

private fun generateCurveData(tab: Int, charge: ChargeItem): List<Float> {
    val peak = if (charge.maxPower > 0) charge.maxPower.toFloat() else if (charge.isDc) 120f else 7.4f
    return when (tab) {
        0 -> listOf(0f, 0.4f, 0.75f, 0.95f, 1f, 0.92f, 0.8f, 0.6f, 0.4f, 0.22f, 0.1f, 0.04f)
            .map { it * peak } // 功率
        1 -> { // 电量 (SoC) start→end ramp
            val s = charge.startBattery.toFloat()
            val e = charge.endBattery.toFloat().coerceAtLeast(s + 1)
            (0..11).map { s + (e - s) * (it / 11f) }
        }
        2 -> listOf(230f, 235f, 240f, 245f, 250f, 252f, 250f, 246f, 242f, 238f, 234f, 230f) // 电压
        else -> listOf(22f, 24f, 26f, 28f, 30f, 31f, 30f, 28f, 26f, 24f, 22f, 21f) // 温度
    }
}

private fun curvePeakLabel(tab: Int, charge: ChargeItem): String {
    val peak = if (charge.maxPower > 0) charge.maxPower else if (charge.isDc) 120.0 else 7.4
    return when (tab) {
        0 -> "%.1f kW 峰值".format(peak)
        1 -> "${charge.endBattery}% 峰值"
        2 -> "252 V 峰值"
        else -> "31°C 峰值"
    }
}

private fun curveTimeAxis(charge: ChargeItem): List<String> {
    val start = charge.date.let { if (it.length >= 16) it.substring(11, 16) else "18:30" }
    return listOf(start, "", "", "结束")
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/** ISO "2025-07-01T18:30:..." -> "7月1日 18:30" */
private fun formatLongDate(iso: String): String {
    if (iso.length < 16) return iso
    val month = iso.substring(5, 7).toIntOrNull() ?: 1
    val day = iso.substring(8, 10).toIntOrNull() ?: 1
    val time = iso.substring(11, 16)
    return "${month}月${day}日 $time"
}
