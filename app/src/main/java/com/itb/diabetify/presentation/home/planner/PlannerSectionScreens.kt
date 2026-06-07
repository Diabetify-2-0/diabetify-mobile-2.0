package com.itb.diabetify.presentation.home.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.presentation.common.PrimaryButton
import com.itb.diabetify.presentation.home.HomeViewModel
import com.itb.diabetify.presentation.navgraph.Route
import com.itb.diabetify.ui.theme.poppinsFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
@Composable
fun PlannerMilestoneScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val allCheckInHistory by viewModel.allPlannerCheckInHistory
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }

    PlannerSectionScaffold(
        title = "Milestone",
        onBack = { navController.popBackStack() }
    ) {
        if (goal == null) {
            PlannerEmptyState("Milestone belum tersedia karena tidak ada goal aktif.")
            return@PlannerSectionScaffold
        }

        val currentWeek = currentMilestoneWeek(goal.createdAtMillis, goal.durationWeeks)
        val history = goalId
            ?.let { id -> allCheckInHistory.filter { it.goalId == id } }
            ?: viewModel.plannerCheckInHistory.value
        val milestones = goal.features.mapNotNull { feature ->
            buildWeeklyMilestone(
                feature = feature,
                currentValue = currentFeatureValue(feature.featureName, viewModel),
                currentWeek = currentWeek,
                totalWeeks = goal.durationWeeks,
                heightCm = viewModel.height.value,
                history = history
            )
        }
        val milestoneCards = buildMilestoneCardUiModels(milestones)

        PlannerMilestoneHeaderCard(
            currentWeek = currentWeek,
            totalWeeks = goal.durationWeeks
        )

        milestoneCards.forEach { milestoneCard ->
            when (milestoneCard.type) {
                PlannerMilestoneCardType.NUMERIC -> PlannerNumericMilestoneCard(milestoneCard)
                PlannerMilestoneCardType.CATEGORICAL -> PlannerCategoricalMilestoneCard(milestoneCard)
            }
        }
    }
}

@Composable
private fun PlannerMilestoneHeaderCard(
    currentWeek: Int,
    totalWeeks: Int
) {
    val checkpoints = milestoneHeaderCheckpoints(totalWeeks)
    val activeStepIndex = checkpoints.indexOfLast { currentWeek >= it.week }.coerceAtLeast(0)
    val activeContainerColor = Color(0xFFF9FAFB)
    val inactiveContainerColor = Color(0xFF3A5467)
    val activeTextColor = Color(0xFF2D475B)
    val inactiveTextColor = Color(0xFF98A8B6)
    val activeLineColor = Color(0xFFF9FAFB)
    val inactiveLineColor = Color(0xFF486274)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D475B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Minggu $currentWeek dari $totalWeeks",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Fokus pada target minggu ini!",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    checkpoints.forEachIndexed { index, step ->
                        val isActive = index <= activeStepIndex
                        val stepContainerColor = if (isActive) activeContainerColor else inactiveContainerColor
                        val stepTextColor = if (isActive) activeTextColor else inactiveTextColor

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(stepContainerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.week.toString(),
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = stepTextColor
                            )
                        }

                        if (index < checkpoints.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(
                                        if (index < activeStepIndex) activeLineColor else inactiveLineColor,
                                        RoundedCornerShape(999.dp)
                                    )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    checkpoints.forEach { step ->
                        Text(
                            text = step.label,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }
    }
}

private data class MilestoneHeaderCheckpoint(
    val week: Int,
    val label: String
)

private fun milestoneHeaderCheckpoints(totalWeeks: Int): List<MilestoneHeaderCheckpoint> {
    val safeTotalWeeks = totalWeeks.coerceAtLeast(1)
    val weekMarkers = listOf(
        1,
        (1 + ((safeTotalWeeks - 1) / 3f)).toInt(),
        (1 + (2f * (safeTotalWeeks - 1) / 3f)).toInt(),
        safeTotalWeeks
    ).fold(mutableListOf<Int>()) { acc, marker ->
        val nextValue = marker.coerceAtLeast((acc.lastOrNull() ?: 0) + 1).coerceAtMost(safeTotalWeeks)
        acc += nextValue
        acc
    }

    return weekMarkers.mapIndexed { index, week ->
        MilestoneHeaderCheckpoint(
            week = week,
            label = when (index) {
                0 -> "Sekarang"
                weekMarkers.lastIndex -> "Selesai"
                else -> "Minggu $week"
            }
        )
    }
}

@Composable
private fun PlannerNumericMilestoneCard(
    milestone: PlannerMilestoneCardUiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlannerMilestoneCardHeader(milestone)

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                PlannerMilestoneMetricCard(
                    modifier = Modifier.weight(0.94f),
                    label = milestone.currentLabel,
                    value = milestone.currentValueText,
                    containerColor = milestone.currentValueContainerColor,
                    valueColor = milestone.currentValueColor
                )
                PlannerMilestoneMetricCard(
                    modifier = Modifier.weight(1.2f),
                    label = milestone.weeklyLabel,
                    value = milestone.weeklyValueText,
                    containerColor = Color(0xFFDDF4EC),
                    valueColor = Color(0xFF175C4A)
                )
                PlannerMilestoneMetricCard(
                    modifier = Modifier.weight(0.94f),
                    label = milestone.targetLabel,
                    value = milestone.targetValueText,
                    containerColor = Color(0xFFF6F6F6),
                    valueColor = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            PlannerProgressBar(
                progress = milestone.progressFraction,
                accentColor = milestone.progressColor,
                trackColor = milestone.progressTrackColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlannerMilestoneCaption(milestone.baselineCaption)
                PlannerMilestoneCaption(milestone.trailingCaption, textAlign = TextAlign.End)
            }

            milestone.highlight?.let { highlight ->
                PlannerMilestoneHighlightBanner(highlight)
            }
        }
    }
}

