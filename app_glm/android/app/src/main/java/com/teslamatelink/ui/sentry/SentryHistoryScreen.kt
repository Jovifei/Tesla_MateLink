package com.teslamatelink.ui.sentry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Sentry-specific colors (from Stitch design spec) ──────────────────────────
private val SentryPurple = Color(0xFF7C3AED)
private val AlertRed = Color(0xFFDC2626)
private val SafeGreen = Color(0xFF059669)
private val DarkSwiss = Color(0xFF171717)

// ── Time range tab ────────────────────────────────────────────────────────────
private enum class TimeRange(val label: String) {
    SEVEN_DAYS("7天"), THIRTY_DAYS("30天"), ALL("全部")
}

// ── Mock data (matches Stitch design spec; data layer unchanged) ──────────────
private data class SentryEvent(
    val time: String,
    val type: String,
    val detail: String,
    val color: Color,
    val isAlert: Boolean = false
)

private val timelineEvents = listOf(
    SentryEvent("18:32", "人员靠近", "持续 2min · 置信度 95%", SentryPurple),
    SentryEvent("14:15", "车辆经过", "持续 1min · 置信度 88%", SentryPurple.copy(alpha = 0.5f)),
    SentryEvent("09:48", "震动检测", "严重程度: 中 · 置信度 92%", AlertRed, isAlert = true),
    SentryEvent("08:20", "人员靠近", "持续 30s · 置信度 99%", SentryPurple.copy(alpha = 0.3f))
)

private data class DistributionItem(val label: String, val count: Int, val color: Color)
private val distribution = listOf(
    DistributionItem("人员靠近", 12, SentryPurple),
    DistributionItem("车辆经过", 10, DarkSwiss),
    DistributionItem("震动检测", 2, AlertRed)
)

// Hourly histogram (12 bars, 2-hour buckets)
private val hourBars = listOf(
    4, 2, 1, 3, 6, 20, 16, 24, 14, 8, 5, 3
)

private enum class Sensitivity(val label: String, val desc: String) {
    LOW("低", "仅检测剧烈撞击或玻璃破碎"),
    MEDIUM("中", "推荐：检测人员停留及靠近"),
    HIGH("高", "记录所有移动物体及微小光亮")
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentryHistoryScreen(
    onNavigateBack: () -> Unit
) {
    var selectedRange by remember { mutableStateOf(TimeRange.SEVEN_DAYS) }
    var selectedSensitivity by remember { mutableStateOf(Sensitivity.MEDIUM) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "哨兵历史",
                        fontSize = 24.sp,
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
                    TimeRangeTabs(selectedRange) { selectedRange = it }
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
            // -- Summary Card --
            SummaryCard()

            // -- Stats Grid --
            StatsGrid()

            // -- Distribution Chart --
            DistributionCard()

            // -- Recent Events Timeline --
            TimelineSection()

            // -- Time Distribution Chart --
            TimeDistributionCard()

            // -- Storage Card --
            StorageCard()

            // -- Sensitivity Settings --
            SensitivitySection(selectedSensitivity) { selectedSensitivity = it }

            // -- Action Buttons --
            ActionButtons()
        }
    }
}

// ── Time Range Tabs ───────────────────────────────────────────────────────────

@Composable
private fun TimeRangeTabs(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.SurfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimeRange.entries.forEach { range ->
                val isSelected = range == selected
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) DarkSwiss else Color.Transparent,
                    modifier = Modifier.clickable { onSelect(range) }
                ) {
                    Text(
                        text = range.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = if (isSelected) StitchColors.White else StitchColors.OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ── Summary Card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "7天哨兵事件",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "24",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    buildAnnotatedString(),
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = SentryPurple
            )
        }
    }
}

@Composable
private fun buildAnnotatedString(): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append("日均 ")
        pushStyle(androidx.compose.ui.text.SpanStyle(fontFamily = JetBrainsMonoFamily))
        append("3.4")
        pop()
        append(" 次 · 消耗 ")
        pushStyle(androidx.compose.ui.text.SpanStyle(fontFamily = JetBrainsMonoFamily))
        append("2.1")
        pop()
        append(" kWh")
    }
}

// ── Stats Grid ────────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard("触发", "18次", DarkSwiss, Modifier.weight(1f))
        StatCard("误报", "6次", AlertRed, Modifier.weight(1f))
        StatCard("持续时长", "8h", StitchColors.OnSurface, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = valueColor
            )
        }
    }
}

