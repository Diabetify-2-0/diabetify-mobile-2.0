package com.itb.diabetify.presentation.home.planner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily

@Composable
fun PlannerGoalDetailScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val latestRisk by viewModel.latestPredictionScore
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }
    val resolvedLatestRisk = latestRisk.takeIf { it > 0.0 } ?: goal?.currentRiskPercentage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        if (goal == null) {
            PlannerSectionScaffold(
                title = "Goal Planner",
                onBack = { navController.popBackStack() }
            ) {
                PlannerEmptyState(
                    message = "Belum ada goal aktif yang bisa ditampilkan."
                )
            }
            return
        }

        val featureModels = buildPlannerFeatureUiModels(goal, viewModel)
        val progressFraction = overallGoalProgress(goal, resolvedLatestRisk)
        val safeGoalId = goal.id

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            PlannerHeroSection(
                goal = goal,
                latestRisk = resolvedLatestRisk,
                progressFraction = progressFraction,
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlannerCardSection(title = "Progres Faktor") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        featureModels.forEach { feature ->
                            PlannerFactorProgressRow(feature = feature)
                        }
                    }
                }

                PlannerCardSection(title = "Fitur-fitur") {
                    PlannerFeatureShortcutGrid(
                        onOpenMilestone = {
                            navController.navigate(Route.PlannerMilestoneScreen.createRoute(safeGoalId))
                        },
                        onOpenAction = {
                            navController.navigate(Route.PlannerActionScreen.createRoute(safeGoalId))
                        },
                        onOpenCoach = {
                            navController.navigate(Route.PlannerCoachScreen.createRoute(safeGoalId))
                        },
                        onOpenChatbot = {
                            navController.navigate(Route.PlannerChatbotScreen.createRoute(safeGoalId))
                        },
                        onOpenCheckIn = {
                            navController.navigate(Route.PlannerCheckInScreen.createRoute(safeGoalId))
                        }
                    )
                }

                PlannerDangerZoneCard(
                    onDeleteGoal = {
                        navController.popBackStack()
                        viewModel.clearActivePlannerGoal()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlannerHeroSection(
    goal: PlannerGoal,
    latestRisk: Double?,
    progressFraction: Float,
    onBack: () -> Unit
) {
    val backgroundColor = Color(0xFF274254)
    val baselineRisk = goal.currentRiskPercentage
    val currentRisk = latestRisk ?: baselineRisk
    val targetRisk = goal.projectedRiskPercentage ?: goal.targetRiskPercentage.toDouble()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Text(
                text = "Goal Planner",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlannerRiskMetric(
                title = "Risiko Awal",
                value = formatRisk(baselineRisk),
                valueColor = Color(0xFFF09595)
            )
            PlannerRiskMetricDivider()
            PlannerRiskMetric(
                title = "Risiko Saat Ini",
                value = formatRisk(currentRisk),
                valueColor = Color(0xFF5DCAA5)
            )
            PlannerRiskMetricDivider()
            PlannerRiskMetric(
                title = "Risiko Target",
                value = formatRisk(targetRisk),
                valueColor = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.85f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF5DCAA5))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatRisk(baselineRisk),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White
            )
            Text(
                text = formatRisk(targetRisk),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RowScope.PlannerRiskMetric(
    title: String,
    value: String,
    valueColor: Color
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = title,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF8AACC8),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            color = valueColor
        )
    }
}

@Composable
private fun PlannerRiskMetricDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .width(1.dp)
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.18f))
    )
}

@Composable
private fun PlannerCardSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
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
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PlannerFactorProgressRow(
    feature: PlannerFeatureUiModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(feature.palette.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = feature.iconResId),
                    contentDescription = feature.progress.label,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(feature.palette.accentColor)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.progress.label,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = feature.currentHeadline,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF4B5563)
                )
            }

            Text(
                text = feature.trailingText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = feature.palette.accentColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(feature.palette.containerColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(feature.progress.progressFraction.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(feature.palette.accentColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = feature.startLabel,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = feature.endLabel,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun PlannerFeatureShortcutGrid(
    onOpenMilestone: () -> Unit,
    onOpenAction: () -> Unit,
    onOpenCoach: () -> Unit,
    onOpenChatbot: () -> Unit,
    onOpenCheckIn: () -> Unit
) {
    val shortcuts = plannerShortcuts()
    val topRows = shortcuts.take(4).chunked(2)
    val bottomShortcut = shortcuts.last()

    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        topRows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { itemIndex, shortcut ->
                    val onClick = when ((rowIndex * 2) + itemIndex) {
                        0 -> onOpenMilestone
                        1 -> onOpenAction
                        2 -> onOpenCoach
                        else -> onOpenChatbot
                    }
                    PlannerShortcutCard(
                        shortcut = shortcut,
                        onClick = onClick
                    )
                }
            }
        }

        PlannerShortcutCard(
            shortcut = bottomShortcut,
            onClick = onOpenCheckIn
        )
    }
}

@Composable
private fun PlannerShortcutCard(
    shortcut: PlannerShortcut,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(102.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(shortcut.palette.containerColor),
                contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = shortcut.iconResId),
                        contentDescription = shortcut.label,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(shortcut.palette.accentColor)
                    )
                }
            Text(
                text = shortcut.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun PlannerDangerZoneCard(
    onDeleteGoal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Zona Berbahaya",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onDeleteGoal)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF1F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_planner_trash),
                        contentDescription = "Hapus goal",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color(0xFFFA4D5E))
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Hapus goal ini",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFFF2D2D)
                )
            }
        }
    }
}

@Composable
internal fun plannerSectionHeaderBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            colorResource(id = R.color.primary),
            colorResource(id = R.color.primary).copy(alpha = 0.8f)
        )
    )
}

@Composable
internal fun PlannerCardContainer(
    title: String,
    body: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = plannerSectionHeaderBrush()
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                body()
            }
        }
    }
}

@Composable
internal fun PlannerSectionScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = colorResource(id = R.color.primary)
                )
            }
            Text(
                text = title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.primary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun PlannerInfoCard(
    title: String,
    body: @Composable ColumnScope.() -> Unit
) {
    PlannerCardContainer(title = title, body = body)
}

@Composable
internal fun PlannerSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1F2937)
        )
        subtitle?.let {
            Text(
                text = it,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
internal fun PlannerEmptyState(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = message,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            )
        }
    }
}

@Composable
internal fun PlannerListItem(
    index: Int,
    text: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = accentColor
            )
        }
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
}
