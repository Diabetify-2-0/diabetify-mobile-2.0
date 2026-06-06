package com.itb.diabetify.presentation.navbar.add_activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.presentation.common.SuccessNotification
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun AddActionPopup(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    viewModel: AddActivityViewModel,
    onOpenHealthProfile: () -> Unit,
    onCreatePlan: () -> Unit
) {
    // States
    val activePlannerGoal by viewModel.activePlannerGoal
    val hasActivePlannerGoal = activePlannerGoal?.status == PlannerGoalStatus.ACTIVE
    val plannerCheckInActions = viewModel.plannerCheckInActions()
    val currentQuestionType by viewModel.currentQuestionType
    val showBottomSheet by viewModel.showBottomSheet
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value

    // Bottom Sheet
    if (showBottomSheet) {
        val isNumericQuestion = currentQuestionType in listOf("weight", "height")

        BottomSheet(
            isVisible = true,
            onDismissRequest = { viewModel.setShowBottomSheet(false) },
            questionType = currentQuestionType,
            currentNumericValue = if (isNumericQuestion) viewModel.currentValueFor(currentQuestionType) else null,
            currentSelectionValue = if (!isNumericQuestion) viewModel.currentValueFor(currentQuestionType) else null,
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
                    .background(Color.Black.copy(alpha = 0.93f))
                    .clickable { onDismissRequest() },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = if (hasActivePlannerGoal) {
                                Arrangement.Center
                            } else {
                                Arrangement.Center
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = if (hasActivePlannerGoal) 0.dp else 40.dp,
                                    bottom = 140.dp
                                )
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
                                        text = "Pembaruan Kesehatan",
                                        fontFamily = poppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = colorResource(id = R.color.white)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = if (hasActivePlannerGoal) {
                                            "Perbarui data yang relevan untuk memantau kemajuan goal Anda"
                                        } else {
                                            "Perbarui data harian atau buat rencana penurunan risiko yang baru"
                                        },
                                        fontFamily = poppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = colorResource(id = R.color.white).copy(alpha = 0.84f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(0.82f)
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(
                                    if (hasActivePlannerGoal) 20.dp else 28.dp
                                )
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                if (hasActivePlannerGoal && plannerCheckInActions.isNotEmpty()) {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(300, delayMillis = 90)) +
                                                slideInVertically(
                                                    animationSpec = tween(300, delayMillis = 90, easing = FastOutSlowInEasing),
                                                    initialOffsetY = { it / 4 }
                                                )
                                    ) {
                                        PlannerCheckInCard(
                                            actions = plannerCheckInActions
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            space = 20.dp,
                                            alignment = Alignment.CenterHorizontally
                                        ),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        AnimatedTrackingButton(
                                            icon = R.drawable.ic_walk,
                                            label = "Aktivitas",
                                            iconSize = 34.dp,
                                            delayMillis = 60,
                                            modifier = Modifier.width(112.dp),
                                            onClick = {
                                                viewModel.setCurrentQuestionType("activity")
                                                viewModel.setShowBottomSheet(true)
                                            }
                                        )

                                        AnimatedTrackingButton(
                                            icon = R.drawable.ic_goal,
                                            label = "Rencana Baru",
                                            delayMillis = 110,
                                            modifier = Modifier.width(112.dp),
                                            onClick = {
                                                onDismissRequest()
                                                onCreatePlan()
                                            }
                                        )
                                    }

                                    AnimatedTrackingButton(
                                        icon = R.drawable.ic_heart_filled,
                                        label = "Data Kesehatan",
                                        delayMillis = 160,
                                        onClick = {
                                            onDismissRequest()
                                            onOpenHealthProfile()
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(animationSpec = tween(300, delayMillis = 350)) +
                                    fadeIn(animationSpec = tween(300, delayMillis = 350)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 72.dp)
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
private fun PlannerCheckInCard(
    actions: List<AddActivityViewModel.PlannerCheckInAction>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.14f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_goal),
                        contentDescription = "Goal Planner",
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
                    )
                }

                Text(
                    text = "Fokus Saat Ini",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    color = colorResource(id = R.color.white),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                actions.forEach { action ->
                    PlannerReminderRow(action = action)
                }
            }
        }
    }
}

@Composable
private fun PlannerReminderRow(
    action: AddActivityViewModel.PlannerCheckInAction
) {
    val statusColor = if (action.isDue) Color(0xFFFDE68A) else Color.White.copy(alpha = 0.72f)
    val isCheckedIn = !action.isDue
    val statusText = when {
        action.isTargetAchieved && !action.isDue -> "${action.cadenceLabel} • sudah sesuai target"
        isCheckedIn -> "${action.cadenceLabel} • sudah dicatat"
        else -> "${action.cadenceLabel} • belum dicatat"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = action.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = colorResource(id = R.color.white)
            )
            Text(
                text = statusText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = statusColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(18.dp)
                .border(
                    width = 1.5.dp,
                    color = if (isCheckedIn) statusColor else Color.White.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(5.dp)
                )
                .background(
                    color = if (isCheckedIn) statusColor.copy(alpha = 0.18f) else Color.Transparent,
                    shape = RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCheckedIn) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sudah check-in",
                    modifier = Modifier.size(12.dp),
                    tint = statusColor
                )
            }
        }
    }
}

@Composable
fun AnimatedTrackingButton(
    icon: Int,
    label: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    delayMillis: Int = 0,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(300, delayMillis = delayMillis)) +
                fadeIn(animationSpec = tween(300, delayMillis = delayMillis))
    ) {
        Column(
            modifier = modifier,
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
                    modifier = Modifier.size(iconSize),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.white))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorResource(id = R.color.white),
                textAlign = TextAlign.Center
            )
        }
    }
}
