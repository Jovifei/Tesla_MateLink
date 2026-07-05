package com.teslamatelink.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.teslamatelink.ui.navigation.Routes
import com.teslamatelink.ui.theme.StitchColors

/**
 * Bottom‑navigation tab definition.
 */
private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(Routes.DASHBOARD, "仪表盘", Icons.Outlined.Dashboard),
    TabItem(Routes.DRIVES,     "行程",   Icons.Outlined.ListAlt),
    TabItem(Routes.CHARGES,    "充电",   Icons.Outlined.Bolt),
    TabItem(Routes.MORE,       "更多",   Icons.Outlined.MoreHoriz),
)

/**
 * Stitch white‑minimal bottom navigation bar.
 *
 * - 4 tabs: Dashboard / Drives / Charges / More
 * - Active tint: [StitchColors.Accent]; inactive: [StitchColors.Outline]
 * - Transparent indicator, zero tonal elevation, 1‑dp top border
 */
@Composable
fun StitchBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        HorizontalDivider(
            thickness = 1.dp,
            color = StitchColors.Border
        )
        NavigationBar(
            modifier = modifier,
            containerColor = StitchColors.Surface,
            tonalElevation = 0.dp
        ) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { onTabSelected(tab.route) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StitchColors.Accent,
                        selectedTextColor = StitchColors.Accent,
                        unselectedIconColor = StitchColors.Outline,
                        unselectedTextColor = StitchColors.Outline,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
