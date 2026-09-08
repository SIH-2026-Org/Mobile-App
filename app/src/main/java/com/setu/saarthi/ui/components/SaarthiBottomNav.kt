package com.setu.saarthi.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.setu.saarthi.ui.navigation.BottomNavItem
import com.setu.saarthi.ui.navigation.bottomNavItems

@Composable
fun SaarthiBottomNav(
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.screen::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
