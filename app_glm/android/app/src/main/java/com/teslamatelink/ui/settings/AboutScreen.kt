package com.teslamatelink.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teslamatelink.ui.components.StitchCard
import com.teslamatelink.ui.theme.JetBrainsMonoFamily
import com.teslamatelink.ui.theme.StitchColors

// ── Static data (matches Stitch design spec) ──────────────────────────────────

private data class VehicleInfo(val label: String, val value: String)
private val vehicleInfoList = listOf(
    VehicleInfo("Vehicle Model", "Model 3 2022"),
    VehicleInfo("VIN", "5YJ3E1EA7NF"),
    VehicleInfo("Odometer", "12,450 km"),
    VehicleInfo("TeslaMate API", "v1.32.0")
)

private data class TechStackItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val desc: String)
private val techStackList = listOf(
    TechStackItem(Icons.Outlined.PhoneIphone, "iOS", "SwiftUI / Swift Charts"),
    TechStackItem(Icons.Outlined.Android, "Android", "Kotlin Compose / Hilt"),
    TechStackItem(Icons.Outlined.Language, "Web", "React + Vite / Recharts")
)

private data class FeatureRow(val feature: String, val android: Boolean, val ios: Boolean, val web: Boolean)
private val featureMatrix = listOf(
    FeatureRow("Real-time Stats", true, true, true),
    FeatureRow("Charging History", true, true, true),
    FeatureRow("Widgets", true, true, false),
    FeatureRow("Live Maps", true, false, true)
)

private data class LicenseItem(val name: String, val version: String)
private val licenses = listOf(
    LicenseItem("Retrofit", "v2.9.0 / Apache 2.0"),
    LicenseItem("OkHttp", "v4.9.3 / Apache 2.0"),
    LicenseItem("Moshi", "v1.13.0 / Apache 2.0"),
    LicenseItem("Hilt", "v2.44 / Apache 2.0"),
    LicenseItem("Recharts", "v2.1.9 / MIT"),
    LicenseItem("Leaflet", "v1.8.0 / BSD"),
    LicenseItem("WidgetKit", "iOS Native"),
    LicenseItem("AMap SDK", "JS API v2.0")
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "关于",
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
            // -- Brand Identity --
            BrandIdentity()

            // -- Vehicle Info --
            VehicleInfoCard()

            // -- Tech Stack --
            TechStackCard()

            // -- Data Source --
            DataSourceCard()

            // -- Function Matrix --
            FunctionMatrixCard()

            // -- Open Source Licenses --
            LicensesCard()

            // -- Contact --
            ContactCard()

            // -- Footer --
            Footer()
        }
    }
}

// ── Brand Identity ────────────────────────────────────────────────────────────

@Composable
private fun BrandIdentity() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular icon with heavy border
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(1.dp, StitchColors.OnSurface, CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ElectricCar,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = StitchColors.OnSurface
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "MateLink",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.64).sp,
                color = StitchColors.OnSurface
            )
            Text(
                text = "Your Tesla Data Companion",
                fontSize = 16.sp,
                color = StitchColors.OnSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                VersionChip("v0.1.0-alpha")
                VersionChip("2025.07.02")
            }
        }
    }
}

@Composable
private fun VersionChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = StitchColors.SurfaceContainerHigh
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ── Vehicle Info Card ─────────────────────────────────────────────────────────

@Composable
private fun VehicleInfoCard() {
    StitchCard {
        Text(
            text = "已连接车辆",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        vehicleInfoList.forEachIndexed { index, info ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = info.label,
                    fontSize = 16.sp,
                    color = StitchColors.OnSurface
                )
                Text(
                    text = info.value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = JetBrainsMonoFamily,
                    color = StitchColors.OnSurface
                )
            }
            if (index < vehicleInfoList.lastIndex) {
                HorizontalDivider(color = StitchColors.Border, thickness = 1.dp)
            }
        }
    }
}

// ── Tech Stack Card ───────────────────────────────────────────────────────────

@Composable
private fun TechStackCard() {
    StitchCard {
        Text(
            text = "技术栈",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            techStackList.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(StitchColors.SurfaceContainerLow, RoundedCornerShape(4.dp))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = StitchColors.Secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchColors.OnSurface
                    )
                    Text(
                        text = item.desc,
                        fontSize = 14.sp,
                        color = StitchColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Data Source Card ──────────────────────────────────────────────────────────

@Composable
private fun DataSourceCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shield icon with green tint
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFD1FAE5)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = Color(0xFF059669)
                )
            }
            Column {
                Text(
                    text = "数据来源",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TeslaMate Self-hosted + TeslaMateApi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchColors.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "所有数据来自您的 TeslaMate 服务器。MateLink 不会收集、存储或上传您的车辆位置、驾驶习惯或任何敏感凭证。您的隐私受自建服务器保护。",
                    fontSize = 14.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
        }
    }
}

// ── Function Matrix Card ──────────────────────────────────────────────────────

@Composable
private fun FunctionMatrixCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = StitchColors.White,
        border = BorderStroke(1.dp, StitchColors.Border),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchColors.SurfaceContainerLow)
                    .padding(24.dp)
            ) {
                Text(
                    text = "功能支持矩阵",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
            }
            HorizontalDivider(color = StitchColors.Border, thickness = 1.dp)
            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchColors.SurfaceContainerLow)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Feature",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text("Android", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = StitchColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("iOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = StitchColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("Web", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = StitchColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
            // Rows
            featureMatrix.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.feature,
                        fontSize = 16.sp,
                        color = StitchColors.OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCheck(row.android, Modifier.weight(1f))
                    FeatureCheck(row.ios, Modifier.weight(1f))
                    FeatureCheck(row.web, Modifier.weight(1f))
                }
                if (index < featureMatrix.lastIndex) {
                    HorizontalDivider(color = StitchColors.Border, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun FeatureCheck(supported: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (supported) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (supported) Color(0xFF059669) else StitchColors.OnSurfaceVariant
        )
    }
}

// ── Licenses Card ─────────────────────────────────────────────────────────────

@Composable
private fun LicensesCard() {
    StitchCard {
        Text(
            text = "开源依赖",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = StitchColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        // 2-column grid
        val rows = licenses.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchColors.OnSurface
                        )
                        Text(
                            text = item.version,
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFamily,
                            color = StitchColors.OnSurfaceVariant
                        )
                    }
                }
                // Fill empty slot if odd count
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Contact Card ──────────────────────────────────────────────────────────────

@Composable
private fun ContactCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StitchColors.White,
            border = BorderStroke(1.dp, StitchColors.Border),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "反馈与联系",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnSurfaceVariant
                )
                ContactRow(Icons.Outlined.Code, "GitHub Repository")
                ContactRow(Icons.Outlined.Email, "contact@jovif.dev")
            }
        }
        // Submit feedback button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StitchColors.Primary,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Forum,
                    contentDescription = null,
                    tint = StitchColors.OnPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "提交反馈",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = StitchColors.OnPrimary
                )
            }
        }
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = StitchColors.OnSurface
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = StitchColors.OnSurface
        )
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "© 2025 JoviF · MIT License",
            fontSize = 14.sp,
            fontFamily = JetBrainsMonoFamily,
            color = StitchColors.OnSurfaceVariant
        )
        Text(
            text = "Made with ❤️ for Tesla owners",
            fontSize = 14.sp,
            color = StitchColors.OnSurfaceVariant
        )
    }
}
