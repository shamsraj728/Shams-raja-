package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val route: String, val title: String) {
    DASHBOARD("dashboard", "Dashboard"),
    CALENDAR("calendar", "Calendar"),
    CREATE("create", "Create Post"),
    POSTS("posts", "Posts"),
    SETTINGS("settings", "Settings"),
    NOTIFICATIONS("notifications", "Notifications"),
    ACCOUNTS("accounts", "Connected Accounts"),
    ACTIVITY("activity", "Activity Log"),
    HELP("help", "Help & FAQs"),
    LOGIN("login", "Sign In"),
    SIGNUP("signup", "Create Account"),
    FORGOT_PASSWORD("forgot", "Reset Password"),
    ONBOARDING("onboarding", "Welcome")
}

sealed class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(Screen.DASHBOARD, "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Calendar : BottomNavItem(Screen.CALENDAR, "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Create : BottomNavItem(Screen.CREATE, "Create", Icons.Filled.AddCircle, Icons.Filled.AddCircle)
    object Posts : BottomNavItem(Screen.POSTS, "Posts", Icons.Filled.Folder, Icons.Outlined.Folder)
    object Settings : BottomNavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val items = listOf(Dashboard, Calendar, Create, Posts, Settings)
    }
}
