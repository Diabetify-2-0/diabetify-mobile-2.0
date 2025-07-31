package com.itb.diabetify.presentation.navbar

import com.itb.diabetify.R
import com.itb.diabetify.presentation.navgraph.Route

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: Int,
    val contentDescription: String
)
val items = listOf(
    NavigationItem(
        route = Route.HomeScreen.route,
        title = "Home",
        icon = R.drawable.ic_home,
        contentDescription = "Navigate to home screen"
    ),
    NavigationItem(
        route = Route.HistoryScreen.route,
        title = "History",
        icon = R.drawable.ic_history,
        contentDescription = "Navigate to history screen"
    ),
    NavigationItem(
        route = Route.GuideScreen.route,
        title = "Recommendation",
        icon = R.drawable.ic_thumbs,
        contentDescription = "Navigate to guide screen"
    ),
    NavigationItem(
        route = Route.SettingsScreen.route,
        title = "Settings",
        icon = R.drawable.ic_user_settings,
        contentDescription = "Navigate to settings screen"
    )
)