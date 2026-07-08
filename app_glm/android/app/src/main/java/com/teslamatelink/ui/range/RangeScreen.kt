package com.teslamatelink.ui.range

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Stitch semantic colors (from design-tokens: estimate/actual/deviation) ──
private val Estimate = Color(0xFF3B82F6)
private val Actual = Color(0xFF059669)
private val Deviation = Color(0xFFF59E0B)

// TODO: 数据层未提供以下分析字段，暂用 mock 对照 Stitch HTML 还原。
//       接入 RangeViewModel 后替换为真实统计。
private data class RangeAnalysis(
    val estimatedKm: Int,
    val actualKm: Int,
    val deviationKm: Int,
    val deviationPct: Double,
    val achievementPct: Double,
    val rating: String,
    val ratingLabel: String,
    val betterThanPct: Int
)

private data class RangeFactor(
    val label: String,
    val value: String,
    val color: Color
)

private data class SeasonBar(
    val label: String,
    val value: Int,
    val ratio: Float,
    val isLow: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeScreen(
    onBack: () -> Unit
) {
    // TODO: mock 数据，等 ViewModel 提供真实续航分析后替换
    val analysis = remember {
        RangeAnalysis(
            estimatedKm = 312,
            actualKm = 286,
            deviationKm = -26,
            deviationPct = -8.3,
            achievementPct = 91.7,
            rating = "A-",
            ratingLabel = "优秀级别",
            betterThanPct = 78
        )
    }
    val factors = remember {
        listOf(
            RangeFactor("外界温度", "-12%", Deviation),
            RangeFactor("空调热泵", "-8%", StitchColors.Secondary),
            RangeFactor("高速行驶", "-15%", StitchColors.Primary),
            RangeFactor("急加/减速", "-6%", Actual)
        )
    }
    val seasons = remember {
        listOf(
            SeasonBar("春季", 315, 0.75f, false),
            SeasonBar("夏季", 302, 0.70f, false),
            SeasonBar("秋季", 328, 0.85f, false),
            SeasonBar("冬季", 268, 0.55f, true)
        )
    }
    var selectedPeriod by remember { mutableStateOf("30天") }

    Scaffold(
        containerColor = StitchColors.Surface,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchColors.Surface)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "续航分析",
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
                                tint = StitchColors.OnSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: refresh */ }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "刷新",
                                tint = StitchColors.OnSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = StitchColors.Surface
                    )
                )
                // Period chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("7天", "30天", "90天").forEach { period ->
                        PeriodChip(
                            text = period,
                            selected = period == selectedPeriod,
                            onClick = { selectedPeriod = period }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── 预估 vs 实际偏差卡 ──
            ComparisonCard(analysis)

            // ── 综合续航评级 + 同车型相对位置 ──
            RatingCard(analysis)

            // ── 续航偏差分布（散点） ──
            DistributionCard()

            // ── 续航影响因素列表 ──
            FactorsCard(factors)

            // ── 30天续航衰减趋势 ──
            TrendCard()

            // ── 季节续航对比 ──
            SeasonCard(seasons)
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (selected) Modifier.background(StitchColors.Primary)
                else Modifier.border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = if (selected) StitchColors.OnPrimary else StitchColors.OnSurface
        )
    }
}

