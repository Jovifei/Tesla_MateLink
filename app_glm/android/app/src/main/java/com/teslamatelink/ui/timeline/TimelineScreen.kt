package com.teslamatelink.ui.timeline

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Stitch brand activity colors (from Stitch tailwind config) ──
private val BrandDrive = Color(0xFF059669)
private val BrandCharge = Color(0xFFF59E0B)
private val BrandRest = Color(0xFFA3A3A3)
private val BrandSentry = Color(0xFF7C3AED)

// ── 24h activity segment model ──
private data class ActivitySegment(
    val fraction: Float,   // width fraction of 24h (0..1)
    val type: String,      // drive / charge / rest / sentry
    val startLabel: String // e.g. "07:00"
)

private fun segColor(type: String): Color = when (type) {
    "drive" -> BrandDrive
    "charge" -> BrandCharge
    "sentry" -> BrandSentry
    else -> BrandRest
}

// TODO: replace with real per-day aggregation from TimelineViewModel.
// These mirror the Stitch 1:1 mock (2025/07/02) until the ViewModel exposes
// a 24h segmented activity model.
private val mockSegments = listOf(
    ActivitySegment(0.291f, "rest", "00:00"),
    ActivitySegment(0.052f, "drive", "07:00"),
    ActivitySegment(0.385f, "rest", "08:15"),
    ActivitySegment(0.021f, "charge", "17:30"),
    ActivitySegment(0.028f, "drive", "18:00"),
    ActivitySegment(0.055f, "rest", "18:40"),
    ActivitySegment(0.017f, "drive", "20:00"),
    ActivitySegment(0.151f, "rest", "20:25")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTimeline()
    }

    Scaffold(
        containerColor = StitchColors.Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "时间线",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchColors.Primary
                        )
                    }
                },
                actions = {
                    // Date navigation
                    IconButton(onClick = { /* TODO: previous day */ }) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "前一天",
                            tint = StitchColors.OnSurfaceVariant
                        )
                    }
                    Text(
                        "2025年7月2日",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = StitchColors.Primary
                    )
                    IconButton(onClick = { /* TODO: next day */ }) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "后一天",
                            tint = StitchColors.OnSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.loadTimeline() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            tint = StitchColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StitchColors.OnSurface)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SummaryCard()
                ActivityBarCard()
                ActivityDetailCard()
                PieChartCard()
                WeeklyChartCard()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Summary Card: 3-column (drive / charge / rest) ──
