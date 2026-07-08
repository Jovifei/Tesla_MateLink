package com.teslamatelink.ui.cost

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// TODO: 数据层未接入（不改 CostViewModel/TariffConfig），以下为 mock（对齐 Stitch 设计稿）
private val AcBlue = Color(0xFF3B82F6)
private val DcOrange = Color(0xFFF59E0B)
private val FlatYellow = Color(0xFFFBBF24)

private data class TrendBar(val label: String, val acPct: Float, val dcPct: Float, val current: Boolean = false)
private data class TouRow(val name: String, val time: String, val price: String, val pct: Int, val color: Color)
private data class HabitRow(val title: String, val sub: String?, val subColor: Color?, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostScreen(
    onBack: () -> Unit
) {
    var period by remember { mutableStateOf("本月") }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "成本分析",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.24).sp,
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
                    PeriodSegmented(
                        selected = period,
                        onSelect = { period = it },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.SurfaceContainerLowest,
                    scrolledContainerColor = StitchColors.SurfaceContainerLowest
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── 本月支出总览 ──
            OverviewCard()

            // ── 统计三宫格 ──
            StatsGrid()

            // ── 充电类型拆分 (AC/DC) ──
            AcDcSplitCard()

            // ── 月度成本趋势 (近12月) ──
            TrendCard()

            // ── 分时电价·峰平谷 ──
            TouCard()

            // ── 本月充电习惯 ──
            HabitCard(
                title = "本月充电习惯",
                rows = listOf(
                    HabitRow("最常充电站", null, null, "家充 18次/64%"),
                    HabitRow("最常时段", "节省 ¥38", StitchColors.StatusOnline, "22:00-06:00 谷段"),
                    HabitRow("DC 快充占比", null, null, "33% (同比+5%)"),
                    HabitRow("平均单次充入", null, null, "10.1 kWh")
                )
            )

            // ── 本月驾驶习惯 ──
            HabitCard(
                title = "本月驾驶习惯",
                rows = listOf(
                    HabitRow("总里程", "日均 34.1 km", StitchColors.OnSurfaceVariant, "1,286 km"),
                    HabitRow("能耗成本", "¥186.50", StitchColors.OnSurfaceVariant, "每公里 ¥0.145"),
                    HabitRow("高速占比", "能耗 +22%", StitchColors.Error, "28%"),
                    HabitRow("急加速次数", "能耗 +8%", StitchColors.Error, "12次")
                )
            )

            // ── 节约建议 ──
            SavingsCard()
        }
    }
}

/** 白色简约卡片：白底、1px #E5E5E5 边框、8dp 圆角、24dp 内边距、无阴影 */
@Composable
private fun CostCard(
    modifier: Modifier = Modifier,
    borderColor: Color = StitchColors.Border,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .padding(24.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = StitchColors.OnSurfaceVariant
    )
}

@Composable
private fun PeriodSegmented(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLow)
            .border(1.dp, StitchColors.OutlineVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf("本月", "全年", "全部").forEach { label ->
            val active = label == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) StitchColors.Primary else Color.Transparent)
                    .clickable { onSelect(label) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = if (active) StitchColors.OnPrimary else StitchColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewCard() {
    CostCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            SectionHeader("本月支出")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, StitchColors.Outline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "峰/平/谷",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "¥186.50",
            fontSize = 48.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.96).sp,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurface
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "↓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchColors.StatusOnline)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "vs 上月 ¥210.20 ↓11.2%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.StatusOnline
            )
        }
    }
}

@Composable
private fun StatsGrid() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
    ) {
        StatCell("充电", "42次", Modifier.weight(1f))
        VDivider()
        StatCell("能耗", "284kWh", Modifier.weight(1f))
        VDivider()
        StatCell("均价", "¥0.66/kWh", Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurface
        )
    }
}

@Composable
private fun VDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(72.dp)
            .background(StitchColors.Border)
    )
}

@Composable
private fun AcDcSplitCard() {
    CostCard {
        SectionHeader("充电类型拆分")
        Spacer(Modifier.height(16.dp))
        // 堆叠横条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StitchColors.SurfaceContainerLow)
        ) {
            Box(Modifier.weight(0.53f).fillMaxHeight().background(AcBlue))
            Box(Modifier.weight(0.47f).fillMaxHeight().background(DcOrange))
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SplitLegend(
                dot = AcBlue,
                title = "AC 家充",
                amount = "¥98.30 (53%)",
                sub = "28次 | 7.4kW",
                modifier = Modifier.weight(1f)
            )
            SplitLegend(
                dot = DcOrange,
                title = "DC 快充",
                amount = "¥88.20 (47%)",
                sub = "14次 | 120kW",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SplitLegend(
    dot: Color,
    title: String,
    amount: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dot)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchColors.OnSurface)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurface
        )
        Text(text = sub, fontSize = 14.sp, color = StitchColors.OnSurfaceVariant)
    }
}

