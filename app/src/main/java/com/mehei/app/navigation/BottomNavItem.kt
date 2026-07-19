package com.mehei.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines the 4 primary bottom navigation destinations.
 */
sealed class BottomNavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
) {
    data object Home : BottomNavItem(
        route = RequestRoute,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Home — browse artists",
    )

    data object Explore : BottomNavItem(
        route = RequestRoute,  // Same route for now — can be separated later
        label = "Explore",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        contentDescription = "Explore — discover styles",
    )

    data object Bookings : BottomNavItem(
        route = BookingsHistoryRoute,
        label = "Bookings",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        contentDescription = "Bookings — view your bookings",
    )

    data object Profile : BottomNavItem(
        route = ProfileRoute,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "Profile — your account",
    )

    companion object {
        val items = listOf(Home, Bookings, Profile)
    }
}