@Composable
private fun PlannerCategoricalMilestoneCard(
    milestone: PlannerMilestoneCardUiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PlannerMilestoneCardHeader(milestone)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = milestone.transitionFromText.orEmpty(),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = milestone.transitionFromColor
                )
                if (!milestone.transitionToText.isNullOrBlank()) {
                    Text(
                        text = "→",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = milestone.transitionToColor
                    )
                    Text(
                        text = milestone.transitionToText.orEmpty(),
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = milestone.transitionToColor
                    )
                }
            }

            PlannerProgressBar(
                progress = milestone.progressFraction,
                accentColor = milestone.progressColor,
                trackColor = milestone.progressTrackColor
            )

            milestone.highlight?.let { highlight ->
                PlannerMilestoneHighlightBanner(highlight)
            }
        }
    }
}

@Composable
private fun PlannerMilestoneCardHeader(
    milestone: PlannerMilestoneCardUiModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(milestone.iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = milestone.iconResId),
                    contentDescription = null,
                    tint = milestone.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = milestone.title,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF304459)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(milestone.statusContainerColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = milestone.statusText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                color = milestone.statusTextColor
            )
        }
    }
}

@Composable
private fun PlannerMilestoneMetricCard(
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
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = Color(0xFFB2B2B2),
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = valueColor
        )
    }
}

@Composable
private fun PlannerMilestoneCaption(
    text: String,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontFamily = poppinsFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color(0xFF5B5B5B),
        textAlign = textAlign
    )
}

