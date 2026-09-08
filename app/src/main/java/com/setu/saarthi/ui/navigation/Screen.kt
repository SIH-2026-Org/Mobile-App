package com.setu.saarthi.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Saved : Screen
    @Serializable data object Profile : Screen
    @Serializable data object Results : Screen
    @Serializable data object Clarify : Screen
    @Serializable data class Detail(val schemeId: String) : Screen
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Saved, "Saved", Icons.Filled.Star),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person)
)
