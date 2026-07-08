package com.teslamatelink.ui.efficiency

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// TODO: 数据层未接入，以下为 mock（对齐 Stitch 设计稿）
private val trendData = listOf(165f, 158f, 160f, 150f, 148f, 155f, 152f)
private val trendLabels = listOf("1", "5", "10", "15", "20", "25", "30")
private const val fleetAvg = 160f

private data class Breakdown(val label: String, val pct: Int, val color: Color)
private data class CompareRow(val label: String, val value: String, val fraction: Float, val color: Color, val valueColor: Color)
private data class Advice(val title: String, val desc: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfficiencyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "能耗分析",
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
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "设置",
                            tint = StitchColors.OnSurface
                        )
                    }
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
            // ── 关键指标 Bento (平均能耗 + 能效评级) ──
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                MetricCard(Modifier.weight(1f)) {
                    SectionHeader("bolt", "平均能耗")
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "154",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.Primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Wh/km",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.StatusOnline
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "-5.2%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.StatusOnline
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "vs 上月",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
                MetricCard(Modifier.weight(1f)) {
                    SectionHeader("star", "能效评级")
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "A+",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.96).sp,
                        color = StitchColors.Accent
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "优于 ",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                        Text(
                            text = "92%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.Primary
                        )
                        Text(
                            text = " 的车主",
                            fontSize = 14.sp,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
            }

            // ── 近30天能耗趋势 曲线 ──
            MetricCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("show_chart", "近30天能耗趋势")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RangeChip("7天", selected = false)
                        RangeChip("30天", selected = true)
                    }
                }
                Spacer(Modifier.height(24.dp))
                TrendChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(256.dp)
                )
            }

            // ── 能耗分布 ──
            MetricCard {
                SectionHeader("bar_chart", "能耗分布")
                Spacer(Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(
                        Breakdown("行驶", 75, StitchColors.Primary),
                        Breakdown("空调", 15, StitchColors.Outline),
                        Breakdown("待机", 7, StitchColors.OutlineVariant),
                        Breakdown("其他", 3, StitchColors.SurfaceTint)
                    ).forEach { b ->
                        BreakdownBar(b)
                    }
                }
            }

            // ── 同车型能耗对比 ──
            MetricCard {
                SectionHeader("compare_arrows", "同车型能耗对比")
                Spacer(Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(
                        CompareRow("你的", "154", 0.65f, StitchColors.Primary, StitchColors.OnSurface),
                        CompareRow("平均", "168", 0.80f, StitchColors.OutlineVariant, StitchColors.OnSurface),
                        CompareRow("最佳", "138", 0.45f, StitchColors.StatusOnline, StitchColors.StatusOnline)
                    ).forEach { c ->
                        CompareBar(c)
                    }
                }
            }

            // ── 优化建议 ──
            MetricCard {
                SectionHeader("lightbulb", "优化建议")
                Spacer(Modifier.height(24.dp))
                val advice = listOf(
                    Triple("速度", "减少高速超速", "控制车速在 110km/h 内，可节省约 10% 能耗。"),
                    Triple("空调", "优化空调使用", "建议设置自动模式 22°C，避免频繁手动大风量。"),
                    Triple("胎压", "保持胎压正常", "当前左前胎压偏低，补充至 2.9 bar 可减少滚阻。")
                )
                advice.forEachIndexed { i, (icon, title, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = if (i == 0) 0.dp else 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = when (icon) {
                                "速度" -> "⚡"
                                "空调" -> "❄"
                                else -> "◎"
                            },
                            fontSize = 16.sp,
                            color = StitchColors.Primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchColors.OnSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 14.sp,
                                color = StitchColors.OnSurfaceVariant
                            )
                        }
                    }
                    if (i < advice.lastIndex) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(StitchColors.SurfaceContainerHighest)
                        )
                    }
                }
            }
        }
    }
}