@Composable
private fun PlannerMilestoneHighlightBanner(
    highlight: PlannerMilestoneHighlight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(highlight.containerColor)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(id = highlight.iconResId),
            contentDescription = null,
            tint = highlight.iconColor,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = highlight.message,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 18.sp,
            color = highlight.textColor,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun PlannerActionScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val latestRisk by viewModel.latestPredictionScore
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }

    PlannerSectionScaffold(
        title = "Aksi",
        onBack = { navController.popBackStack() }
    ) {
        if (goal == null) {
            PlannerEmptyState("Langkah aksi belum tersedia karena tidak ada goal aktif.")
            return@PlannerSectionScaffold
        }

        val featureProgress = goal.features.map { feature ->
            buildFeatureProgress(
                feature = feature,
                currentValue = currentFeatureValue(feature.featureName, viewModel),
                heightCm = viewModel.height.value
            )
        }
        val completionState = buildGoalCompletionState(
            goal = goal,
            featureProgress = featureProgress,
            latestRisk = latestRisk.takeIf { it > 0.0 }
        )

        PlannerSectionTitle(
            subtitle = "Gunakan daftar ini sebagai panduan tindakan paling relevan untuk mengejar target risiko Anda."
        )

        PlannerInfoCard(title = "Ringkasan") {
            Text(
                text = goal.summary?.let(::sanitizePlannerText)
                    ?: "Planner belum memberikan ringkasan tambahan untuk goal ini.",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color(0xFF4B5563)
            )
        }

        PlannerInfoCard(title = "Prioritas Faktor") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                featureProgress.forEach { progress ->
                    Column(
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = progress.statusText,
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (progress.isTargetReached) Color(0xFF6EC522) else Color(0xFF1269FE)
                            )
                        }
                        PlannerProgressBar(
                            progress = progress.progressFraction,
                            accentColor = if (progress.isTargetReached) Color(0xFF6EC522) else Color(0xFF1269FE),
                            trackColor = Color(0xFFE5E7EB)
                        )
                        Text(
                            text = progress.actionText,
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

        PlannerInfoCard(title = "Checklist Aksi") {
            if (goal.actionSteps.isEmpty()) {
                Text(
                    text = "Belum ada langkah aksi spesifik dari planner.",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    goal.actionSteps.forEachIndexed { index, item ->
                        PlannerListItem(
                            index = index + 1,
                            text = sanitizePlannerText(item),
                            accentColor = Color(0xFF8A3FFC)
                        )
                    }
                }
            }
        }

        if (completionState.shouldShow) {
            PlannerInfoCard(title = completionState.title) {
                Text(
                    text = completionState.message,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF4B5563)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    completionState.highlights.forEachIndexed { index, highlight ->
                        PlannerListItem(
                            index = index + 1,
                            text = highlight,
                            accentColor = Color(0xFF6EC522)
                        )
                    }
                }

                PrimaryButton(
                    text = "Tandai Goal Selesai",
                    onClick = {
                        navController.popBackStack()
                        viewModel.completeActivePlannerGoal()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }

        PrimaryButton(
            text = "Perbarui Data Kesehatan",
            onClick = {
                navController.navigate(Route.HealthProfileFromPlannerScreen.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Composable
fun PlannerCoachScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val latestRisk by viewModel.latestPredictionScore
    val allCheckInHistory by viewModel.allPlannerCheckInHistory
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }

    PlannerSectionScaffold(
        title = "Coach",
        onBack = { navController.popBackStack() }
    ) {
        if (goal == null) {
            PlannerEmptyState("Catatan coach belum tersedia karena tidak ada goal aktif.")
            return@PlannerSectionScaffold
        }

        val history = goalId
            ?.let { id -> allCheckInHistory.filter { it.goalId == id }.sortedByDescending { it.createdAtMillis } }
            ?: viewModel.plannerCheckInHistory.value
        val currentWeek = currentMilestoneWeek(goal.createdAtMillis, goal.durationWeeks)
        val milestones = goal.features.mapNotNull { feature ->
            buildWeeklyMilestone(
                feature = feature,
                currentValue = currentFeatureValue(feature.featureName, viewModel),
                currentWeek = currentWeek,
                totalWeeks = goal.durationWeeks,
                heightCm = viewModel.height.value,
                history = history
            )
        }
        val coachNote = buildWeeklyCoachNote(
            goal = goal,
            milestones = milestones,
            history = history,
            latestRisk = latestRisk.takeIf { it > 0.0 }
        )

        PlannerSectionTitle(
            subtitle = "Saran mingguan disusun dari progres faktor, target minggu ini, dan check-in terbaru Anda."
        )

        PlannerInfoCard(title = "Headline") {
            Text(
                text = coachNote.headline,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = colorResource(id = R.color.primary)
            )
            Text(
                text = coachNote.message,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color(0xFF4B5563)
            )
        }

        PlannerInfoCard(title = "Fokus Minggu Ini") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                coachNote.suggestions.forEachIndexed { index, suggestion ->
                    PlannerListItem(
                        index = index + 1,
                        text = suggestion,
                        accentColor = Color(0xFFFBBF24)
                    )
                }
            }
        }

        PlannerInfoCard(title = "Catatan Penting") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = coachNote.disclaimer,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun PlannerCheckInScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
    val allCheckInHistory by viewModel.allPlannerCheckInHistory
    val goal = activeGoal?.takeIf { goalId.isNullOrBlank() || it.id == goalId }

    PlannerSectionScaffold(
        title = "Check-in",
        onBack = { navController.popBackStack() }
    ) {
        if (goal == null) {
            PlannerEmptyState("Check-in belum tersedia karena tidak ada goal aktif.")
            return@PlannerSectionScaffold
        }

        val history = goalId
            ?.let { id -> allCheckInHistory.filter { it.goalId == id }.sortedByDescending { it.createdAtMillis } }
            ?: viewModel.plannerCheckInHistory.value

        PlannerSectionTitle(
            subtitle = "Timeline ini mencatat update yang berkaitan langsung dengan goal aktif Anda"
        )

        PlannerInfoCard(title = "Timeline") {
            if (history.isEmpty()) {
                Text(
                    text = "Belum ada check-in untuk goal ini. Gunakan menu tambah aktivitas atau perbarui data kesehatan untuk mulai mencatat progres",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                PlannerCheckInTimeline(
                    entries = history.take(12)
                )
            }
        }

        PrimaryButton(
            text = "Perbarui Data Kesehatan",
            onClick = { navController.navigate(Route.HealthProfileFromPlannerScreen.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun PlannerChatbotScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    PlannerSectionScaffold(
        title = "Chatbot",
        onBack = { navController.popBackStack() }
    ) {
        PlannerSectionTitle(
            subtitle = "Ruang ini disiapkan untuk percakapan pendamping goal planner secara lebih personal."
        )

        PlannerInfoCard(title = "Segera Hadir") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFD9FBFB))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_planner_message_chatbot),
                        contentDescription = "Chatbot planner",
                        tint = Color(0xFF08B4BD),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Chatbot planner akan ditempatkan di halaman ini.",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Text(
                text = "Untuk sementara, gunakan halaman Action, Milestone, dan Check-in untuk memantau rencana Anda secara manual.",
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun PlannerProgressBar(
    progress: Float,
    accentColor: Color,
    trackColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )
    }
}

@Composable
private fun PlannerTripleValueRow(
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    thirdLabel: String,
    thirdValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlannerValuePill(
            modifier = Modifier.weight(1f),
            label = firstLabel,
            value = firstValue
        )
        PlannerValuePill(
            modifier = Modifier.weight(1f),
            label = secondLabel,
            value = secondValue
        )
        PlannerValuePill(
            modifier = Modifier.weight(1f),
            label = thirdLabel,
            value = thirdValue
        )
    }
}

@Composable
private fun PlannerValuePill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color(0xFF1F2937)
        )
    }
}

@Composable
private fun PlannerCheckInTimeline(
    entries: List<PlannerCheckInEntry>
) {
    val groupedEntries = groupPlannerCheckInTimelineItemsByDay(
        buildPlannerCheckInTimelineItems(entries)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        groupedEntries.forEach { group ->
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = group.headerText,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFFBBBBBB)
                )

                Column {
                    group.items.forEachIndexed { index, item ->
                        PlannerCheckInTimelineItem(
                            item = item
                        )
                        if (index != group.items.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerCheckInTimelineItem(
    item: PlannerCheckInTimelineUiItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(item.iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconResId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = item.label,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF111827)
            )

            Row(
                modifier = Modifier.height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.valueText,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = item.valueColor
                )

                item.deltaChip?.let { chip ->
                    PlannerTimelineDeltaChip(
                        text = chip,
                        backgroundColor = item.deltaBackgroundColor,
                        textColor = item.deltaTextColor
                    )
                }
            }
        }

        Text(
            text = item.timeText,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = Color(0xFFD1D5DB)
        )
    }
}

private data class PlannerCheckInDayGroup(
    val headerText: String,
    val items: List<PlannerCheckInTimelineUiItem>
)

private data class PlannerCheckInTimelineUiItem(
    val id: String,
    val createdAtMillis: Long,
    val label: String,
    val valueText: String,
    val timeText: String,
    val deltaChip: String? = null,
    val iconResId: Int,
    val iconBackgroundColor: Color,
    val valueColor: Color,
    val deltaBackgroundColor: Color = Color(0xFFF3F4F6),
    val deltaTextColor: Color = Color(0xFF5D6A85)
)

private data class PlannerActivityTimelineSummary(
    val weeklyActiveDays: Int,
    val deltaFromPrevious: Int? = null
)

@Composable
private fun PlannerTimelineDeltaChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor
        )
    }
}