// ── Distribution Card (Donut + Legend) ───────────────────────────────────────

@Composable
private fun DistributionCard() {
    StitchCard {
        Text(
            text = "事件类型分布",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart
            DonutChart(
                segments = distribution.map { it.count.toFloat() to it.color },
                modifier = Modifier.size(128.dp)
            )
            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                distribution.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(item.color, RoundedCornerShape(50))
                        )
                        Text(
                            text = item.label,
                            fontSize = 14.sp,
                            color = StitchColors.OnSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.count}次",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier
) {
    val total = segments.sumOf { it.first.toDouble() }.toFloat()
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier) {
            val strokeWidth = 16.dp.toPx()
            val diameter = minOf(size.width, size.height) - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f
            segments.forEach { (fraction, color) ->
                val sweepAngle = (fraction / total) * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        Text(
            text = "100%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurface
        )
    }
}

// ── Timeline Section ──────────────────────────────────────────────────────────

@Composable
private fun TimelineSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "最近哨兵事件",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            timelineEvents.forEachIndexed { index, event ->
                TimelineItem(event, isLast = index == timelineEvents.lastIndex)
            }
        }
    }
}

@Composable
private fun TimelineItem(event: SentryEvent, isLast: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeline dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .background(event.color, RoundedCornerShape(50))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(StitchColors.Border)
                )
            }
        }
        // Event card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = StitchColors.White,
            border = BorderStroke(1.dp, StitchColors.Border),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = event.time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFamily,
                        color = StitchColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.type,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (event.isAlert) AlertRed else DarkSwiss
                    )
                    Text(
                        text = event.detail,
                        fontSize = 12.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Thumbnail placeholder
                    Surface(
                        modifier = Modifier.size(width = 96.dp, height = 54.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = StitchColors.SurfaceContainerHigh
                    ) {}
                    Text(
                        text = "查看",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = StitchColors.OnSurface
                    )
                }
            }
        }
    }
}

// ── Time Distribution Card (Histogram) ────────────────────────────────────────

@Composable
private fun TimeDistributionCard() {
    val maxValue = hourBars.max()
    StitchCard {
        Text(
            text = "事件时段分布",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Histogram bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            hourBars.forEachIndexed { index, value ->
                val isPeak = value >= 14 // High-frequency bars
                val heightFraction = value.toFloat() / maxValue.toFloat()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp * heightFraction)
                        .background(
                            if (isPeak) SentryPurple else StitchColors.SurfaceContainerHigh,
                            RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "00:00",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurfaceVariant
            )
            Text(
                text = "高频时段 (22-06)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily,
                color = SentryPurple
            )
            Text(
                text = "23:59",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

// ── Storage Card ──────────────────────────────────────────────────────────────

@Composable
private fun StorageCard() {
    StitchCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "录像存储",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Text(
                text = "2.4 GB / 64 GB",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = DarkSwiss
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(StitchColors.SurfaceContainerHigh, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.0375f)
                    .height(8.dp)
                    .background(SentryPurple, RoundedCornerShape(50))
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = StitchColors.OnSurfaceVariant
            )
            Text(
                text = "7天后自动清理旧录像",
                fontSize = 12.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

// ── Sensitivity Section ───────────────────────────────────────────────────────

@Composable
private fun SensitivitySection(
    selected: Sensitivity,
    onSelect: (Sensitivity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "灵敏度设置",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Sensitivity.entries.forEach { level ->
                val isSelected = level == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(level) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StitchColors.SurfaceContainerLow else StitchColors.White,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) DarkSwiss else StitchColors.Border
                    ),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Box {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = level.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = StitchColors.OnSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = level.desc,
                                fontSize = 10.sp,
                                color = if (isSelected) DarkSwiss else StitchColors.OnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                lineHeight = 14.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(12.dp),
                                tint = DarkSwiss
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Action Buttons ────────────────────────────────────────────────────────────

@Composable
private fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Export button
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = StitchColors.White,
            border = BorderStroke(1.dp, DarkSwiss),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "导出事件",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurface
                )
            }
        }
        // Clear history button
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = StitchColors.White,
            border = BorderStroke(1.dp, AlertRed),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AlertRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "清空历史",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = AlertRed
                )
            }
        }
    }
}
