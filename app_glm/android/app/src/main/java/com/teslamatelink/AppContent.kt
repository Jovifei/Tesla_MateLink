package com.teslamatelink

import androidx.compose.runtime.Composable
import com.teslamatelink.ui.navigation.NavGraph

@Composable
fun AppContent() {
    NavGraph(startDestination = "onboarding")
}
