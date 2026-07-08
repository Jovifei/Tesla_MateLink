package com.teslamatelink.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Timeline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.navigation.Routes
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

/**
 * Data class representing an entry on the More screen.
 * [trailingValue] optionally shows a value (battery SOH, firmware version) before the chevron.
 */
data class MoreEntry(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector,
    val trailingValue: String? = null,
    val trailingValueGold: Boolean = false
)

/**
 * Analysis entries — aligned to Stitch more.html "数据分析" group.
 */
private val analysisEntries = listOf(
    MoreEntry("统计", "月度与年度汇总", Routes.STATISTICS, Icons.Outlined.QueryStats),
    MoreEntry("热力图", "驾驶频率与模式", Routes.HEATMAP, Icons.Outlined.Map),
    MoreEntry("效率", "Golden Foot 评分", Routes.EFFICIENCY, Icons.Outlined.EnergySavingsLeaf),
    MoreEntry("续航", "预估 vs 实际", Routes.RANGE, Icons.Outlined.Bolt),
    MoreEntry("能耗分析", "待机能耗分析", Routes.VAMPIRE, Icons.Outlined.NightsStay),
    MoreEntry("电池健康", "电池衰减与 SOH", "battery_health", Icons.Outlined.BatteryChargingFull, trailingValue = "95.8%", trailingValueGold = true),
    MoreEntry("时间线", "充电与行程时间线", "timeline", Icons.Outlined.Timeline),
    MoreEntry("哨兵历史", "哨兵事件与记录", Routes.SENTRY_HISTORY, Icons.Outlined.Security),
)

/**
 * Report / export entries — aligned to Stitch more.html "报告与导出" group.
 */
private val reportEntries = listOf(
    MoreEntry("年度报告 PDF", "年度驾驶总结", "annual_report", Icons.Outlined.PictureAsPdf),
    MoreEntry("数据导出 CSV/JSON", "导出原始数据", "data_export", Icons.Outlined.Download),
    MoreEntry("固件版本", "当前固件版本", "firmware_version", Icons.Outlined.Terminal, trailingValue = "2024.26.7"),
)

/**
 * System entries — aligned to Stitch more.html "系统" group.
 */
private val systemEntries = listOf(
    MoreEntry("设置", "应用偏好", Routes.SETTINGS, Icons.Outlined.Settings),
    MoreEntry("关于", "版本与开源许可", Routes.ABOUT, Icons.Outlined.Info),
)

/**
 * Full Stitch white-minimal "More" screen.
 * Vehicle summary + grouped navigation (analysis / reports / system) + logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "更多",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchColors.OnSurface
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigate("notifications") }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "通知",
                            tint = StitchColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Background,
                    titleContentColor = StitchColors.OnSurface,
                    actionIconContentColor = StitchColors.OnSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // -- Vehicle summary card --
            item {
                VehicleSummaryCard(onClick = { onNavigate("vehicles") })
            }

            // -- 数据分析 section --
            item { SectionHeader("数据分析") }
            items(analysisEntries) { entry ->
                MoreEntryRow(entry = entry, onClick = { onNavigate(entry.route) })
            }

            // -- 报告与导出 section --
            item { SectionHeader("报告与导出") }
            items(reportEntries) { entry ->
                MoreEntryRow(entry = entry, onClick = { onNavigate(entry.route) })
            }

            // -- 系统 section --
            item { SectionHeader("系统") }
            items(systemEntries) { entry ->
                MoreEntryRow(entry = entry, onClick = { onNavigate(entry.route) })
            }

            // -- Logout --
            item {
                LogoutButton(onClick = { onNavigate("logout") })
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Label-caps section header (uppercase, small, on-surface-variant).
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = StitchColors.OnSurfaceVariant
    )
}

/**
 * Vehicle summary card — TODO: replace placeholder with real car data from ViewModel.
 */
@Composable
private fun VehicleSummaryCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle icon box
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                color = StitchColors.SurfaceContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = StitchColors.OnSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tesla Model 3", // TODO: inject real car name
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = "VIN 5YJ3E1...", // TODO: inject real VIN
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            // Online status chip
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                color = StitchColors.StatusOnlineBg
            ) {
                Text(
                    text = "在线",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.StatusOnline,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Single row: icon + title/description + optional trailing value + chevron, with bottom divider.
 */
@Composable
private fun MoreEntryRow(
    entry: MoreEntry,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = entry.title,
                modifier = Modifier.size(24.dp),
                tint = StitchColors.OnSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = entry.description,
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            // Optional trailing value (battery SOH / firmware version)
            if (entry.trailingValue != null) {
                Text(
                    text = entry.trailingValue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = if (entry.trailingValueGold) StitchColors.Accent else StitchColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = StitchColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(
            color = StitchColors.Border,
            thickness = 1.dp
        )
    }
}

/**
 * Logout button — red text + logout icon, centered, no card chrome.
 */
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            tint = StitchColors.Error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "退出登录",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = StitchColors.Error
        )
    }
}