@Composable
private fun TrendCard() {
    val bars = remember {
        listOf(
            TrendBar("M1", 0.60f, 0.30f),
            TrendBar("M2", 0.50f, 0.20f),
            TrendBar("M3", 0.70f, 0.15f),
            TrendBar("M4", 0.55f, 0.25f),
            TrendBar("M5", 0.45f, 0.30f),
            TrendBar("M6", 0.60f, 0.35f),
            TrendBar("M7", 0.50f, 0.28f),
            TrendBar("M8", 0.65f, 0.20f),
            TrendBar("M9", 0.58f, 0.22f),
            TrendBar("M10", 0.48f, 0.30f),
            TrendBar("M11", 0.40f, 0.40f),
            TrendBar("NOW", 0.55f, 0.45f, current = true)
        )
    }
    CostCard {
        SectionHeader("月度成本趋势 (近12月)")
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(192.dp)) {
            // 平均线 (AVG ¥175) — 位于图表约 40% 高度处
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 76.dp)
                    .height(1.dp)
                    .background(StitchColors.Accent)
            )
            Text(
                text = "AVG ¥175",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.Accent,
                modifier = Modifier.padding(top = 66.dp, start = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { b -> TrendColumn(b, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TrendColumn(bar: TrendBar, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val barMod = if (bar.current) {
            Modifier
                .width(14.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(1.dp, StitchColors.Primary, RoundedCornerShape(3.dp))
        } else {
            Modifier
                .width(14.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(3.dp))
        }
        // 堆叠柱：底部 AC 蓝，顶部 DC 橙
        Column(
            modifier = barMod,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(Modifier.fillMaxWidth().weight(bar.dcPct).background(DcOrange))
            Box(Modifier.fillMaxWidth().weight(bar.acPct).background(AcBlue))
            val rest = (1f - bar.acPct - bar.dcPct).coerceAtLeast(0.0001f)
            Box(Modifier.fillMaxWidth().weight(rest))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = bar.label,
            fontSize = 10.sp,
            fontWeight = if (bar.current) FontWeight.Bold else FontWeight.Normal,
            color = StitchColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun TouCard() {
    val rows = remember {
        listOf(
            TouRow("峰段", "18-23时", "¥1.0", 38, StitchColors.Error),
            TouRow("平段", "7-18时", "¥0.6", 45, FlatYellow),
            TouRow("谷段", "23-7时", "¥0.3", 17, StitchColors.StatusOnline)
        )
    }
    CostCard {
        SectionHeader("分时电价·峰平谷")
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { r -> TouBar(r) }
        }
    }
}

@Composable
private fun TouBar(r: TouRow) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = r.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchColors.OnSurface)
                Spacer(Modifier.width(8.dp))
                Text(text = r.time, fontSize = 12.sp, color = StitchColors.OnSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = r.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "${r.pct}%", fontSize = 12.sp, color = StitchColors.OnSurfaceVariant)
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(StitchColors.SurfaceContainerLow)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(r.pct / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(r.color)
            )
        }
    }
}

@Composable
private fun HabitCard(title: String, rows: List<HabitRow>) {
    CostCard {
        SectionHeader(title)
        Spacer(Modifier.height(8.dp))
        rows.forEachIndexed { i, row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = row.title, fontSize = 16.sp, color = StitchColors.OnSurface)
                    if (row.sub != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = row.sub,
                            fontSize = 12.sp,
                            fontWeight = if (row.subColor == StitchColors.StatusOnline) FontWeight.Bold else FontWeight.Normal,
                            color = row.subColor ?: StitchColors.OnSurfaceVariant
                        )
                    }
                }
                Text(
                    text = row.value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
            }
            if (i < rows.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(StitchColors.OutlineVariant))
            }
        }
    }
}

@Composable
private fun SavingsCard() {
    CostCard(borderColor = StitchColors.StatusOnline, borderWidth = 2.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "节约建议",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.StatusOnline
            )
        }
        Spacer(Modifier.height(16.dp))
        val tips = listOf(
            "改用谷段充电可省 ¥38/月",
            "家充占比提升至 70% 可省 ¥25",
            "减少急加速可省 ¥15"
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tips.forEach { tip ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .padding(top = 7.dp)
                            .width(6.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StitchColors.StatusOnline)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = tip, fontSize = 16.sp, color = StitchColors.OnSurface)
                }
            }
        }
    }
}
