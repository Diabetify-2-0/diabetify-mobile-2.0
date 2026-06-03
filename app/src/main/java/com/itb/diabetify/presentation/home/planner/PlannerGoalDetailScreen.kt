package com.itb.diabetify.presentation.home.planner

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalFeature
import com.itb.diabetify.presentation.common.CustomizableButton
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.home.components.HomeCard
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PlannerGoalDetailScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val latestRisk by viewModel.latestPredictionScore
    val activeCheckInHistory by viewModel.plannerCheckInHistory
    val allCheckInHistory by viewModel.allPlannerCheckInHistory
    val scrollState = rememberScrollState()
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }
    val checkInHistory = goalId
        ?.let { id -> allCheckInHistory.filter { it.goalId == id }.sortedByDescending { it.createdAtMillis } }
        ?: activeCheckInHistory
    val navigateBackToHome: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate(Route.HomeScreen.route) {
                launchSingleTop = true
            }
        }
    }
    val removeGoalAndReturnHome: () -> Unit = {
        navigateBackToHome()
        viewModel.clearActivePlannerGoal()
    }
    val completeGoalAndReturnHome: () -> Unit = {
        navigateBackToHome()
        viewModel.completeActivePlannerGoal()
    }

    LaunchedEffect(goalId) {
        if (!goalId.isNullOrBlank()) {
            viewModel.refreshPlannerCheckInHistoryForGoal(goalId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        PlannerGoalHeader(
            onBack = navigateBackToHome
        )

        if (goal == null) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            val safeGoal = goal
            val featureProgress = safeGoal.features.map { feature ->
                buildFeatureProgress(
                    feature = feature,
                    currentValue = currentFeatureValue(feature.featureName, viewModel),
                    heightCm = viewModel.height.value
                )
            }
            val completionState = buildGoalCompletionState(
                goal = safeGoal,
                featureProgress = featureProgress,
                latestRisk = latestRisk.takeIf { it > 0.0 }
            )
            val currentMilestoneWeek = currentMilestoneWeek(safeGoal.createdAtMillis)
            val milestones = safeGoal.features.mapNotNull { feature ->
                buildWeeklyMilestone(
                    feature = feature,
                    currentValue = currentFeatureValue(feature.featureName, viewModel),
                    currentWeek = currentMilestoneWeek,
                    heightCm = viewModel.height.value
                )
            }

            val coachNote = buildWeeklyCoachNote(
                goal = safeGoal,
                milestones = milestones,
                history = checkInHistory,
                latestRisk = latestRisk.takeIf { it > 0.0 },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                RiskProgressCard(
                    goal = safeGoal,
                    latestRisk = latestRisk.takeIf { it > 0.0 }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (completionState.shouldShow) {
                    GoalCompletionSection(
                        completionState = completionState,
                        onCompleteGoal = completeGoalAndReturnHome
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                WeeklyCoachSection(coachNote = coachNote)
                Spacer(modifier = Modifier.height(16.dp))

                if (milestones.isNotEmpty()) {
                    WeeklyMilestoneSection(
                        currentWeek = currentMilestoneWeek,
                        milestones = milestones
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (safeGoal.features.isNotEmpty()) {
                    HomeCard(title = "Progress Target") {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            featureProgress.forEach { progress ->
                                GoalFeatureProgressRow(progress = progress)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                CheckInTimelineSection(
                    history = checkInHistory
                )
                Spacer(modifier = Modifier.height(16.dp))

                GoalListSection(
                    title = "Langkah Aksi",
                    emptyText = "Langkah aksi belum tersedia dari planner.",
                    items = safeGoal.actionSteps
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = if (completionState.isCompleted) "Buat Rencana Baru" else "Perbarui Data Kesehatan",
                    onClick = {
                        if (completionState.isCompleted) {
                            navController.navigate(Route.CounterfactualScreen.route)
                        } else {
                            navController.navigate(Route.HealthProfileScreen.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                CustomizableButton(
                    text = if (completionState.isCompleted) "Tutup Goal" else "Hapus Goal",
                    onClick = removeGoalAndReturnHome,
                    backgroundColor = Color(0xFFEF4444),
                    backgroundColorSecondary = Color(0xFFDC2626),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GoalCompletionSection(
    completionState: GoalCompletionState,
    onCompleteGoal: () -> Unit
) {
    HomeCard(title = if (completionState.isCompleted) "Goal Selesai" else "Goal Siap Diselesaikan") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(22.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = completionState.title,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            color = colorResource(id = R.color.primary)
                        )
                        Text(
                            text = completionState.message,
                            fontFamily = poppinsFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                completionState.highlights.forEachIndexed { index, highlight ->
                    GoalListItem(
                        number = index + 1,
                        text = highlight
                    )
                }
            }

            if (!completionState.isCompleted) {
                PrimaryButton(
                    text = "Tandai Goal Selesai",
                    onClick = onCompleteGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckInTimelineSection(
    history: List<PlannerCheckInEntry>
) {
    HomeCard(title = "Timeline Check-in") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (history.isEmpty()) {
                Text(
                    text = "Belum ada check-in untuk goal ini. Gunakan tombol tengah untuk mulai mencatat progres.",
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                history.take(6).forEachIndexed { index, entry ->
                    CheckInTimelineItem(entry = entry)
                    if (index != history.take(6).lastIndex) {
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInTimelineItem(
    entry: PlannerCheckInEntry
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFECFDF5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entry.label,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.primary),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimelineDate(entry.createdAtMillis),
                    fontFamily = poppinsFontFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.End
                )
            }

            Text(
                text = entry.valueText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF059669)
            )

            Text(
                text = entry.note,
                fontFamily = poppinsFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun WeeklyMilestoneSection(
    currentWeek: Int,
    milestones: List<PlannerWeeklyMilestone>
) {
    HomeCard(title = "Milestone Mingguan") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFF6FF))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Minggu $currentWeek dari $MILESTONE_TOTAL_WEEKS",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = "Target minggu ini dihitung bertahap dari baseline menuju target akhir.",
                        fontFamily = poppinsFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF4B5563)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }

            milestones.forEachIndexed { index, milestone ->
                WeeklyMilestoneItem(milestone = milestone)
                if (index != milestones.lastIndex) {
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                }
            }
        }
    }
}

@Composable
private fun WeeklyMilestoneItem(
    milestone: PlannerWeeklyMilestone
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = milestone.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colorResource(id = R.color.primary),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = milestone.statusText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = milestone.statusColor,
                textAlign = TextAlign.End
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Saat ini",
                value = milestone.currentText
            )
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Minggu ini",
                value = milestone.expectedText
            )
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Target akhir",
                value = milestone.finalTargetText
            )
        }

        GoalProgressBar(
            progress = milestone.progressFraction,
            color = milestone.statusColor
        )
    }
}

@Composable
private fun WeeklyCoachSection(
    coachNote: WeeklyCoachNote
) {
    HomeCard(title = "Catatan Coach Minggu Ini") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = coachNote.headline,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = coachNote.message,
                        fontFamily = poppinsFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                coachNote.suggestions.forEachIndexed { index, suggestion ->
                    GoalListItem(
                        number = index + 1,
                        text = suggestion
                    )
                }
            }

            Text(
                text = coachNote.disclaimer,
                fontFamily = poppinsFontFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

private data class PlannerFeatureProgress(
    val label: String,
    val baselineText: String,
    val currentText: String,
    val targetText: String,
    val actionText: String,
    val progressFraction: Float,
    val statusText: String,
    val isTargetReached: Boolean
)

private data class PlannerWeeklyMilestone(
    val label: String,
    val currentText: String,
    val expectedText: String,
    val finalTargetText: String,
    val progressFraction: Float,
    val statusText: String,
    val statusColor: Color,
    val status: MilestoneStatus
)

private enum class MilestoneStatus {
    ACHIEVED,
    ON_TRACK,
    BEHIND,
    MONITOR
}

private data class WeeklyCoachNote(
    val headline: String,
    val message: String,
    val suggestions: List<String>,
    val disclaimer: String = "Catatan ini bersifat pendamping perilaku dan bukan pengganti konsultasi tenaga kesehatan."
)

private data class GoalCompletionState(
    val isCompleted: Boolean,
    val isEligible: Boolean,
    val title: String,
    val message: String,
    val highlights: List<String>
) {
    val shouldShow: Boolean = isCompleted || isEligible
}

@Composable
private fun PlannerGoalHeader(
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
            text = "Goal Planner",
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}


@Composable
private fun RiskProgressCard(
    goal: PlannerGoal,
    latestRisk: Double?
) {
    HomeCard(title = "Arah Risiko") {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RiskMetricPanel(
                    modifier = Modifier.weight(1f),
                    label = "Awal",
                    value = formatRisk(goal.currentRiskPercentage),
                    containerColor = Color(0xFFFFF7ED),
                    valueColor = Color(0xFFEA580C)
                )
                RiskMetricPanel(
                    modifier = Modifier.weight(1f),
                    label = "Target",
                    value = formatRisk(goal.projectedRiskPercentage),
                    containerColor = Color(0xFFECFDF5),
                    valueColor = Color(0xFF059669)
                )

            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

        }
    }
}

@Composable
private fun RiskMetricPanel(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun GoalFeatureProgressRow(progress: PlannerFeatureProgress) {
    val statusColor = if (progress.isTargetReached) Color(0xFF059669) else Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
            text = progress.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colorResource(id = R.color.primary),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Awal",
                value = progress.baselineText
            )
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Saat ini",
                value = progress.currentText
            )
            FeatureValuePanel(
                modifier = Modifier.weight(1f),
                label = "Target",
                value = progress.targetText
            )
        }

        GoalProgressBar(
            progress = progress.progressFraction,
            color = statusColor
        )

        Text(
            text = progress.statusText,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = statusColor
        )

        Text(
            text = progress.actionText,
            fontFamily = poppinsFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color(0xFF4B5563)
        )
    }
}

@Composable
private fun GoalProgressBar(
    progress: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(color)
        )
    }
}

@Composable
private fun FeatureValuePanel(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontSize = 10.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = colorResource(id = R.color.primary)
        )
    }
}

@Composable
private fun GoalListSection(
    title: String,
    emptyText: String,
    items: List<String>
) {
    HomeCard(title = title) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                Text(
                    text = emptyText,
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                items.forEachIndexed { index, item ->
                    GoalListItem(
                        number = index + 1,
                        text = sanitizePlannerText(item)
                    )
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalListItem(
    number: Int,
    text: String
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
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF2563EB)
            )
        }
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatRisk(value: Double?): String {
    return value?.let { String.format("%.1f%%", it) } ?: "-"
}

private fun buildGoalCompletionState(
    goal: PlannerGoal,
    featureProgress: List<PlannerFeatureProgress>,
    latestRisk: Double?
): GoalCompletionState {
    val riskReached = latestRisk != null &&
        latestRisk <= goal.targetRiskPercentage + COMPLETION_RISK_BUFFER_PERCENTAGE
    val featureTargetsReached = featureProgress.isNotEmpty() &&
        featureProgress.all { it.isTargetReached }
    val isEligible = riskReached || featureTargetsReached

    val reason = when {
        riskReached -> "Risiko terbaru sudah berada di sekitar target planner (${formatRisk(latestRisk)} dari target <${goal.targetRiskPercentage}%)."
        featureTargetsReached -> "Semua faktor yang dipilih sudah mencapai target perubahan pada rencana ini."
        else -> "Goal masih berjalan dan belum memenuhi target penutupan."
    }
    val completedFeatureCount = featureProgress.count { it.isTargetReached }
    val highlights = buildList {
        add(reason)
        if (featureProgress.isNotEmpty()) {
            add("$completedFeatureCount dari ${featureProgress.size} target faktor sudah tercapai.")
        }
        goal.projectedRiskPercentage?.let { projectedRisk ->
            add("Skenario awal planner memproyeksikan risiko ke ${formatRisk(projectedRisk)}.")
        }
    }

    return GoalCompletionState(
        isCompleted = false,
        isEligible = isEligible,
        title = "Progres sudah cukup untuk menutup goal",
        message = "Anda bisa menandai goal ini selesai. Setelah dikonfirmasi, goal akan dihapus dari planner aktif.",
        highlights = highlights.distinct().take(3)
    )
}

private fun currentMilestoneWeek(createdAtMillis: Long): Int {
    val elapsedMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)
    val elapsedWeeks = ceil(elapsedMillis.toDouble() / WEEK_MILLIS).toInt().coerceAtLeast(1)
    return elapsedWeeks.coerceIn(1, MILESTONE_TOTAL_WEEKS)
}

private fun buildWeeklyMilestone(
    feature: PlannerGoalFeature,
    currentValue: Double?,
    currentWeek: Int,
    heightCm: Int
): PlannerWeeklyMilestone? {
    val baseline = feature.baselineValue ?: return null
    val target = feature.targetValue ?: return null
    val currentText = formatFeatureValue(feature.featureName, currentValue, heightCm)
    val finalTargetText = formatFeatureValue(feature.featureName, target, heightCm)

    if (isCategoricalFeature(feature.featureName)) {
        val reached = isTargetReached(
            featureName = feature.featureName,
            baseline = baseline,
            target = target,
            current = currentValue
        )
        return PlannerWeeklyMilestone(
            label = displayFeatureLabel(feature),
            currentText = currentText,
            expectedText = if (currentWeek >= MILESTONE_TOTAL_WEEKS) finalTargetText else "Pantau",
            finalTargetText = finalTargetText,
            progressFraction = if (reached) 1f else 0f,
            statusText = if (reached) "Tercapai" else "Pantau",
            statusColor = if (reached) Color(0xFF059669) else Color(0xFF2563EB),
            status = if (reached) MilestoneStatus.ACHIEVED else MilestoneStatus.MONITOR
        )
    }

    val expectedFraction = currentWeek.toDouble() / MILESTONE_TOTAL_WEEKS
    val expectedValue = baseline + ((target - baseline) * expectedFraction)
    val progressFraction = calculateProgressFraction(
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val reached = isTargetReached(
        featureName = feature.featureName,
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val onTrack = reached || progressFraction + MILESTONE_TOLERANCE >= expectedFraction.toFloat()
    val statusText = when {
        reached -> "Tercapai"
        onTrack -> "On track"
        else -> "Tertinggal"
    }
    val statusColor = when {
        reached -> Color(0xFF059669)
        onTrack -> Color(0xFF2563EB)
        else -> Color(0xFFEA580C)
    }
    val status = when {
        reached -> MilestoneStatus.ACHIEVED
        onTrack -> MilestoneStatus.ON_TRACK
        else -> MilestoneStatus.BEHIND
    }

    return PlannerWeeklyMilestone(
        label = displayFeatureLabel(feature),
        currentText = currentText,
        expectedText = formatFeatureValue(feature.featureName, expectedValue, heightCm),
        finalTargetText = finalTargetText,
        progressFraction = if (reached) 1f else progressFraction,
        statusText = statusText,
        statusColor = statusColor,
        status = status
    )
}

private fun buildWeeklyCoachNote(
    goal: PlannerGoal,
    milestones: List<PlannerWeeklyMilestone>,
    history: List<PlannerCheckInEntry>,
    latestRisk: Double?,
): WeeklyCoachNote {
    val achievedCount = milestones.count { it.status == MilestoneStatus.ACHIEVED }
    val behindMilestones = milestones.filter { it.status == MilestoneStatus.BEHIND }
    val onTrackMilestones = milestones.filter { it.status == MilestoneStatus.ON_TRACK }
    val recentCheckIn = history.maxByOrNull { it.createdAtMillis }
    val focusMilestone = behindMilestones.firstOrNull()
        ?: onTrackMilestones.firstOrNull()
        ?: milestones.firstOrNull()

    val headline = when {
        behindMilestones.isNotEmpty() -> "Fokuskan minggu ini pada ${focusMilestone?.label ?: "target utama"}"
        achievedCount > 0 && achievedCount == milestones.size -> "Semua target utama sudah berada di jalur baik"
        recentCheckIn == null -> "Mulai dari check-in pertama untuk membaca progres"
        else -> "Pertahankan ritme check-in dan progres bertahap"
    }

    val message = when {
        recentCheckIn == null -> "Goal sudah tersimpan, tetapi planner belum memiliki catatan check-in. Satu check-in sederhana sudah cukup untuk mulai membangun timeline progres."
        latestRisk != null -> "Risiko terbaru Anda tercatat ${formatRisk(latestRisk)}. Gunakan angka ini sebagai arah umum, lalu lihat perubahan faktor untuk mengetahui tindakan yang paling berdampak."
        else -> goal.summary?.takeIf { it.isNotBlank() }?.let(::sanitizePlannerText)
            ?: "Planner akan lebih berguna setelah Anda melakukan check-in dan prediksi terbaru tersedia."
    }

    val suggestions = mutableListOf<String>()
    if (focusMilestone != null) {
        suggestions += "Prioritaskan ${focusMilestone.label}: saat ini ${focusMilestone.currentText}, target minggu ini ${focusMilestone.expectedText}."
    }
    if (recentCheckIn != null) {
        suggestions += "Check-in terakhir: ${recentCheckIn.label} (${recentCheckIn.valueText}) pada ${formatTimelineDate(recentCheckIn.createdAtMillis)}."
    } else {
        suggestions += "Gunakan tombol tengah untuk melakukan check-in pertama sesuai goal aktif."
    }
    if (behindMilestones.isNotEmpty()) {
        suggestions += "Jangan ubah banyak hal sekaligus; pilih satu faktor tertinggal dan lakukan update data konsisten minggu ini."
    } else {
        suggestions += "Pertahankan pola yang sudah berjalan dan lakukan review ulang setelah check-in berikutnya."
    }

    return WeeklyCoachNote(
        headline = headline,
        message = message,
        suggestions = suggestions.distinct().take(3)
    )
}

private fun buildFeatureProgress(
    feature: PlannerGoalFeature,
    currentValue: Double?,
    heightCm: Int
): PlannerFeatureProgress {
    val baseline = feature.baselineValue
    val target = feature.targetValue
    val progressFraction = calculateProgressFraction(
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val isReached = isTargetReached(
        featureName = feature.featureName,
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val progressPercentage = (progressFraction * 100).roundToInt()

    return PlannerFeatureProgress(
        label = displayFeatureLabel(feature),
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = formatFeatureValue(feature.featureName, currentValue, heightCm),
        targetText = formatFeatureValue(feature.featureName, target, heightCm),
        actionText = displayFeatureActionText(feature, heightCm),
        progressFraction = if (isReached) 1f else progressFraction,
        statusText = when {
            isReached -> "Target tercapai"
            progressPercentage <= 0 -> "Belum ada perubahan menuju target"
            else -> "$progressPercentage% menuju target"
        },
        isTargetReached = isReached
    )
}

private fun calculateProgressFraction(
    baseline: Double?,
    target: Double?,
    current: Double?
): Float {
    if (baseline == null || target == null || current == null) {
        return 0f
    }

    val distance = target - baseline
    if (distance == 0.0) {
        return if (current == target) 1f else 0f
    }

    return ((current - baseline) / distance).toFloat().coerceIn(0f, 1f)
}

private fun isTargetReached(
    featureName: String,
    baseline: Double?,
    target: Double?,
    current: Double?
): Boolean {
    if (baseline == null || target == null || current == null) {
        return false
    }

    return when (featureName) {
        "smoking_status",
        "is_hypertension",
        "is_cholesterol",
        "is_bloodline",
        "is_macrosomic_baby" -> current.roundToInt() == target.roundToInt()
        else -> {
            val tolerance = 0.05
            when {
                kotlin.math.abs(current - target) <= tolerance -> true
                target > baseline -> current >= target
                target < baseline -> current <= target
                else -> false
            }
        }
    }
}

private fun isCategoricalFeature(featureName: String): Boolean {
    return featureName in setOf(
        "smoking_status",
        "brinkman_index",
        "is_hypertension",
        "is_cholesterol",
        "is_bloodline",
        "is_macrosomic_baby"
    )
}

private fun currentFeatureValue(
    featureName: String,
    viewModel: HomeViewModel
): Double? {
    return when (featureName) {
        "BMI" -> viewModel.bmi.value.takeIf { it > 0.0 }
        "moderate_physical_activity_frequency" -> viewModel.physicalActivityAverage.value.toDouble()
        "smoking_status" -> viewModel.smokingStatus.value.toDoubleOrNull()
        "is_hypertension" -> if (viewModel.isHypertension.value) 1.0 else 0.0
        "is_cholesterol" -> if (viewModel.isCholesterol.value) 1.0 else 0.0
        "is_bloodline" -> if (viewModel.isBloodline.value) 1.0 else 0.0
        "is_macrosomic_baby" -> viewModel.macrosomicBaby.value.toDouble()
        "age" -> viewModel.baselineAge.value.takeIf { it > 0 }?.toDouble()
        else -> null
    }
}

private fun formatFeatureValue(name: String, value: Double?): String {
    return formatFeatureValue(name, value, 0)
}

private fun displayFeatureLabel(feature: PlannerGoalFeature): String {
    return if (feature.featureName == "BMI") {
        "Berat Badan"
    } else {
        sanitizePlannerText(feature.label)
    }
}

private fun displayFeatureActionText(
    feature: PlannerGoalFeature,
    heightCm: Int
): String {
    if (feature.featureName != "BMI") {
        return sanitizePlannerText(feature.actionLabel)
    }

    val baselineWeight = feature.baselineValue?.let { bmiToWeight(it, heightCm) }
    val targetWeight = feature.targetValue?.let { bmiToWeight(it, heightCm) }
    if (baselineWeight == null || targetWeight == null) {
        return "Pantau perubahan berat badan secara bertahap sesuai target planner."
    }

    val delta = targetWeight - baselineWeight
    return if (delta < 0) {
        "Turunkan berat sekitar ${String.format("%.1f", kotlin.math.abs(delta))} kg dari baseline."
    } else {
        "Naikkan berat sekitar ${String.format("%.1f", delta)} kg dari baseline."
    }
}

private fun formatFeatureValue(
    name: String,
    value: Double?,
    heightCm: Int
): String {
    if (value == null) {
        return "-"
    }

    return when (name) {
        "BMI" -> bmiToWeightText(value, heightCm)
        "age" -> "${value.toInt()} tahun"
        "moderate_physical_activity_frequency" -> "${value.toInt()} hari/minggu"
        "smoking_status" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> "Sudah berhenti"
            2 -> "Masih aktif"
            else -> value.toInt().toString()
        }
        "brinkman_index" -> when (value.toInt()) {
            0 -> "Sangat rendah"
            1 -> "Ringan"
            2 -> "Sedang"
            3 -> "Tinggi"
            else -> value.toInt().toString()
        }
        "is_hypertension", "is_cholesterol", "is_bloodline" -> if (value.toInt() == 1) "Ya" else "Tidak"
        "is_macrosomic_baby" -> when (value.toInt()) {
            0 -> "Tidak"
            1 -> "Ya"
            2 -> "Tidak relevan"
            else -> value.toInt().toString()
        }
        else -> String.format("%.2f", value)
    }
}

private fun bmiToWeightText(bmi: Double, heightCm: Int): String {
    val weight = bmiToWeight(bmi, heightCm) ?: return "-"
    return "${String.format("%.1f", weight)} kg"
}

private fun bmiToWeight(bmi: Double, heightCm: Int): Double? {
    val heightMeters = heightCm / 100.0
    if (heightMeters <= 0.0) {
        return null
    }
    return bmi * heightMeters * heightMeters
}

private fun sanitizePlannerText(text: String): String {
    return text
        .replace(Regex("\\bBMI\\b", RegexOption.IGNORE_CASE), "berat badan")
        .replace(Regex("\\bIMT\\b", RegexOption.IGNORE_CASE), "berat badan")
}

private fun formatCreatedDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(Date(timestamp))
}

private fun formatTimelineDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
    return formatter.format(Date(timestamp))
}

private const val MILESTONE_TOTAL_WEEKS = 12
private const val MILESTONE_TOLERANCE = 0.05f
private const val COMPLETION_RISK_BUFFER_PERCENTAGE = 2.0
private const val WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L
