package com.teslamatelink.ui.statistics

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.components.StitchDataRow
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

/**
 * 里程钻取 - 年度总览页 (Stitch 白色瑞士风 1:1 还原)。
 * 层级: 年度总览 -> 月度详情 (onMonthClick) -> 当日行程。
 * 内容: 年度总里程大数字 / 12月柱状趋势 / 场景分布 / 365天热力网格 / Top5 里程日。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onMonthClick: (year: Int, month: Int) -> Unit,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "里程钻取",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = StitchColors.OnSurface) }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "未知错误",
                        color = StitchColors.Error,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                val months = uiState.months

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(0.dp))

                    // 1. 年度总里程大数字
                    YearTotalCard(
                        totalKm = uiState.totalKm,
                        totalKwh = uiState.totalKwh,
                        totalDrives = uiState.totalDrives
                    )

                    // 2. 月度趋势 (12 月柱状)
                    MonthlyTrendCard(months = months, onMonthClick = onMonthClick)

                    // 3. 场景分布 (通勤/出行/出差/其他)
                    SceneDistributionCard(totalKm = uiState.totalKm)

                    // 4. 365 天热力网格
                    HeatmapCard()

                    // 5. Top5 里程日
                    TopDaysCard()

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// MARK: - 1. 年度总里程
@Composable
private fun YearTotalCard(totalKm: Int, totalKwh: Double, totalDrives: Int) {
    StitchCard {
        Text(
            "2026 年度总里程",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$totalKm",
                color = StitchColors.OnSurface,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "km",
                color = StitchColors.OnSurfaceVariant,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        StitchDataRow(label = "总能耗", value = "${totalKwh.toInt()} kWh")
        Spacer(Modifier.height(8.dp))
        StitchDataRow(label = "总行程", value = "$totalDrives 次")
    }
}

// MARK: - 2. 月度趋势柱状
@Composable
private fun MonthlyTrendCard(months: List<MonthSummary>, onMonthClick: (Int, Int) -> Unit) {
    StitchCard {
        Text(
            "月度趋势",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(16.dp))

        if (months.isEmpty()) {
            Text("暂无行驶数据", color = StitchColors.OnSurfaceVariant, fontSize = 14.sp)
            return@StitchCard
        }

        val maxKm = (months.maxOfOrNull { it.km } ?: 1).coerceAtLeast(1)
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            months.sortedBy { it.month }.forEach { m ->
                Column(
                    modifier = Modifier.weight(1f).clickable { onMonthClick(m.year, m.month) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val ratio = m.km.toFloat() / maxKm
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((110 * ratio).coerceAtLeast(4f).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(StitchColors.OnSurface)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${m.month}",
                        color = StitchColors.OnSurfaceVariant,
                        fontSize = 9.sp,
                        fontFamily = JetBrainsMonoFamily
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "点击柱状钻取至月度详情",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

// MARK: - 3. 场景分布
private data class Scene(val name: String, val ratio: Float)

@Composable
private fun SceneDistributionCard(totalKm: Int) {
    // TODO: 场景分布数据待接入 CarRepository (drive.tags/geofence 分类)，暂用 mock 比例
    val scenes = listOf(
        Scene("通勤", 0.45f),
        Scene("出行", 0.30f),
        Scene("出差", 0.15f),
        Scene("其他", 0.10f)
    )
    StitchCard {
        Text(
            "场景分布",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(16.dp))
        scenes.forEach { s ->
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.name, color = StitchColors.OnSurface, fontSize = 14.sp)
                    Text(
                        "${(totalKm * s.ratio).toInt()} km",
                        color = StitchColors.OnSurface,
                        fontSize = 14.sp,
                        fontFamily = JetBrainsMonoFamily
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StitchColors.SurfaceContainerHigh)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(s.ratio)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StitchColors.OnSurface)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// MARK: - 4. 365 天热力网格
@Composable
private fun HeatmapCard() {
    // TODO: 每日里程强度待接入 CarRepository，暂用确定性 mock（伪随机 seed）
    val days = remember365Intensity()
    StitchCard {
        Text(
            "全年活跃热力",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(16.dp))
        // 53 周 x 7 天, 每周一列
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            for (week in 0 until 53) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (dow in 0 until 7) {
                        val idx = week * 7 + dow
                        val intensity = if (idx < days.size) days[idx] else -1f
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(heatColor(intensity))
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("少", color = StitchColors.OnSurfaceVariant, fontSize = 10.sp)
            Spacer(Modifier.width(6.dp))
            listOf(0.1f, 0.4f, 0.7f, 1.0f).forEach {
                Box(
                    Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(heatColor(it))
                )
                Spacer(Modifier.width(3.dp))
            }
            Text("多", color = StitchColors.OnSurfaceVariant, fontSize = 10.sp)
        }
    }
}

private fun heatColor(intensity: Float): Color = when {
    intensity < 0f -> Color(0xFFF1EDEC)      // 无数据
    intensity < 0.25f -> Color(0xFFD6D3D1)
    intensity < 0.5f -> Color(0xFF9CA3AF)
    intensity < 0.75f -> Color(0xFF4B5563)
    else -> Color(0xFF1C1B1B)
}

private fun remember365Intensity(): List<Float> {
    // 确定性伪随机: 保证重组稳定, 无需真实数据
    var seed = 20260707L
    return List(365) {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        val v = ((seed ushr 33).toInt() and 0xFF) / 255f
        v
    }
}

// MARK: - 5. Top5 里程日
@Composable
private fun TopDaysCard() {
    // TODO: Top5 里程日待接入 CarRepository (按日聚合排序)，暂用 mock
    val topDays = listOf(
        "2026-05-02" to 412,
        "2026-04-18" to 386,
        "2026-06-11" to 355,
        "2026-03-27" to 328,
        "2026-05-30" to 301
    )
    StitchCard {
        Text(
            "Top5 里程日",
            color = StitchColors.OnSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(16.dp))
        topDays.forEachIndexed { i, (date, km) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchColors.OnSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            color = StitchColors.OnPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFamily
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(date, color = StitchColors.OnSurface, fontSize = 14.sp, fontFamily = JetBrainsMonoFamily)
                }
                Text(
                    "$km km",
                    color = StitchColors.OnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily
                )
            }
        }
    }
}
