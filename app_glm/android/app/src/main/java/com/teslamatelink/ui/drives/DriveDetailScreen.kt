package com.teslamatelink.ui.drives

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ChartTab(val label: String, val unit: String)

private val chartTabs = listOf(
    ChartTab("速度", "km/h"),
    ChartTab("功率", "kW"),
    ChartTab("海拔", "m"),
    ChartTab("车内温度", "°C"),
    ChartTab("车外温度", "°C")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveDetailScreen(
    driveId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DriveViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(driveId) { viewModel.loadDriveDetail(driveId) }

    val drive = state.selectedDrive

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "行程详情",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
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
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.IosShare,
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
        if (drive == null) return@Scaffold

        // Derived values
        val maxSpeed = (drive.avgSpeed * 1.32).toInt()
        val energyKwh = drive.efficiency * drive.distanceKm / 1000.0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 路线概览卡 ──
            DetailCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${drive.startAddress} → ${drive.endAddress}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchColors.OnSurface
                        )
                    }
                    Text(
                        text = formatDateTime(drive.startDate),
                        fontSize = 13.sp,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }

            // ── 地图占位卡（路线轨迹） ──
            RouteTraceCard()

            // ── 统计数据网格 (2×3) ──
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCell(Modifier.weight(1f), "距离", "%.1f".format(drive.distanceKm), "km")
                StatCell(Modifier.weight(1f), "时长", drive.durationMinutes.toString(), "min")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCell(Modifier.weight(1f), "最高速度", maxSpeed.toString(), "km/h")
                StatCell(Modifier.weight(1f), "均速", "%.0f".format(drive.avgSpeed), "km/h")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCell(Modifier.weight(1f), "能耗", "%.1f".format(energyKwh), "kWh")
                StatCell(Modifier.weight(1f), "效率", "%.0f".format(drive.efficiency), "Wh/km")
            }

            // ── 电量变化卡 ──
            DetailCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "电量变化",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchColors.OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${drive.batteryStart}%",
                            fontSize = 14.sp,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                        BatteryDeltaBar(
                            startPct = drive.batteryStart,
                            endPct = drive.batteryEnd,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${drive.batteryEnd}%",
                            fontSize = 14.sp,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "-${drive.batteryStart - drive.batteryEnd}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // ── 行程曲线大卡 ──
            DetailCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "行程曲线",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchColors.OnSurface
                    )
                    Spacer(Modifier.height(16.dp))

                    // Tabs (horizontal scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chartTabs.forEachIndexed { i, tab ->
                            ChartTabChip(
                                label = tab.label,
                                selected = selectedTab == i,
                                onClick = { selectedTab = i }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // Chart
                    val data = generateChartData(selectedTab)
                    LineCurveChart(
                        values = data,
                        peakLabel = "${data.maxOrNull()?.toInt() ?: 0} ${chartTabs[selectedTab].unit}",
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    // X axis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val half = drive.durationMinutes / 2
                        listOf("0", "$half min", "${drive.durationMinutes} min").forEach {
                            Text(
                                text = it,
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFamily,
                                color = StitchColors.OnSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "模拟数据 — 基于行程摘要",
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = StitchColors.OnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // ── 导出按钮 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StitchColors.OnSurface, RoundedCornerShape(8.dp))
                    .clickable { }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.IosShare,
                    contentDescription = null,
                    tint = StitchColors.OnSurface,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(Modifier.height(0.dp))
                Text(
                    text = "  导出此行程",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchColors.OnSurface
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Reusable card: white surface, 1px on-surface border, 8dp radius, no shadow ──
@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(1.dp, StitchColors.OnSurface, RoundedCornerShape(8.dp))
    ) { content() }
}

@Composable
private fun StatCell(modifier: Modifier, label: String, value: String, unit: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(1.dp, StitchColors.OnSurface, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = " $unit",
                    fontSize = 13.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChartTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) StitchColors.Primary else StitchColors.SurfaceContainerLowest
    val fg = if (selected) StitchColors.OnPrimary else StitchColors.OnSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, StitchColors.OnSurface, RoundedCornerShape(50))
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = fg)
    }
}

