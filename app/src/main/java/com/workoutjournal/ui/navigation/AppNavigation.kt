package com.workoutjournal.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.workoutjournal.ui.screens.history.HistoryScreen
import com.workoutjournal.ui.screens.progress.ProgressScreen
import com.workoutjournal.ui.screens.session.SessionScreen
import com.workoutjournal.ui.theme.GradientEnd
import com.workoutjournal.ui.theme.GradientEndDark
import com.workoutjournal.ui.theme.GradientStart
import com.workoutjournal.ui.theme.GradientStartDark

private sealed class BottomDest(val route: String, val label: String) {
    object History : BottomDest("history", "History")
    object Progress : BottomDest("progress", "Progress")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomDestinations = listOf(BottomDest.History, BottomDest.Progress)
    val showBottomBar = currentDestination?.route?.startsWith("session/") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val isDark = isSystemInDarkTheme()
                val gradStart = if (isDark) GradientStartDark else GradientStart
                val gradEnd   = if (isDark) GradientEndDark   else GradientEnd

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bottomDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(50))
                                    .then(
                                        if (selected) Modifier.background(
                                            Brush.horizontalGradient(colors = listOf(gradStart, gradEnd))
                                        ) else Modifier
                                    )
                                    .clickable {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomDest.History.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomDest.History.route) {
                HistoryScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate("session/$sessionId")
                    }
                )
            }
            composable("session/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull()
                    ?: return@composable
                SessionScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomDest.Progress.route) {
                ProgressScreen()
            }
        }
    }
}