@Composable
private fun ComparisonCard(analysis: RangeAnalysis) {
    StitchCard {
        Text(
            "预估 VS 实际续航",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Estimate ring: 312 km, offset 50/251.2 ≈ 80% filled
                RingChart(
                    value = analysis.estimatedKm,
                    ringColor = Estimate,
                    valueColor = StitchColors.OnSurface,
                    fillFraction = 0.80f
                )
                // Actual ring: 286 km, offset 85/251.2 ≈ 66% filled
                RingChart(
                    value = analysis.actualKm,
                    ringColor = Actual,
                    valueColor = Actual,
                    fillFraction = 0.66f
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${analysis.deviationKm} km",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily,
                    color = Deviation
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchColors.ErrorContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${analysis.deviationPct}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnErrorContainer
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "本周期实际续航达成率 ${analysis.achievementPct}%",
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

@Composable
private fun RingChart(
    value: Int,
    ringColor: Color,
    valueColor: Color,
    fillFraction: Float
) {
    Box(
        modifier = Modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            // Track
            drawArc(
                color = StitchColors.SurfaceContainerHighest,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            // Progress (start at top, -90°)
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * fillFraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$value",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = valueColor
            )
            Text(
                "km",
                fontSize = 10.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun RatingCard(analysis: RangeAnalysis) {
    StitchCard {
        Text(
            "续航效率评级",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                analysis.rating,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                analysis.ratingLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = Actual
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "优于同车型 ",
                fontSize = 14.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Text(
                "${analysis.betterThanPct}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StitchColors.Primary
            )
            Text(
                " 的用户",
                fontSize = 14.sp,
                color = StitchColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun DistributionCard() {
    // TODO: mock 散点，接入真实 (预估,实际) 数据点后替换
    val points = remember {
        listOf(
            Triple(0.20f, 0.85f, Actual),
            Triple(0.35f, 0.70f, Actual),
            Triple(0.50f, 0.60f, Deviation),
            Triple(0.65f, 0.45f, Actual),
            Triple(0.80f, 0.35f, StitchColors.Error),
            Triple(0.45f, 0.55f, Deviation),
            Triple(0.15f, 0.90f, Actual)
        )
    }
    StitchCard {
        Text(
            "续航偏差分布",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            // Left + bottom axis
            drawLine(StitchColors.Border, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
            drawLine(StitchColors.Border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            // Diagonal dashed reference (bottom-left -> top-right)
            drawLine(
                color = StitchColors.Border,
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            // Scatter points (stroke ring, r=4)
            points.forEach { (fx, fy, c) ->
                drawCircle(
                    color = c,
                    radius = 4.dp.toPx(),
                    center = Offset(size.width * fx, size.height * fy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendDot(Actual, "<5%")
            Spacer(Modifier.width(16.dp))
            LegendDot(Deviation, "5-15%")
            Spacer(Modifier.width(16.dp))
            LegendDot(StitchColors.Error, ">15%")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = StitchColors.OnSurface
        )
    }
}

@Composable
private fun FactorsCard(factors: List<RangeFactor>) {
    StitchCard {
        Text(
            "续航影响因素",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        // 2x2 grid
        factors.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                rowItems.forEach { factor ->
                    FactorTile(factor, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FactorTile(factor: RangeFactor, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            factor.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = StitchColors.OnSurface
        )
        Text(
            factor.value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = JetBrainsMonoFamily,
            color = factor.color
        )
    }
}

@Composable
private fun TrendCard() {
    // TODO: mock 趋势曲线，接入真实衰减序列后替换（理论值 vs 实际值）
    val theory = remember {
        listOf(0.20f, 0.22f, 0.21f, 0.25f, 0.23f, 0.24f, 0.26f, 0.24f, 0.27f, 0.25f, 0.28f)
    }
    val real = remember {
        listOf(0.35f, 0.38f, 0.34f, 0.45f, 0.40f, 0.42f, 0.48f, 0.45f, 0.55f, 0.50f, 0.58f)
    }
    StitchCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    "30天续航衰减趋势",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "满电后续航稳定性分析",
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrendLegend(Estimate, "理论值")
                TrendLegend(Actual, "实际值")
            }
        }
        Spacer(Modifier.height(24.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
        ) {
            fun buildPath(data: List<Float>): Path {
                val p = Path()
                data.forEachIndexed { i, v ->
                    val x = size.width * i / (data.size - 1)
                    val y = size.height * v
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                return p
            }
            drawPath(buildPath(theory), Estimate, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            drawPath(buildPath(real), Actual, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun TrendLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(2.dp)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = StitchColors.OnSurface
        )
    }
}

@Composable
private fun SeasonCard(seasons: List<SeasonBar>) {
    StitchCard {
        Text(
            "季节续航对比 (km)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            seasons.forEach { season ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "${season.value}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = JetBrainsMonoFamily,
                        color = if (season.isLow) StitchColors.Error else StitchColors.OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(season.ratio)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(
                                if (season.isLow) StitchColors.Error
                                else StitchColors.SurfaceContainerHigh
                            )
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        season.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (season.isLow) StitchColors.Error else StitchColors.OnSurface
                    )
                }
            }
        }
    }
}
