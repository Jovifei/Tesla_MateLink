package com.teslamatelink.ui.updates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

data class SoftwareVersion(
    val version: String,
    val releaseNotes: String,
    val isInstalled: Boolean,
    val date: String,
    val sizeGb: String = "1.0GB",
    val durationMinutes: Int = 40,
    val isSuccess: Boolean = true
)

private val WarningColor = Color(0xFFF59E0B)
private val SuccessColor = Color(0xFF059669)
private val ErrorColor = Color(0xFFDC2626)
private val MediaColor = Color(0xFFA16207)
private val NavColor = Color(0xFFA16207)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    onNavigateBack: () -> Unit
) {
    val versions = remember {
        listOf(
            SoftwareVersion("2025.20.6", "Improved sentry mode battery optimization", true, "06-15", "1.1GB", 42, true),
            SoftwareVersion("2025.16.5", "Enhanced autopilot visualization", false, "05-20", "0.9GB", 38, true),
            SoftwareVersion("2025.12.1", "New energy app with charging stats", false, "04-10", "1.3GB", 55, true),
            SoftwareVersion("2025.8.2", "Bug fixes and performance improvements", false, "03-05", "0.8GB", 35, false),
            SoftwareVersion("2025.4.1", "Initial release", false, "02-12", "1.0GB", 45, true)
        )
    }
    val current = versions.first()
    val totalUpdates = versions.size
    val firstVersion = versions.last().version
    val totalHours = versions.sumOf { it.durationMinutes } / 60.0
    var installing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StitchColors.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchColors.White),
                title = {
                    Text(
                        text = "固件版本",
                        color = StitchColors.OnSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.24).sp
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
                    IconButton(onClick = { /* refresh */ }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            tint = StitchColors.OnSurface
                        )
                    }
                }
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
            // ── Current Version Card ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
                    .background(StitchColors.White, RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(
                            "最新",
                            color = SuccessColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier
                                .background(SuccessColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        "当前版本",
                        color = StitchColors.OnSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        current.version,
                        color = StitchColors.OnSurface,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFamily,
                        letterSpacing = (-0.84).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "已安装于",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Text(
                            current.date,
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 14.sp,
                            fontFamily = JetBrainsMonoFamily
                        )
                        Box(modifier = Modifier.size(4.dp).background(StitchColors.Border, androidx.compose.foundation.shape.CircleShape))
                        Text(
                            "Model 3 2022",
                            color = StitchColors.OnSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Stats Grid ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "总更新",
                    value = "$totalUpdates",
                    suffix = "次",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "首次安装",
                    value = firstVersion,
                    modifier = Modifier.weight(1f),
                    smallValue = true
                )
                StatCard(
                    label = "累计时长",
                    value = String.format("%.0f", totalHours),
                    suffix = "h",
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Update Available Card ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningColor, RoundedCornerShape(8.dp))
                    .background(WarningColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(20.dp))
                                Text(
                                    "新版本可用",
                                    color = WarningColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "2025.24.8",
                                color = StitchColors.OnSurface,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFamily
                            )
                            Text(
                                "大小: 1.2GB",
                                color = StitchColors.OnSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            "发布说明 ▼",
                            color = StitchColors.OnSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { installing = true },
                            enabled = !installing,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StitchColors.Primary,
                                contentColor = StitchColors.OnPrimary
                            )
                        ) {
                            Text(
                                if (installing) "准备中..." else "立即安装",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StitchColors.White,
                                contentColor = StitchColors.OnSurface
                            ),
                            border = BorderStroke(1.dp, StitchColors.Border)
                        ) {
                            Text("预约安装", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Update History Timeline ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
                    .background(StitchColors.White, RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "更新历史",
                        color = StitchColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    versions.forEachIndexed { index, v ->
                        TimelineItem(
                            version = v,
                            isLast = index == versions.lastIndex
                        )
                    }
                }
            }

            // ── Charts Grid ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature Distribution
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
                        .background(StitchColors.White, RoundedCornerShape(8.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "年度功能更新",
                            color = StitchColors.OnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            val segments = listOf(
                                0.32f to StitchColors.OnSurface,
                                0.25f to SuccessColor,
                                0.18f to MediaColor,
                                0.15f to WarningColor,
                                0.10f to StitchColors.SurfaceContainerHigh
                            )
                            DonutChart(segments = segments, centerText = "100%")
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem(color = StitchColors.OnSurface, label = "自动驾驶", value = "32%")
                                LegendItem(color = SuccessColor, label = "安全", value = "25%")
                                LegendItem(color = MediaColor, label = "媒体", value = "18%")
                                LegendItem(color = WarningColor, label = "导航", value = "15%")
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
                    .background(StitchColors.White, RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "安装时段分布",
                        color = StitchColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        DistributionBar(label = "凌晨", sublabel = "0-4时", percent = 78, barColor = StitchColors.Primary)
                        DistributionBar(label = "白天", sublabel = "20-22时", percent = 15, barColor = StitchColors.Secondary)
                        DistributionBar(label = "其他", sublabel = "", percent = 7, barColor = StitchColors.Border)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    suffix: String = "",
    modifier: Modifier = Modifier,
    smallValue: Boolean = false
) {
    Box(
        modifier = modifier
            .border(1.dp, StitchColors.Border, RoundedCornerShape(8.dp))
            .background(StitchColors.White, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                label.uppercase(),
                color = StitchColors.OnSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    value,
                    color = StitchColors.OnSurface,
                    fontSize = if (smallValue) 14.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily
                )
                if (suffix.isNotEmpty()) {
                    Text(
                        suffix,
                        color = StitchColors.OnSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(version: SoftwareVersion, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .drawBehind {
                    if (!isLast) {
                        val lineY = size.height
                        drawLine(
                            color = StitchColors.Border,
                            start = Offset(center.x, lineY),
                            end = Offset(center.x, lineY + 32.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                .border(
                    1.dp,
                    if (version.isSuccess) StitchColors.OnSurface else ErrorColor,
                    androidx.compose.foundation.shape.CircleShape
                )
                .background(StitchColors.White, androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(
                if (version.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (version.isSuccess) SuccessColor else ErrorColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    version.version,
                    color = StitchColors.OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily
                )
                Text(
                    if (version.isSuccess) "SUCCESS" else "RETRY",
                    color = if (version.isSuccess) SuccessColor else ErrorColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFamily
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${version.date} • ${version.sizeGb} • ${version.durationMinutes}min",
                color = StitchColors.OnSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = JetBrainsMonoFamily,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun DonutChart(segments: List<Pair<Float, Color>>, centerText: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(128.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            var startAngle = -90f
            segments.forEach { (fraction, color) ->
                val sweep = fraction * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        Text(
            centerText,
            color = StitchColors.OnSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFamily
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
            Text(label, color = StitchColors.OnSurface, fontSize = 14.sp)
        }
        Text(value, color = StitchColors.OnSurface, fontSize = 14.sp, fontFamily = JetBrainsMonoFamily)
    }
}

@Composable
private fun DistributionBar(label: String, sublabel: String, percent: Int, barColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(label, color = StitchColors.OnSurface, fontSize = 14.sp)
                if (sublabel.isNotEmpty()) {
                    Text(
                        " $sublabel",
                        color = StitchColors.OnSurfaceVariant,
                        fontSize = 14.sp,
                        fontFamily = JetBrainsMonoFamily
                    )
                }
            }
            Text(
                "$percent%",
                color = StitchColors.OnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(StitchColors.SurfaceContainerLow, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}
