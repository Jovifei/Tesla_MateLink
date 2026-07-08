package com.teslamatelink.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Stitch design tokens (heatmap-specific) ───────────────────────────────────
// Cell intensity scale mirrors the Stitch legend (少 → 多).
private val CellEmpty = Color(0xFFE5E2E1) // surface-variant
private val CellLow = Color(0xFF5F5E5E)   // surface-tint
private val CellMid = Color(0xFFC8C6C5)   // primary-fixed-dim
private val CellHigh = Color(0xFF000000)  // primary
private val SparkStroke = Color(0xFF171717)

private const val GRID_COLS = 13
private const val GRID_ROWS = 7

// ── Mock data (matches Stitch design spec; data layer unchanged) ──────────────
// TODO(data): wire heatmap grid + routes to DrivingStats via DelegatingCarRepository.

private data class RouteRank(
    val from: String,
    val to: String,
    val count: String,
    val spark: List<Offset> // points in 0..100 x, 0..20 y space
)

private val routes = listOf(
    RouteRank(
        "Home", "Office", "42 次",
        listOf(Offset(0f, 15f), Offset(20f, 10f), Offset(40f, 18f), Offset(60f, 5f), Offset(80f, 12f), Offset(100f, 8f))
    ),
    RouteRank(
        "Office", "Gym", "18 次",
        listOf(Offset(0f, 10f), Offset(20f, 15f), Offset(40f, 8f), Offset(60f, 18f), Offset(80f, 10f), Offset(100f, 5f))
    ),
    RouteRank(
        "Home", "Supermarket", "12 次",
        listOf(Offset(0f, 12f), Offset(20f, 18f), Offset(40f, 5f), Offset(60f, 15f), Offset(80f, 8f), Offset(100f, 12f))
    )
)

private val segments = listOf("30天", "90天", "全年")

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "热力图",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 选择日期范围 */ }) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "日期",
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
                .padding(top = 24.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            SegmentedControl()
            HeatmapCard()
            DataCardsRow()
            RouteRankingSection()
        }
    }
}

// ── Segmented control (30天 / 90天 / 全年) ─────────────────────────────────────

@Composable
private fun SegmentedControl() {
    var selected by remember { mutableStateOf(1) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, StitchColors.SurfaceContainerHighest, RoundedCornerShape(4.dp))
    ) {
        segments.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) StitchColors.SurfaceContainerLow else StitchColors.SurfaceContainerLowest)
                    .let {
                        if (index < segments.lastIndex) {
                            it.border(0.dp, Color.Transparent)
                        } else it
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) StitchColors.Primary else StitchColors.OnSurfaceVariant
                    )
                }
            }
            if (index < segments.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(StitchColors.SurfaceContainerHighest)
                )
            }
        }
    }
}

// ── Main heatmap card ─────────────────────────────────────────────────────────

@Composable
private fun HeatmapCard() {
    val gridData = remember {
        List(GRID_COLS) { col ->
            List(GRID_ROWS) { row ->
                // Deterministic pseudo-intensity 0..3 (matches Stitch density feel).
                val v = ((col * 7 + row * 3) % 11)
                when {
                    v <= 2 -> 0
                    v <= 5 -> 1
                    v <= 8 -> 2
                    else -> 3
                }
            }
        }
    }

    StitchCard {
        Text(
            text = "驾驶热力分布",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.Primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Contribution grid: 13 columns × 7 rows, w-4 h-4 cells, gap-1.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            gridData.forEach { column ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    column.forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(levelColor(level))
                        )
                    }
                }
            }
        }

        // Legend: 少 [scale] 多
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("少", fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(CellEmpty, CellLow, CellMid, CellHigh).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c)
                    )
                }
            }
            Text("多", fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
        }
    }
}

private fun levelColor(level: Int): Color = when (level) {
    0 -> CellEmpty
    1 -> CellLow
    2 -> CellMid
    else -> CellHigh
}

// ── Data cards (1×2 grid): 高频时段 / 最常目的地 ───────────────────────────────

@Composable
private fun DataCardsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataCard(
            icon = Icons.Filled.Schedule,
            label = "高频时段",
            value = "08:00 - 10:00",
            valueMono = true,
            modifier = Modifier.weight(1f)
        )
        DataCard(
            icon = Icons.Filled.LocationOn,
            label = "最常目的地",
            value = "Office",
            valueMono = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DataCard(
    icon: ImageVector,
    label: String,
    value: String,
    valueMono: Boolean,
    modifier: Modifier = Modifier
) {
    StitchCard(modifier = modifier) {
        Icon(
            icon,
            contentDescription = null,
            tint = StitchColors.Primary,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(24.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = StitchColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (valueMono) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.Primary
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.Primary
            )
        }
    }
}

// ── Route ranking list (常用路线排行) ──────────────────────────────────────────

@Composable
private fun RouteRankingSection() {
    Column {
        Text(
            text = "常用路线排行",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.Primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
                .background(StitchColors.SurfaceContainerLowest)
        ) {
            routes.forEachIndexed { index, route ->
                RouteRow(route)
                if (index < routes.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StitchColors.SurfaceContainerHighest)
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteRow(route: RouteRank) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(route.from, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StitchColors.Primary)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(route.to, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StitchColors.Primary)
            }
            Text(route.count, fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
        }
        Sparkline(
            points = route.spark,
            modifier = Modifier
                .width(96.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StitchColors.SurfaceContainerHighest)
        )
    }
}

@Composable
private fun Sparkline(points: List<Offset>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val sx = size.width / 100f
        val sy = size.height / 20f
        val path = Path().apply {
            moveTo(points.first().x * sx, points.first().y * sy)
            points.drop(1).forEach { lineTo(it.x * sx, it.y * sy) }
        }
        drawPath(
            path = path,
            color = SparkStroke,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