private fun buildPlannerCheckInTimelineItems(
    entries: List<PlannerCheckInEntry>
): List<PlannerCheckInTimelineUiItem> {
    val previousByTypeMap = buildPreviousCheckInMapByType(entries)
    val activitySummaryMap = buildActivityTimelineSummaryMap(entries)
    return entries.map { entry ->
        entry.toTimelineUi(
            previousEntry = previousByTypeMap[entry.id],
            activitySummary = activitySummaryMap[entry.id]
        )
    }
}

private fun buildPreviousCheckInMapByType(
    entries: List<PlannerCheckInEntry>
): Map<String, PlannerCheckInEntry?> {
    val previousById = mutableMapOf<String, PlannerCheckInEntry?>()
    val previousEntryByType = mutableMapOf<String, PlannerCheckInEntry?>()

    entries
        .sortedBy { it.createdAtMillis }
        .forEach { currentEntry ->
            previousById[currentEntry.id] = previousEntryByType[currentEntry.type]
            previousEntryByType[currentEntry.type] = currentEntry
        }

    return previousById
}

private fun groupPlannerCheckInTimelineItemsByDay(
    items: List<PlannerCheckInTimelineUiItem>
): List<PlannerCheckInDayGroup> {
    return items
        .groupBy { plannerCheckInDayKey(it.createdAtMillis) }
        .toList()
        .sortedByDescending { it.first }
        .map { (dayKey, items) ->
            PlannerCheckInDayGroup(
                headerText = plannerCheckInDayHeader(dayKey),
                items = items.sortedByDescending { it.createdAtMillis }
            )
        }
}

