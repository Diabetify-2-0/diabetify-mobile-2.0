package com.itb.diabetify.presentation.navbar.add_activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.itb.diabetify.R
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun AddActionPopup(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: AddActivityViewModel,
    onOpenHealthProfile: () -> Unit
) {
    // States
    val smokeFieldState by viewModel.smokeFieldState
    val workoutFieldState by viewModel.workoutFieldState
    val currentSmokingStatus by viewModel.currentSmokingStatus
    val shouldShowSmokingTracker = currentSmokingStatus == 2
    val currentValues = mapOf(
        "cigarette" to smokeFieldState.text,
        "activity" to workoutFieldState.text
    )
    val currentQuestionType by viewModel.currentQuestionType
    val showBottomSheet by viewModel.showBottomSheet
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value

    // Bottom Sheet
    if (showBottomSheet) {
        val isNumericQuestion = currentQuestionType == "cigarette"

        BottomSheet(
            isVisible = true,
            onDismissRequest = { viewModel.setShowBottomSheet(false) },
            questionType = currentQuestionType,
            currentNumericValue = if (isNumericQuestion) currentValues[currentQuestionType] else null,
            currentSelectionValue = if (!isNumericQuestion) currentValues[currentQuestionType] else null,
            viewModel = viewModel
        )
    }

    // Popup
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onDismissRequest() },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = 120)) +
                                    slideInVertically(
                                        animationSpec = tween(300, delayMillis = 120, easing = FastOutSlowInEasing),
                                        initialOffsetY = { it / 4 }
                                    )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "Catat Hari Ini",
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = colorResource(id = R.color.white)
                                )
                                Text(
                                    modifier = Modifier.padding(horizontal = 30.dp),
                                    text = "Fokus untuk perilaku harian yang paling memengaruhi risiko Anda saat ini",
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Center,
                                    color = colorResource(id = R.color.white)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (shouldShowSmokingTracker) Arrangement.SpaceEvenly else Arrangement.Center
                        ) {
                            if (shouldShowSmokingTracker) {
                                AnimatedTrackingButton(
                                    icon = R.drawable.ic_smoking,
                                    label = "Rokok",
                                    delayMillis = 50,
                                    onClick = {
                                        viewModel.setCurrentQuestionType("cigarette")
                                        viewModel.setShowBottomSheet(true)
                                    }
                                )
                            }

                            AnimatedTrackingButton(
                                icon = R.drawable.ic_walk,
                                label = "Aktivitas",
                                delayMillis = if (shouldShowSmokingTracker) 100 else 60,
                                onClick = {
                                    viewModel.setCurrentQuestionType("activity")
                                    viewModel.setShowBottomSheet(true)
                                }
                            )
                        }

                        if (!shouldShowSmokingTracker) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300, delayMillis = 140)) +
                                        slideInVertically(
                                            animationSpec = tween(300, delayMillis = 140, easing = FastOutSlowInEasing),
                                            initialOffsetY = { it / 4 }
                                        )
                            ) {
                                SmokingTrackingNoticeCard(
                                    smokingStatus = currentSmokingStatus,
                                    onClick = {
                                        onDismissRequest()
                                        onOpenHealthProfile()
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300, delayMillis = 180)) +
                                    slideInVertically(
                                        animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing),
                                        initialOffsetY = { it / 4 }
                                    )
                        ) {
                            HealthProfileShortcutCard(
                                onClick = {
                                    onDismissRequest()
                                    onOpenHealthProfile()
                                }
                            )
                        }

                        // Close button
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(animationSpec = tween(300, delayMillis = 350)) +
                                    fadeIn(animationSpec = tween(300, delayMillis = 350))
                        ) {
                            FloatingActionButton(
                                onClick = { onDismissRequest() },
                                containerColor = colorResource(id = R.color.primary),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close menu",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
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

                // Success notification
                SuccessNotification(
                    showSuccess = successMessage != null,
                    successMessage = successMessage,
                    onDismiss = { viewModel.onSuccessShown() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1000f)
                )
            }
        }
    }
}

@Composable
private fun SmokingTrackingNoticeCard(
    smokingStatus: Int,
    onClick: () -> Unit
) {
    val title = when (smokingStatus) {
        1 -> "Status rokok Anda tercatat sudah berhenti"
        else -> "Status rokok Anda tercatat tidak pernah merokok"
    }
    val body = when (smokingStatus) {
        1 -> "Anda tidak perlu mencatat 0 batang setiap hari. Jika kebiasaan merokok berubah lagi, perbarui statusnya dari Profil Kesehatan."
        else -> "Pencatatan rokok harian tidak ditampilkan untuk kondisi ini. Jika status merokok berubah, perbarui dulu dari Profil Kesehatan."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_smoking),
                        contentDescription = "Info Merokok",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
                    )
                }

                Text(
                    text = title,
                    modifier = Modifier.padding(start = 12.dp),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.white)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = body,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorResource(id = R.color.white).copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun HealthProfileShortcutCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = "Profil Kesehatan",
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "Perbarui Data Kesehatan",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.white)
                    )
                    Text(
                        text = "Berat, tinggi, riwayat klinis, dan baseline lainnya diatur dari profil kesehatan.",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colorResource(id = R.color.white).copy(alpha = 0.92f)
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = "Buka Profil Kesehatan",
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(20.dp),
                colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
            )
        }
    }
}

@Composable
fun AnimatedTrackingButton(
    icon: Int,
    label: String,
    delayMillis: Int = 0,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(300, delayMillis = delayMillis)) +
                fadeIn(animationSpec = tween(300, delayMillis = delayMillis))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = colorResource(id = R.color.tertiary),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorResource(id = R.color.white)
            )
        }
    }
}
