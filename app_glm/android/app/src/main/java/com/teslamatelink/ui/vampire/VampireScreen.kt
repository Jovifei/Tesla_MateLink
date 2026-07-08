package com.teslamatelink.ui.vampire

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.ExtensionOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.geometry.Offset
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

// ── Mock data (matches Stitch design spec; data layer unchanged) ──────────────
// TODO: 接入待机摘要真实数据（standby summary / vampire drain 计算结果）

private data class DrainSource(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val value: String
)

private val drainSources = listOf(
    DrainSource(Icons.Filled.Videocam, "哨兵模式", "12 events", "-0.45%"),
    DrainSource(Icons.Filled.AcUnit, "温度预调节", "45 mins", "-0.22%"),
    DrainSource(Icons.Filled.Smartphone, "应用唤醒", "8 API calls", "-0.13%")
)

private data class AdviceCard(
    val icon: ImageVector,
    val title: String,
    val desc: String
)

private val adviceCards = listOf(
    AdviceCard(Icons.Filled.Nature, "尽量停在阴凉处", "降低电池过热唤醒频率"),
    AdviceCard(Icons.Outlined.ExtensionOff, "减少第三方插件", "防止非必要 API 轮询唤醒")
)

// 24h 电量曲线（模拟）：SoC 百分比随时间下降
private val socCurve = listOf(80f, 78f, 74f, 70f, 66f, 62f, 58f, 55f, 52f, 50f, 48f, 46f)

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VampireScreen(
    onBack: () -> Unit
) {
    var smartStandby by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "待机耗电详情",
                        fontSize = 16.sp,
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
                actions = {
                    IconButton(onClick = { /* TODO: 待机设置 */ }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "设置",
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
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            StatsGrid()
            TrendCard()
            BreakdownSection()
            AdviceSection()
            SmartStandbyCard(smartStandby) { smartStandby = it }
        }
    }
}

// ── Stats Grid (2x2) ──────────────────────────────────────────────────────────

@Composable
private fun StatsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("总耗电", "-0.8", "%", Modifier.weight(1f))
            StatCard("待机时长", "12.5", "h", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("效率排名", "85", "%", Modifier.weight(1f))
            StatCard("平均功率", "42", "W", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// ── Trend Card (24h line chart) ───────────────────────────────────────────────

@Composable
private fun TrendCard() {
    StitchCard {
        Text(
            text = "耗电趋势 (24h)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StitchColors.OnSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = StitchColors.SurfaceContainerHighest
                // 横向虚线网格（4 条）
                for (i in 0..3) {
                    val y = size.height * i / 3f
                    var startX = 0f
                    val dash = 6.dp.toPx()
                    val gap = 4.dp.toPx()
                    while (startX < size.width) {
                        drawLine(
                            color = gridColor.copy(alpha = 0.5f),
                            start = Offset(startX, y),
                            end = Offset((startX + dash).coerceAtMost(size.width), y),
                            strokeWidth = 1f
                        )
                        startX += dash + gap
                    }
                }
                // 左轴 + 底轴
                drawLine(gridColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)

                // 金色曲线
                val maxV = socCurve.max()
                val minV = socCurve.min()
                val range = (maxV - minV).coerceAtLeast(1f)
                val path = Path()
                socCurve.forEachIndexed { index, v ->
                    val x = size.width * index / (socCurve.size - 1)
                    val y = size.height * (1f - (v - minV) / range) * 0.85f + size.height * 0.075f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = StitchColors.Accent,
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            // 起点数值标签 80%
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 12.dp),
                shape = RoundedCornerShape(4.dp),
                color = StitchColors.Background,
                border = BorderStroke(1.dp, StitchColors.Border)
            ) {
                Text(
                    text = "80%",
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("08:30", "14:30", "20:30").forEach {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "模拟数据 — 基于待机摘要",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Breakdown Section ──────────────────────────────────────────────────────────

@Composable
private fun BreakdownSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "耗电分布",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.Primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            drainSources.forEach { BreakdownRow(it) }
        }
    }
}

@Composable
private fun BreakdownRow(source: DrainSource) {
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = StitchColors.SurfaceContainerLow,
                    border = BorderStroke(1.dp, StitchColors.Border)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = source.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = StitchColors.OnSurface
                        )
                    }
                }
                Column {
                    Text(
                        text = source.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = StitchColors.OnSurface
                    )
                    Text(
                        text = source.detail,
                        fontSize = 14.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }
            Text(
                text = source.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JetBrainsMonoFamily,
                color = StitchColors.OnSurface
            )
        }
    }
}

// ── Advice Section (2 bento cards) ─────────────────────────────────────────────

@Composable
private fun AdviceSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "优化建议",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = StitchColors.Primary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            adviceCards.forEach { AdviceItem(it, Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun AdviceItem(card: AdviceCard, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = StitchColors.OnSurfaceVariant
            )
            Column {
                Text(
                    text = card.title,
                    fontSize = 16.sp,
                    color = StitchColors.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.desc,
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ── Smart Standby Toggle ────────────────────────────────────────────────────────

@Composable
private fun SmartStandbyCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "智能待机模式",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.Primary
                )
                Text(
                    text = "夜间自动关闭哨兵以节省电量",
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StitchColors.White,
                    checkedTrackColor = StitchColors.Primary,
                    checkedBorderColor = StitchColors.Primary,
                    uncheckedThumbColor = StitchColors.White,
                    uncheckedTrackColor = StitchColors.Border,
                    uncheckedBorderColor = StitchColors.Border
                )
            )
        }
    }
}