private fun PlannerCheckInEntry.toTimelineUi(
    previousEntry: PlannerCheckInEntry? = null,
    activitySummary: PlannerActivityTimelineSummary? = null
): PlannerCheckInTimelineUiItem {
    val timeText = plannerCheckInTime(createdAtMillis)
    return when (type) {
        "weight" -> toWeightTimelineUi(previousEntry, timeText)
        "hypertension" -> toStatusTimelineUi(
            previousEntry = previousEntry,
            timeText = timeText,
            label = "Hipertensi",
            iconResId = R.drawable.ic_hypertension,
            positiveColor = Color(0xFF0F766E),
            positiveBackground = Color(0xFFE8F7F2),
            negativeColor = Color(0xFFDC2626),
            negativeBackground = Color(0xFFFFE8E8)
        )
        "cholesterol" -> toStatusTimelineUi(
            previousEntry = previousEntry,
            timeText = timeText,
            label = "Kolesterol",
            iconResId = R.drawable.ic_cholesterol,
            positiveColor = Color(0xFF0F766E),
            positiveBackground = Color(0xFFE8F7F2),
            negativeColor = Color(0xFFDC2626),
            negativeBackground = Color(0xFFFFE8E8)
        )
        "smoking" -> toSmokingTimelineUi(
            previousEntry = previousEntry,
            timeText = timeText
        )
        "activity" -> toActivityTimelineUi(
            activitySummary = activitySummary,
            timeText = timeText
        )
        else -> PlannerCheckInTimelineUiItem(
            id = id,
            createdAtMillis = createdAtMillis,
            label = label.ifBlank { "Check-in" },
            valueText = valueText,
            timeText = timeText,
            iconResId = R.drawable.ic_weight,
            iconBackgroundColor = Color(0xFFF2F5F9),
            valueColor = Color(0xFF334155)
        )
    }
}