@Composable
private fun SummaryCard() {
    StitchCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryColumn("驾驶", "4", "h", "32", "min", "19%", BrandDrive, Modifier.weight(1f))
            VDivider()
            SummaryColumn("充电", "2", "h", "15", "min", "9%", BrandCharge, Modifier.weight(1f))
            VDivider()
            SummaryColumn("休息", "17", "h", "13", "min", "72%", BrandRest, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    hours: String,
    hUnit: String,
    mins: String,
    mUnit: String,
    percent: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(hours, fontSize = 24.sp, fontWeight = FontWeight.Medium, fontFamily = JetBrainsMonoFamily, color = color)
            Text(hUnit, fontSize = 12.sp, fontFamily = JetBrainsMonoFamily, color = color, modifier = Modifier.padding(start = 1.dp, bottom = 3.dp))
            Text(" $mins", fontSize = 24.sp, fontWeight = FontWeight.Medium, fontFamily = JetBrainsMonoFamily, color = color)
            Text(mUnit, fontSize = 12.sp, fontFamily = JetBrainsMonoFamily, color = color, modifier = Modifier.padding(start = 1.dp, bottom = 3.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            percent,
            fontSize = 16.sp,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun VDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(StitchColors.OutlineVariant)
    )
}

// ── 24h Activity Bar Card ──
@Composable
private fun ActivityBarCard() {
    StitchCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "今日活动·24小时",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Text(
                "2025/07/02",
                fontSize = 16.sp,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))

        // Segmented 24h strip drawn with Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            var x = 0f
            mockSegments.forEach { seg ->
                val w = size.width * seg.fraction
                drawRect(
                    color = segColor(seg.type),
                    topLeft = Offset(x, 0f),
                    size = Size(w, size.height)
                )
                // 1px white separator between segments
                if (x > 0f) {
                    drawLine(
                        color = Color.White,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                x += w
            }
        }
        Spacer(Modifier.height(6.dp))

        // Time axis labels (subset, matching Stitch)
        Row(modifier = Modifier.fillMaxWidth()) {
            AxisLabel("00:00", 0.291f, StitchColors.OnSurfaceVariant, StitchColors.OutlineVariant)
            AxisLabel("07:00", 0.052f, BrandDrive, BrandDrive)
            AxisLabel("08:15", 0.385f, StitchColors.OnSurfaceVariant, StitchColors.OutlineVariant)
            AxisLabel("17:30", 0.021f, BrandCharge, BrandCharge)
            AxisLabel("18:00", 0.028f, BrandDrive, BrandDrive)
            AxisLabel("20:00", 0.223f, StitchColors.OnSurfaceVariant, StitchColors.OutlineVariant)
        }

        Spacer(Modifier.height(16.dp))

        // Dynamic labels (key segments)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DynamicLabel(BrandDrive, "07:00 - 08:15", "家 → 公司 (31km)")
            DynamicLabel(BrandCharge, "17:30 - 18:00", "公司超充 (+18kWh)")
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.AxisLabel(
    time: String,
    fraction: Float,
    textColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .weight(fraction)
            .height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxSize()
                .background(borderColor)
        )
        Text(
            time,
            fontSize = 10.sp,
            fontFamily = JetBrainsMonoFamily,
            color = textColor,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
private fun DynamicLabel(dot: Color, time: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(dot)
        )
        Spacer(Modifier.width(8.dp))
        Text(time, fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = StitchColors.Primary)
        Spacer(Modifier.width(8.dp))
        Text(desc, fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
    }
}

// ── Activity Detail Card ──
@Composable
private fun ActivityDetailCard() {
    StitchCard {
        Text(
            "活动详情",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // TODO: bind to real segmented events once ViewModel exposes them.
            DetailRow(BrandDrive, Icons.Filled.DirectionsCar, "20:00 - 20:25", "12.5km", StitchColors.Primary, "驾驶 · 25min")
            DetailRow(BrandRest, Icons.Filled.LocalParking, "18:40 - 20:00", "公司停车", StitchColors.OnSurfaceVariant, "休息 · 1h 20min")
            DetailRow(BrandDrive, Icons.Filled.DirectionsCar, "18:00 - 18:40", "18.2km", StitchColors.Primary, "驾驶 · 40min")
            DetailRow(BrandCharge, Icons.Filled.EvStation, "17:30 - 18:00", "+18.5kWh", BrandCharge, "充电 · 30min", cost = "¥9.25")
        }
    }
}

@Composable
private fun DetailRow(
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    cost: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(Modifier.width(16.dp))
        // Icon chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StitchColors.SurfaceContainer)
                .padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(time, fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = StitchColors.OnSurface)
                Text(value, fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = valueColor)
            }
            if (cost != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(subtitle, fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
                    Text(cost, fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = StitchColors.OnSurface)
                }
            } else {
                Text(subtitle, fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
            }
        }
    }
}

// ── Pie Chart Card (时段占比) ──
@Composable
private fun PieChartCard() {
    StitchCard {
        Text(
            "时段占比",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val stroke = 18.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)
                    // rest 72%, drive 19%, charge 9%  (start at top, -90°)
                    var start = -90f
                    val slices = listOf(BrandRest to 72f, BrandDrive to 19f, BrandCharge to 9f)
                    slices.forEach { (color, pct) ->
                        val sweep = pct / 100f * 360f
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = StitchColors.OnSurfaceVariant)
                    Text("24h", fontSize = 24.sp, fontWeight = FontWeight.Medium, fontFamily = JetBrainsMonoFamily, color = StitchColors.OnSurface)
                }
            }
            Spacer(Modifier.height(24.dp))
            LegendRow(BrandDrive, "驾驶", "19%")
            Spacer(Modifier.height(8.dp))
            LegendRow(BrandCharge, "充电", "9%")
            Spacer(Modifier.height(8.dp))
            LegendRow(BrandRest, "休息", "72%")
        }
    }
}

@Composable
private fun LegendRow(dot: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(dot))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = StitchColors.OnSurface)
        }
        Text(value, fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = StitchColors.OnSurface)
    }
}

// ── Weekly Chart Card (周对比) ──
private data class WeekBar(val rest: Int, val drive: Int, val charge: Int, val label: String, val active: Boolean = false, val dim: Boolean = false)

// TODO: replace with real weekly aggregation.
private val mockWeek = listOf(
    WeekBar(48, 16, 8, "一"),
    WeekBar(40, 24, 16, "二"),
    WeekBar(56, 32, 12, "三", active = true),
    WeekBar(64, 8, 0, "四", dim = true),
    WeekBar(48, 20, 12, "五", dim = true),
    WeekBar(32, 40, 24, "六", dim = true),
    WeekBar(40, 16, 0, "日", dim = true)
)

@Composable
private fun WeeklyChartCard() {
    StitchCard {
        Text(
            "周对比",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            mockWeek.forEach { bar ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val alpha = if (bar.dim) 0.4f else 1f
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (bar.active) Modifier.padding(1.dp) else Modifier
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bar.rest.dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(BrandRest.copy(alpha = alpha))
                        )
                        Spacer(Modifier.height(2.dp))
                        if (bar.drive > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(bar.drive.dp)
                                    .background(BrandDrive.copy(alpha = alpha))
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        if (bar.charge > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(bar.charge.dp)
                                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .background(BrandCharge.copy(alpha = alpha))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        bar.label,
                        fontSize = 10.sp,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = if (bar.active) FontWeight.Bold else FontWeight.Normal,
                        color = if (bar.active) StitchColors.Primary else StitchColors.OnSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StitchColors.OutlineVariant))
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("平均活跃", fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
            Text("6h 12min", fontSize = 16.sp, fontFamily = JetBrainsMonoFamily, color = StitchColors.Primary)
        }
    }
}
