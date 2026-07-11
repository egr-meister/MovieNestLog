package com.movienest.log.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.screens.EntryDetailScreen
import com.movienest.log.ui.screens.EntryEditorScreen
import com.movienest.log.ui.screens.FavoritesScreen
import com.movienest.log.ui.screens.GenreScreen
import com.movienest.log.ui.screens.HomeScreen
import com.movienest.log.ui.screens.LibraryScreen
import com.movienest.log.ui.screens.OnboardingScreen
import com.movienest.log.ui.screens.SettingsScreen
import com.movienest.log.ui.screens.StatisticsScreen
import com.movienest.log.ui.screens.WatchingScreen

private val bottomRoutes = BottomDestination.entries.map { it.route }

@Composable
fun MovieNestApp(startAtOnboarding: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(navController, currentRoute)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startAtOnboarding) Routes.ONBOARDING else Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onOpenEntry = { navController.navigate(Routes.entryDetail(it)) },
                    onAddEntry = { status ->
                        navController.navigate(Routes.addEntry(status?.name))
                    },
                    onOpenLibrary = { navController.navigateToTab(Routes.LIBRARY) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onOpenEntry = { navController.navigate(Routes.entryDetail(it)) },
                    onAddEntry = { navController.navigate(Routes.ENTRY_ADD) }
                )
            }

            composable(Routes.WATCHING) {
                WatchingScreen(
                    onOpenEntry = { navController.navigate(Routes.entryDetail(it)) },
                    onEditEntry = { navController.navigate(Routes.entryEdit(it)) }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(onOpenEntry = { navController.navigate(Routes.entryDetail(it)) })
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen(onOpenEntry = { navController.navigate(Routes.entryDetail(it)) })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onManageGenres = { navController.navigate(Routes.GENRES) },
                    onReplayOnboarding = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.GENRES) {
                GenreScreen(onBack = { navController.popBackStack() })
            }

            // Add entry (with optional start status argument)
            composable(
                route = Routes.ENTRY_ADD_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_START_STATUS) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                EntryEditorScreen(
                    onSaved = { id ->
                        navController.navigate(Routes.entryDetail(id)) {
                            popUpTo(Routes.ENTRY_ADD_PATTERN) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onMissing = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.ENTRY_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_ENTRY_ID) { type = NavType.StringType })
            ) {
                EntryDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.entryEdit(it)) },
                    onOpenLibrary = { navController.navigateToTab(Routes.LIBRARY) }
                )
            }

            composable(
                route = Routes.ENTRY_EDIT,
                arguments = listOf(navArgument(Routes.ARG_ENTRY_ID) { type = NavType.StringType })
            ) {
                EntryEditorScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    onMissing = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        BottomDestination.entries.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(dest.route) },
                icon = { Icon(dest.icon(), contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}

private fun BottomDestination.icon() = when (this) {
    BottomDestination.Home -> Icons.Filled.Home
    BottomDestination.Library -> Icons.AutoMirrored.Filled.List
    BottomDestination.Watching -> Icons.Filled.PlayArrow
    BottomDestination.Favorites -> Icons.Filled.Favorite
    BottomDestination.Stats -> Icons.Filled.QueryStats
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Settings entry point reachable from bottom-bar-less routes via Home search? Provide a menu hook.
@Suppress("unused")
private fun NavHostController.openSettings() {
    navigate(Routes.SETTINGS) { launchSingleTop = true }
}