private fun PlannerCheckInEntry.toWeightTimelineUi(
    previousWeightEntry: PlannerCheckInEntry?,
    timeText: String
): PlannerCheckInTimelineUiItem {
    val currentWeight = parseWeightKg(valueText)
    val previousWeight = previousWeightEntry?.valueText?.let(::parseWeightKg)

    val delta = if (currentWeight != null && previousWeight != null) {
        currentWeight - previousWeight
    } else {
        null
    }
    val isStable = delta?.let { it == 0f } == true
    val isDecreasing = delta?.let { it < 0f } == true
    val deltaText = when {
        delta == null || isStable -> null
        isDecreasing -> "↓ ${formatWeightDelta(abs(delta))} kg"
        else -> "↑ ${formatWeightDelta(abs(delta))} kg"
    }
    val positiveColor = Color(0xFF0F766E)
    val positiveBackground = Color(0xFFE8F7F2)
    val negativeColor = Color(0xFFDC2626)
    val negativeBackground = Color(0xFFFFE8E8)
    val stableColor = Color(0xFFCA8A04)
    val stableBackground = Color(0xFFFEF3C7)

    return PlannerCheckInTimelineUiItem(
        id = id,
        createdAtMillis = createdAtMillis,
        label = "Berat badan",
        valueText = valueText,
        timeText = timeText,
        deltaChip = deltaText,
        iconResId = R.drawable.ic_weight,
        iconBackgroundColor = when {
            isStable -> stableBackground
            isDecreasing -> Color(0xFFEAF8EF)
            else -> Color(0xFFFFF1F1)
        },
        valueColor = when {
            isStable -> stableColor
            isDecreasing -> positiveColor
            else -> negativeColor
        },
        deltaBackgroundColor = when {
            isStable -> stableBackground
            isDecreasing -> positiveBackground
            else -> negativeBackground
        },
        deltaTextColor = when {
            isStable -> stableColor
            isDecreasing -> positiveColor
            else -> negativeColor
        }
    )
}

private fun PlannerCheckInEntry.toActivityTimelineUi(
    activitySummary: PlannerActivityTimelineSummary?,
    timeText: String
): PlannerCheckInTimelineUiItem {
    val weeklyActiveDays = activitySummary?.weeklyActiveDays
        ?: if (parseActivityChecked(valueText) == true) 1 else 0
    val delta = activitySummary?.deltaFromPrevious
    val hasImproved = delta != null && delta > 0
    val positiveColor = Color(0xFF0F766E)
    val positiveBackground = Color(0xFFE8F7F2)
    val neutralColor = Color(0xFF5D6A85)
    val neutralBackground = Color(0xFFF2F5F9)

    return PlannerCheckInTimelineUiItem(
        id = id,
        createdAtMillis = createdAtMillis,
        label = "Aktivitas fisik",
        valueText = "$weeklyActiveDays hari minggu ini",
        timeText = timeText,
        deltaChip = if (hasImproved) "+$delta hari" else null,
        iconResId = R.drawable.ic_walk,
        iconBackgroundColor = if (hasImproved) positiveBackground else neutralBackground,
        valueColor = if (hasImproved) positiveColor else neutralColor,
        deltaBackgroundColor = positiveBackground,
        deltaTextColor = positiveColor
    )
}

private fun PlannerCheckInEntry.toStatusTimelineUi(
    previousEntry: PlannerCheckInEntry?,
    timeText: String,
    label: String,
    iconResId: Int,
    positiveColor: Color,
    positiveBackground: Color,
    negativeColor: Color,
    negativeBackground: Color
): PlannerCheckInTimelineUiItem {
    val isControlled = parseControlledStatus(valueText)
    val previousControlled = previousEntry?.valueText?.let(::parseControlledStatus)
    val deltaChip = when {
        isControlled == null || previousControlled == null -> null
        isControlled == previousControlled -> null
        isControlled -> "Membaik"
        else -> "Memburuk"
    }
    val isImproving = deltaChip == "Membaik"

    return PlannerCheckInTimelineUiItem(
        id = id,
        createdAtMillis = createdAtMillis,
        label = label,
        valueText = if (isControlled == true) "Terkontrol" else "Belum terkontrol",
        timeText = timeText,
        deltaChip = deltaChip,
        iconResId = iconResId,
        iconBackgroundColor = if (isControlled == true) positiveBackground else negativeBackground,
        valueColor = if (isControlled == true) positiveColor else negativeColor,
        deltaBackgroundColor = if (isImproving) positiveBackground else negativeBackground,
        deltaTextColor = if (isImproving) positiveColor else negativeColor
    )
}

