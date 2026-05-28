package com.itb.diabetify.presentation.home.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlannerGoalHistoryScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val goalHistory by viewModel.plannerGoalHistory
    val allCheckInHistory by viewModel.allPlannerCheckInHistory
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.refreshPlannerGoalHistory()
    }

    LaunchedEffect(goalHistory) {
        goalHistory.take(10).forEach { goal ->
            viewModel.refreshPlannerCheckInHistoryForGoal(goal.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        PlannerHistoryHeader(
            onBack = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeCard(title = "Riwayat Goal Planner") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (goalHistory.isEmpty()) {
                        EmptyPlannerHistory(
                            onCreatePlan = {
                                navController.navigate(Route.CounterfactualScreen.route)
                            }
                        )
                    } else {
                        goalHistory.forEachIndexed { index, goal ->
                            PlannerHistoryItem(
                                goal = goal,
                                checkInCount = allCheckInHistory.count { it.goalId == goal.id },
                                onClick = {
                                    navController.navigate(Route.PlannerGoalDetailScreen.createRoute(goal.id))
                                }
                            )
                            if (index != goalHistory.lastIndex) {
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlannerHistoryHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(id = R.color.primary)
            )
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Riwayat Planner",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun EmptyPlannerHistory(
    onCreatePlan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Belum ada riwayat goal planner. Simpan hasil counterfactual sebagai goal untuk mulai membangun riwayat.",
            fontFamily = poppinsFontFamily,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF4B5563)
        )
        PrimaryButton(
            text = "Buat Rencana",
            onClick = onCreatePlan,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Composable
private fun PlannerHistoryItem(
    goal: PlannerGoal,
    checkInCount: Int,
    onClick: () -> Unit
) {
    val statusMeta = plannerStatusMeta(goal.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = goal.title,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = "Dibuat ${formatGoalHistoryDate(goal.createdAtMillis)}",
                        fontFamily = poppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusMeta.containerColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusMeta.icon,
                        contentDescription = null,
                        tint = statusMeta.textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = statusMeta.label,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusMeta.textColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Awal",
                    value = formatHistoryRisk(goal.currentRiskPercentage)
                )
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Target",
                    value = "<${goal.targetRiskPercentage}%"
                )
                PlannerHistoryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Check-in",
                    value = checkInCount.toString()
                )
            }

            if (goal.features.isNotEmpty()) {
                Text(
                    text = goal.features.take(2).joinToString(" • ") { it.label },
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}

@Composable
private fun PlannerHistoryMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 10.sp,
            color = Color(0xFF6B7280),
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = colorResource(id = R.color.primary),
            maxLines = 1
        )
    }
}

private data class PlannerStatusMeta(
    val label: String,
    val textColor: Color,
    val containerColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun plannerStatusMeta(status: PlannerGoalStatus): PlannerStatusMeta {
    return when (status) {
        PlannerGoalStatus.ACTIVE -> PlannerStatusMeta(
            label = "Aktif",
            textColor = Color(0xFF047857),
            containerColor = Color(0xFFECFDF5),
            icon = Icons.Outlined.Info
        )
        PlannerGoalStatus.COMPLETED -> PlannerStatusMeta(
            label = "Selesai",
            textColor = Color(0xFF059669),
            containerColor = Color(0xFFD1FAE5),
            icon = Icons.Outlined.CheckCircle
        )
        PlannerGoalStatus.ARCHIVED -> PlannerStatusMeta(
            label = "Arsip",
            textColor = Color(0xFF475569),
            containerColor = Color(0xFFE2E8F0),
            icon = Icons.Outlined.Info
        )
    }
}

private fun formatHistoryRisk(value: Double?): String {
    return value?.let { String.format("%.1f%%", it) } ?: "-"
}

private fun formatGoalHistoryDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(Date(timestamp))
}
