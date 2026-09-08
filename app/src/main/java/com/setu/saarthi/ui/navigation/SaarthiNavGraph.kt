package com.setu.saarthi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.setu.saarthi.ui.components.SaarthiBottomNav
import com.setu.saarthi.ui.screens.clarify.ClarifyScreen
import com.setu.saarthi.ui.screens.detail.SchemeDetailScreen
import com.setu.saarthi.ui.screens.detail.SchemeDetailViewModel
import com.setu.saarthi.ui.screens.home.HomeScreen
import com.setu.saarthi.ui.screens.profile.ProfileScreen
import com.setu.saarthi.ui.screens.results.ResultsScreen
import com.setu.saarthi.ui.screens.saved.SavedScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val TOP_LEVEL_SCREENS = setOf(Screen.Home::class, Screen.Saved::class, Screen.Profile::class)

@Composable
fun SaarthiNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomNav = TOP_LEVEL_SCREENS.any { currentDestination?.hasRoute(it) == true }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                SaarthiBottomNav(currentDestination = currentDestination) { item ->
                    navController.navigate(item.screen) {
                        popUpTo(Screen.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToResults = { navController.navigate(Screen.Results) },
                    onNavigateToClarify = { navController.navigate(Screen.Clarify) }
                )
            }
            composable<Screen.Saved> {
                SavedScreen(onSchemeClick = { schemeId -> navController.navigate(Screen.Detail(schemeId)) })
            }
            composable<Screen.Profile> {
                ProfileScreen()
            }
            composable<Screen.Results> {
                ResultsScreen(
                    onBack = { navController.popBackStack() },
                    onSchemeClick = { schemeId -> navController.navigate(Screen.Detail(schemeId)) }
                )
            }
            composable<Screen.Clarify> {
                ClarifyScreen(
                    onBack = { navController.popBackStack() },
                    onDone = {
                        navController.navigate(Screen.Results) {
                            popUpTo(Screen.Home)
                        }
                    }
                )
            }
            composable<Screen.Detail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.Detail>()
                val viewModel: SchemeDetailViewModel = koinViewModel(parameters = { parametersOf(route.schemeId) })
                SchemeDetailScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
            }
        }
    }
}