private fun PlannerCheckInEntry.toSmokingTimelineUi(
    previousEntry: PlannerCheckInEntry?,
    timeText: String
): PlannerCheckInTimelineUiItem {
    val isStopped = parseSmokingStoppedStatus(valueText)
    val previousStopped = previousEntry?.valueText?.let(::parseSmokingStoppedStatus)
    val deltaChip = when {
        isStopped == null || previousStopped == null -> null
        isStopped == previousStopped -> null
        isStopped -> "Membaik"
        else -> "Memburuk"
    }
    val isImproving = deltaChip == "Membaik"
    val positiveColor = Color(0xFF0F766E)
    val positiveBackground = Color(0xFFE8F7F2)
    val negativeColor = Color(0xFFDC2626)
    val negativeBackground = Color(0xFFFFE8E8)

    return PlannerCheckInTimelineUiItem(
        id = id,
        createdAtMillis = createdAtMillis,
        label = "Status merokok",
        valueText = if (isStopped == true) "Sudah Berhenti" else "Masih aktif",
        timeText = timeText,
        deltaChip = deltaChip,
        iconResId = R.drawable.ic_smoking,
        iconBackgroundColor = if (isStopped == true) positiveBackground else negativeBackground,
        valueColor = if (isStopped == true) positiveColor else negativeColor,
        deltaBackgroundColor = if (isImproving) positiveBackground else negativeBackground,
        deltaTextColor = if (isImproving) positiveColor else negativeColor
    )
}

private fun buildActivityTimelineSummaryMap(
    entries: List<PlannerCheckInEntry>
): Map<String, PlannerActivityTimelineSummary> {
    val activityEntries = entries
        .filter { it.type == "activity" }
        .sortedBy { it.createdAtMillis }

    if (activityEntries.isEmpty()) {
        return emptyMap()
    }

    val summaryById = mutableMapOf<String, PlannerActivityTimelineSummary>()
    var previousWeeklyCount: Int? = null
    val rollingWindowMillis = 6L * 24L * 60L * 60L * 1000L

    activityEntries.forEach { currentEntry ->
        val windowStart = currentEntry.createdAtMillis - rollingWindowMillis
        val weeklyCount = activityEntries.count { candidate ->
            candidate.createdAtMillis in windowStart..currentEntry.createdAtMillis &&
                parseActivityChecked(candidate.valueText) == true
        }
        summaryById[currentEntry.id] = PlannerActivityTimelineSummary(
            weeklyActiveDays = weeklyCount,
            deltaFromPrevious = previousWeeklyCount?.let { weeklyCount - it }
        )
        previousWeeklyCount = weeklyCount
    }

    return summaryById
}

private fun parseWeightKg(text: String): Float? {
    return Regex("""(\d+(?:[.,]\d+)?)""")
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.replace(',', '.')
        ?.toFloatOrNull()
}

private fun parseControlledStatus(text: String): Boolean? {
    return when (text.trim().lowercase(Locale("id", "ID"))) {
        "tidak", "no", "false", "terkontrol" -> true
        "ya", "yes", "true", "belum terkontrol" -> false
        else -> null
    }
}

private fun parseActivityChecked(text: String): Boolean? {
    return when (text.trim().lowercase(Locale("id", "ID"))) {
        "aktif hari ini", "aktif", "ya", "true" -> true
        "tidak aktif hari ini", "tidak aktif", "tidak", "false" -> false
        else -> null
    }
}

private fun parseSmokingStoppedStatus(text: String): Boolean? {
    return when (text.trim().lowercase(Locale("id", "ID"))) {
        "sudah berhenti", "berhenti merokok", "former smoker" -> true
        "masih aktif", "masih merokok", "active smoker" -> false
        else -> null
    }
}

private fun formatWeightDelta(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun plannerCheckInDayKey(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")).format(Date(timestamp))
}

private fun plannerCheckInDayHeader(dayKey: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
    val date = formatter.parse(dayKey) ?: return dayKey
    val targetCalendar = Calendar.getInstance().apply { time = date }
    val todayCalendar = Calendar.getInstance()
    val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dateText = SimpleDateFormat("dd MMM", Locale("id", "ID")).format(date)

    return when {
        targetCalendar.isSameDay(todayCalendar) -> "Hari ini · $dateText"
        targetCalendar.isSameDay(yesterdayCalendar) -> "Kemarin · $dateText"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(date)
    }
}

private fun plannerCheckInTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date(timestamp))
}

private fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}