// ── Route trace placeholder card (dashed line, matches Stitch SVG mock) ──
@Composable
private fun RouteTraceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(1.dp, StitchColors.OnSurface, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.1f, h * 0.85f)
                quadraticBezierTo(w * 0.5f, h * 0.7f, w * 0.85f, h * 0.2f)
            }
            drawPath(
                path = path,
                color = StitchColors.OnSurface,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )
            // start dot
            drawCircle(StitchColors.OnSurface, radius = 8f, center = Offset(w * 0.1f, h * 0.85f))
            // end flag marker (square)
            drawRect(
                color = StitchColors.OnSurface,
                topLeft = Offset(w * 0.82f, h * 0.17f),
                size = androidx.compose.ui.geometry.Size(16f, 16f)
            )
        }
        Text(
            text = "路线轨迹",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = StitchColors.OnSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
}

// ── Battery delta bar: light track + black segment from end% to start% ──
@Composable
private fun BatteryDeltaBar(startPct: Int, endPct: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .background(StitchColors.SurfaceContainerHighest)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val left = w * (endPct / 100f)
            val right = w * (startPct / 100f)
            drawRoundRect(
                color = StitchColors.Primary,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size((right - left).coerceAtLeast(0f), h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
            )
        }
    }
}

// ── Minimal line chart: gold main line + dashed grid ──
@Composable
private fun LineCurveChart(values: List<Float>, peakLabel: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // dashed horizontal grid lines
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            for (i in 1..3) {
                val y = h * i / 4f
                drawLine(
                    color = StitchColors.SurfaceContainerHighest,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
            }
            // bottom + left axis
            drawLine(StitchColors.SurfaceContainerHighest, Offset(0f, h), Offset(w, h), 1.5f)
            drawLine(StitchColors.SurfaceContainerHighest, Offset(0f, 0f), Offset(0f, h), 1.5f)

            if (values.size < 2) return@Canvas
            val minV = values.min()
            val maxV = values.max()
            val range = (maxV - minV).takeIf { it > 0f } ?: 1f
            val stepX = w / (values.size - 1)
            val path = Path()
            values.forEachIndexed { i, v ->
                val x = stepX * i
                val y = h - ((v - minV) / range) * (h * 0.9f) - h * 0.05f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = StitchColors.Accent,   // 金色 #A16207
                style = Stroke(width = 3f)
            )
        }
        // Peak label chip
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StitchColors.Background)
                .border(1.dp, StitchColors.OnSurface, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = peakLabel,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurface
            )
        }
    }
}

private fun generateChartData(tab: Int): List<Float> = when (tab) {
    0 -> listOf(0f, 45f, 80f, 65f, 50f, 90f, 70f, 55f, 40f, 30f,
        60f, 75f, 85f, 50f, 35f, 20f, 55f, 70f, 45f, 30f,
        65f, 80f, 60f, 40f, 25f, 50f, 75f, 55f, 35f, 0f) // 速度
    1 -> listOf(0f, 20f, 60f, 40f, 30f, 80f, 110f, 50f, 35f, 25f,
        55f, 90f, 120f, 70f, 40f, 15f, 45f, 85f, 50f, 20f,
        60f, 95f, 75f, 45f, 20f, 40f, 70f, 55f, 25f, 0f) // 功率
    2 -> listOf(50f, 80f, 150f, 200f, 300f, 350f, 420f, 380f, 250f, 180f,
        120f, 90f, 150f, 220f, 310f, 400f, 450f, 350f, 200f, 100f,
        80f, 120f, 180f, 250f, 320f, 380f, 300f, 200f, 100f, 50f) // 海拔
    3 -> listOf(22f, 23f, 24f, 25f, 26f, 27f, 26f, 25f, 24f, 23f,
        22f, 21f, 22f, 23f, 24f, 25f, 26f, 25f, 24f, 23f,
        22f, 21f, 20f, 21f, 22f, 23f, 24f, 23f, 22f, 21f) // 车内温度
    4 -> listOf(15f, 15f, 16f, 16f, 17f, 17f, 18f, 18f, 17f, 16f,
        15f, 15f, 16f, 17f, 18f, 18f, 19f, 18f, 17f, 16f,
        15f, 14f, 14f, 15f, 16f, 17f, 17f, 16f, 15f, 14f) // 车外温度
    else -> emptyList()
}

private fun formatDateTime(isoDate: String?): String {
    if (isoDate.isNullOrEmpty()) return "—"
    return try {
        val dt = ZonedDateTime.parse(isoDate)
        dt.format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.getDefault()))
    } catch (_: Exception) {
        "—"
    }
}
