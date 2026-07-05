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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
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
import com.teslamatelink.ui.theme.StitchColors

/**
 * Data class representing an entry on the More screen.
 */
data class MoreEntry(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector
)

/**
 * Predefined analysis entries.
 */
private val analysisEntries = listOf(
    MoreEntry("统计", "月度与年度汇总", Routes.STATISTICS, Icons.Outlined.BarChart),
    MoreEntry("热力图", "驾驶频率与模式", Routes.HEATMAP, Icons.Outlined.Map),
    MoreEntry("效率分析", "能耗效率分析", Routes.EFFICIENCY, Icons.Outlined.Speed),
    MoreEntry("续航分析", "预估续航与实际对比", Routes.RANGE, Icons.Outlined.Route),
    MoreEntry("充电统计", "充电记录与成本", Routes.COST, Icons.Outlined.BatteryChargingFull),
    MoreEntry("幽灵耗电", "待机能耗分析", Routes.VAMPIRE, Icons.Outlined.NightsStay),
)

/**
 * Predefined report / about entries.
 */
private val reportEntries = listOf(
    MoreEntry("年度报告", "年度驾驶总结报告", "annual_report", Icons.Outlined.Description),
    MoreEntry("设置", "应用设置与偏好", Routes.SETTINGS, Icons.Outlined.Settings),
    MoreEntry("关于", "版本信息与开源许可", Routes.ABOUT, Icons.Outlined.Info),
)

/**
 * Full Stitch white-minimal "More" screen.
 * Shows analysis shortcuts and report / about entries.
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
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Surface,
                    titleContentColor = StitchColors.OnSurface,
                    navigationIconContentColor = StitchColors.OnSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // -- Analysis section --
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "分析",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchColors.OnSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(analysisEntries) { entry ->
                MoreEntryRow(entry = entry, onClick = { onNavigate(entry.route) })
            }

            // -- Report section --
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "报告",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchColors.OnSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(reportEntries) { entry ->
                MoreEntryRow(entry = entry, onClick = { onNavigate(entry.route) })
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Single row for a [MoreEntry] — icon, title, description, with bottom divider.
 */
@Composable
private fun MoreEntryRow(
    entry: MoreEntry,
    onClick: () -> Unit
) {
    Surface(
        color = StitchColors.Surface,
        modifier = Modifier.fillMaxWidth()
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
                    tint = StitchColors.OnSurfaceVariant
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
            }
            HorizontalDivider(
                color = StitchColors.OutlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}
