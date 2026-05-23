package com.itb.diabetify.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.presentation.settings.components.ConfirmationDialog
import com.itb.diabetify.presentation.settings.components.HealthProfileCard
import com.itb.diabetify.presentation.settings.components.NotificationCard
import com.itb.diabetify.presentation.settings.components.NotificationItem
import com.itb.diabetify.presentation.settings.components.ProfileCard
import com.itb.diabetify.presentation.settings.components.SettingsCard
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    // States
    val showLogoutDialog by viewModel.showLogoutDialog
    val errorMessage = viewModel.errorMessage.value
    val isLoading = viewModel.logoutState.value.isLoading
    val healthProfile = viewModel.healthProfile.value
    val context = LocalContext.current

    // Navigation Event
    val navigationEvent = viewModel.navigationEvent.value
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let {
            when (it) {
                "LOGIN_SCREEN" -> {
                    onLogout()
                    viewModel.onNavigationHandled()
                }
            }
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            onConfirm = {
                viewModel.logout()
                viewModel.setShowLogoutDialog(false)
            },
            onDismiss = { viewModel.setShowLogoutDialog(false) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(id = R.color.primary),
                                colorResource(id = R.color.primary).copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Pengaturan",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Profil Akun",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.primary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProfileCard(
                    name = viewModel.nameFieldState.value.text,
                    email = viewModel.emailFieldState.value.text,
                    onEditClick = {
                        navController.navigate(Route.EditProfileScreen.route) {
                            launchSingleTop = true
                        }
                    },
                    actionLabel = "Edit Profil Akun"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Profil Kesehatan",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.primary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                HealthProfileCard(
                    summary = if (healthProfile.hasProfile) {
                        "BMI ${String.format("%.1f", healthProfile.bmi)} • ${healthProfile.weight} kg • ${healthProfile.height} cm"
                    } else {
                        "Belum ada baseline kesehatan yang tersimpan"
                    },
                    smokingSummary = if (healthProfile.hasProfile) {
                        "Status merokok: ${settingsSmokingStatusLabel(healthProfile.smokingStatus)}"
                    } else {
                        "Lengkapi survey kesehatan agar data profil medis tersedia"
                    },
                    onViewClick = {
                        navController.navigate(Route.HealthProfileScreen.route) {
                            launchSingleTop = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Notification settings
                NotificationCard(
                    title = "Pengaturan Notifikasi",
                    notificationItems = listOf(
                        NotificationItem(
                            icon = R.drawable.ic_notification,
                            title = "Pengingat Harian",
                            isEnabled = viewModel.dailyReminderEnabled.value,
                            onToggle = { enabled -> viewModel.setDailyReminderEnabled(enabled) }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cards section
                getSettingsCards(context).forEach { cardData ->
                    SettingsCard(cardData = cardData)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Logout button
                PrimaryButton(
                    text = "Logout",
                    onClick = { viewModel.setShowLogoutDialog(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Error notification
        ErrorNotification(
            showError = errorMessage != null,
            errorMessage = errorMessage,
            onDismiss = { viewModel.onErrorShown() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )
    }
}

private fun settingsSmokingStatusLabel(value: Int): String = when (value) {
    0 -> "Tidak pernah"
    1 -> "Sudah berhenti"
    2 -> "Masih merokok"
    else -> "Tidak diketahui"
}
