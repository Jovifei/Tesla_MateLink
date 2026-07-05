package com.teslamatelink.ui.more

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.teslamatelink.ui.theme.StitchColors

/**
 * Placeholder "More" screen — to be fleshed out with settings / about links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen() {
    Scaffold(
        containerColor = StitchColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("更多") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StitchColors.Surface,
                    titleContentColor = StitchColors.OnSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "More",
                style = MaterialTheme.typography.headlineMedium,
                color = StitchColors.OnSurface
            )
        }
    }
}
