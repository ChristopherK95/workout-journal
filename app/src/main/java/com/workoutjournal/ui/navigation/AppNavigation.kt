package com.workoutjournal.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.workoutjournal.ui.components.ToolsMenu
import com.workoutjournal.ui.screens.history.HistoryScreen
import com.workoutjournal.ui.screens.progress.ProgressScreen
import com.workoutjournal.ui.screens.session.SessionScreen

private sealed class NavDest(val route: String, val label: String) {
    object History : NavDest("history", "History")
    object Progress : NavDest("progress", "Progress")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val destinations = listOf(NavDest.History, NavDest.Progress)
    val isSessionRoute = currentDestination?.route?.startsWith("session/") == true
    val currentDest = destinations.firstOrNull {
        currentDestination?.hierarchy?.any { h -> h.route == it.route } == true
    } ?: NavDest.History

    var navDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0D0D1F),
        topBar = {
            if (!isSessionRoute) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .background(Color(0xFF0D0D1F))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable { navDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = currentDest.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch view",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = navDropdownExpanded,
                            onDismissRequest = { navDropdownExpanded = false }
                        ) {
                            destinations.forEach { dest ->
                                DropdownMenuItem(
                                    text = { Text(dest.label) },
                                    leadingIcon = null,
                                    onClick = {
                                        navDropdownExpanded = false
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    ToolsMenu()
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavDest.History.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavDest.History.route) {
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
            composable(NavDest.Progress.route) {
                ProgressScreen()
            }
        }
    }
}
