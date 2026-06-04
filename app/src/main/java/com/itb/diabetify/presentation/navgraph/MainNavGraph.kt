package com.itb.diabetify.presentation.navgraph

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.history.HistoryScreen
import com.itb.diabetify.presentation.history.HistoryViewModel
import com.itb.diabetify.presentation.home.HomeScreen
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.navbar.BottomNavigationBar
import com.itb.diabetify.presentation.navbar.NavigationViewModel
import com.itb.diabetify.presentation.guide.GuideScreen
import com.itb.diabetify.presentation.guide.guide_detail.GuideDetailScreen
import com.itb.diabetify.presentation.guide.tips_detail.TipsDetailScreen
import com.itb.diabetify.presentation.home.risk_detail.RiskDetailScreen
import com.itb.diabetify.presentation.home.risk_factor_detail.RiskFactorDetailScreen
import com.itb.diabetify.presentation.settings.SettingsScreen
import com.itb.diabetify.presentation.settings.SettingsViewModel
import com.itb.diabetify.presentation.settings.edit_profile.EditProfileScreen
import com.itb.diabetify.presentation.settings.health_profile.HealthProfileScreen
import com.itb.diabetify.presentation.navbar.add_activity.AddActivityViewModel
import com.itb.diabetify.presentation.no_internet.NoInternetScreen
import com.itb.diabetify.presentation.register.RegisterViewModel
import com.itb.diabetify.presentation.survey.SurveyScreen
import com.itb.diabetify.presentation.survey.SurveyViewModel
import com.itb.diabetify.presentation.home.counterfactual.CounterfactualResultScreen
import com.itb.diabetify.presentation.home.counterfactual.CounterfactualScreen
import com.itb.diabetify.presentation.home.planner.PlannerActionScreen
import com.itb.diabetify.presentation.home.planner.PlannerChatbotScreen
import com.itb.diabetify.presentation.home.planner.PlannerCheckInScreen
import com.itb.diabetify.presentation.home.planner.PlannerCoachScreen
import com.itb.diabetify.presentation.home.planner.PlannerGoalDetailScreen
import com.itb.diabetify.presentation.home.planner.PlannerMilestoneScreen

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun MainNavGraph(
    navController: NavController,
    onRetryConnection: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val addActivityViewModel: AddActivityViewModel = hiltViewModel()
    val mainNavController = rememberNavController()

    var shouldNavigateToLogin by rememberSaveable { mutableStateOf(false) }
    val navigationHistory = remember { mutableListOf<String>() }
    val currentRoute = mainNavController.currentBackStackEntryAsState().value?.destination?.route
    var shouldOpenAddPopupOnHome by rememberSaveable { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()
    val primaryColor = colorResource(id = R.color.primary)

    SideEffect {
        systemUiController.setStatusBarColor(
            color = primaryColor,
            darkIcons = false
        )
    }

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            if (navigationViewModel.navigationItems.any { it.route == route }) {
                if (navigationHistory.isEmpty() || navigationHistory.last() != route) {
                    navigationHistory.add(route)
                }
                navigationViewModel.onNavigationItemSelected(route)
            }
        }
    }

    val homeRoute = Route.HomeScreen.route
    fun returnToHomeAndOpenAddPopup() {
        shouldOpenAddPopupOnHome = true
        if (!mainNavController.popBackStack(homeRoute, inclusive = false)) {
            mainNavController.navigate(homeRoute) {
                popUpTo(mainNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(currentRoute, shouldOpenAddPopupOnHome) {
        if (currentRoute == homeRoute && shouldOpenAddPopupOnHome) {
            shouldOpenAddPopupOnHome = false
            navigationViewModel.setShowPopUp(true)
        }
    }

    val settingsChildRoutes = setOf(
        Route.EditProfileScreen.route,
        Route.HealthProfileScreen.route
    )
    val guideChildRoutes = setOf(
        Route.GuideDetailScreen.route,
        Route.TipsDetailScreen.route
    )

    BackHandler {
        when (currentRoute) {
            Route.SurveyScreen.route -> {
                // Do nothing to prevent back navigation
            }
            in settingsChildRoutes -> {
                if (!mainNavController.popBackStack(Route.SettingsScreen.route, inclusive = false)) {
                    mainNavController.navigate(Route.SettingsScreen.route) {
                        launchSingleTop = true
                    }
                }
            }
            Route.HealthProfileFromHomePopupScreen.route -> {
                returnToHomeAndOpenAddPopup()
            }
            Route.CounterfactualResultScreen.route -> {
                if (!mainNavController.popBackStack(homeRoute, inclusive = false)) {
                    mainNavController.navigate(homeRoute) {
                        popUpTo(mainNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            in guideChildRoutes -> {
                if (!mainNavController.popBackStack(Route.GuideScreen.route, inclusive = false)) {
                    mainNavController.navigate(Route.GuideScreen.route) {
                        launchSingleTop = true
                    }
                }
            }
            homeRoute -> {
                // Do nothing when already at home
            }
            else -> {
                navigationViewModel.onNavigationItemSelected(homeRoute)
                mainNavController.navigate(homeRoute) {
                    popUpTo(mainNavController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        }
    }

    if (shouldNavigateToLogin) {
        shouldNavigateToLogin = false
        navController.navigate(Route.LoginScreen.route) {
            popUpTo(Route.MainNavigation.route) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val bottomBarHiddenRoutes = setOf(
        Route.SurveyScreen.route,
        Route.SurveySuccessScreen.route,
        Route.RiskDetailScreen.route,
        Route.RiskFactorDetailScreen.route,
        Route.CounterfactualScreen.route,
        Route.CounterfactualResultScreen.route,
        Route.PlannerGoalDetailScreen.route,
        Route.PlannerMilestoneScreen.route,
        Route.PlannerActionScreen.route,
        Route.PlannerCoachScreen.route,
        Route.PlannerCheckInScreen.route,
        Route.PlannerChatbotScreen.route,
        Route.EditProfileScreen.route,
        Route.HealthProfileScreen.route,
        Route.HealthProfileFromHomePopupScreen.route,
        Route.GuideDetailScreen.route,
        Route.TipsDetailScreen.route
    )
    val shouldShowBottomBar = currentRoute !in bottomBarHiddenRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = navigationViewModel,
                    addActivityViewModel = addActivityViewModel,
                    onOpenHealthProfile = {
                        mainNavController.navigate(Route.HealthProfileFromHomePopupScreen.route) {
                            launchSingleTop = true
                        }
                    },
                    onCreatePlan = {
                        mainNavController.navigate(Route.CounterfactualScreen.route) {
                            launchSingleTop = true
                        }
                    },
                    onItemSelected = { route ->
                        mainNavController.navigate(route) {
                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = Route.HomeScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Route.HomeScreen.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                )
            }

            composable(
                route = Route.SurveyScreen.route
            ) {
                val surveyViewModel: SurveyViewModel = hiltViewModel()
                SurveyScreen(
                    navController = mainNavController,
                    viewModel = surveyViewModel,
                )
            }

            composable(
                route = Route.SurveySuccessScreen.route
            ) {
                com.itb.diabetify.presentation.survey.SuccessScreen(
                    navController = mainNavController
                )
            }

            composable(route = Route.RiskDetailScreen.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                RiskDetailScreen(
                    navController = mainNavController,
                     viewModel = homeViewModel,
                )
            }

            composable(route = Route.RiskFactorDetailScreen.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                RiskFactorDetailScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                )
            }

            composable(route = Route.CounterfactualScreen.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                CounterfactualScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel
                )
            }

            composable(route = Route.CounterfactualResultScreen.route) {
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                CounterfactualResultScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel
                )
            }

            composable(
                route = Route.PlannerGoalDetailScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerGoalDetailScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(
                route = Route.PlannerMilestoneScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerMilestoneScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(
                route = Route.PlannerActionScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerActionScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(
                route = Route.PlannerCoachScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerCoachScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(
                route = Route.PlannerCheckInScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerCheckInScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(
                route = Route.PlannerChatbotScreen.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel(
                    mainNavController.getBackStackEntry(Route.HomeScreen.route)
                )
                PlannerChatbotScreen(
                    navController = mainNavController,
                    viewModel = homeViewModel,
                    goalId = backStackEntry.arguments?.getString("goalId")
                )
            }

            composable(route = Route.HistoryScreen.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(
                    viewModel = historyViewModel
                )
            }

            composable(route = Route.GuideScreen.route) {
                GuideScreen(
                    navController = mainNavController,
                )
            }

            composable(
                route = Route.GuideDetailScreen.route,
                arguments = listOf(
                    navArgument("guideId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val guideId = backStackEntry.arguments?.getString("guideId") ?: return@composable
                GuideDetailScreen(
                    navController = mainNavController,
                    guideId = guideId
                )
            }

            composable(
                route = Route.TipsDetailScreen.route,
                arguments = listOf(
                    navArgument("tipsId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val tipsId = backStackEntry.arguments?.getString("tipsId") ?: return@composable
                TipsDetailScreen(
                    navController = mainNavController,
                    tipsId = tipsId
                )
            }

            composable(route = Route.SettingsScreen.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    navController = mainNavController,
                    viewModel = settingsViewModel,
                    onLogout = {
                        shouldNavigateToLogin = true
                    }
                )
            }

            composable(route = Route.EditProfileScreen.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                EditProfileScreen(
                    navController = mainNavController,
                    viewModel = settingsViewModel,
                )
            }

            composable(route = Route.HealthProfileScreen.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                HealthProfileScreen(
                    navController = mainNavController,
                    viewModel = settingsViewModel,
                )
            }

            composable(route = Route.HealthProfileFromHomePopupScreen.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                HealthProfileScreen(
                    navController = mainNavController,
                    viewModel = settingsViewModel,
                    onBack = {
                        returnToHomeAndOpenAddPopup()
                    }
                )
            }

            composable(route = Route.NoInternetScreen.route) {
                NoInternetScreen(
                    navController = mainNavController,
                    onRetryClicked = onRetryConnection
                )
            }
        }
    }
}