/** 白色简约卡片：白底、1px #E5E5E5 边框、8px 圆角、24px 内边距、无阴影 */
@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchColors.SurfaceContainerLowest)
            .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
            .padding(24.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(icon: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = when (icon) {
                "bolt" -> "⚡"
                "star" -> "★"
                "show_chart" -> "📈"
                "bar_chart" -> "▊"
                "compare_arrows" -> "⇄"
                "lightbulb" -> "💡"
                else -> ""
            },
            fontSize = 14.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = StitchColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) StitchColors.Primary else Color.Transparent)
            .border(
                1.dp,
                if (selected) StitchColors.Primary else StitchColors.SurfaceContainerHighest,
                RoundedCornerShape(4.dp)
            )
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = if (selected) StitchColors.OnPrimary else StitchColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownBar(b: Breakdown) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(b.label, fontSize = 14.sp, color = StitchColors.OnSurface)
            Text(
                text = "${b.pct}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurface
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StitchColors.SurfaceContainerHighest)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(b.pct / 100f)
                    .height(8.dp)
                    .background(b.color)
            )
        }
    }
}

@Composable
private fun CompareBar(c: CompareRow) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(c.label, fontSize = 14.sp, color = StitchColors.OnSurface)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = c.value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = c.valueColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Wh/km",
                    fontSize = 12.sp,
                    color = c.valueColor,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StitchColors.SurfaceContainerHighest)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(c.fraction)
                    .height(8.dp)
                    .background(c.color)
            )
        }
    }
}

/** 能耗趋势折线：金色 #A16207 主线 + 车队平均虚线，纯 Compose Canvas */
@Composable
private fun TrendChart(modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val yMin = 130f
    val yMax = 180f
    Canvas(modifier = modifier) {
        val padL = 34.dp.toPx()
        val padB = 20.dp.toPx()
        val padT = 8.dp.toPx()
        val padR = 8.dp.toPx()
        val chartW = size.width - padL - padR
        val chartH = size.height - padT - padB
        val axisStyle = TextStyle(fontSize = 10.sp, color = StitchColors.Outline, fontFamily = JetBrainsMonoFamily)

        fun yToPx(v: Float) = padT + chartH * (1f - (v - yMin) / (yMax - yMin))
        fun xToPx(i: Int) = padL + chartW * (i.toFloat() / (trendData.size - 1))

        // Y 轴网格 + 标签 (130..180 step 10)
        var yv = yMin
        while (yv <= yMax) {
            val y = yToPx(yv)
            drawLine(
                StitchColors.SurfaceContainer,
                Offset(padL, y),
                Offset(size.width - padR, y),
                strokeWidth = 1f
            )
            val lbl = textMeasurer.measure(yv.toInt().toString(), axisStyle)
            drawText(lbl, topLeft = Offset(padL - lbl.size.width - 6.dp.toPx(), y - lbl.size.height / 2))
            yv += 10f
        }

        // X 轴标签
        trendLabels.forEachIndexed { i, label ->
            val x = xToPx(i)
            val lbl = textMeasurer.measure(label, axisStyle)
            drawText(lbl, topLeft = Offset(x - lbl.size.width / 2, size.height - padB + 4.dp.toPx()))
        }

        // 车队平均虚线 (160)
        val avgY = yToPx(fleetAvg)
        drawLine(
            StitchColors.OutlineVariant,
            Offset(padL, avgY),
            Offset(size.width - padR, avgY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        // 主曲线 (金色)
        val path = Path()
        trendData.forEachIndexed { i, v ->
            val x = xToPx(i)
            val y = yToPx(v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = StitchColors.Accent, style = Stroke(width = 2.dp.toPx()))

        // 数据点 (白心金环)
        trendData.forEachIndexed { i, v ->
            val x = xToPx(i)
            val y = yToPx(v)
            drawCircle(StitchColors.Accent, radius = 3.dp.toPx(), center = Offset(x, y))
            drawCircle(StitchColors.SurfaceContainerLowest, radius = 1.5.dp.toPx(), center = Offset(x, y))
        }
    }
}
