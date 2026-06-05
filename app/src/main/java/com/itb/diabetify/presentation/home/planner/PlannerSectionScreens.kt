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
@Composable
fun PlannerMilestoneScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    goalId: String? = null
) {
    val activeGoal by viewModel.activePlannerGoal
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
        val milestones = goal.features.mapNotNull { feature ->
            buildWeeklyMilestone(
                feature = feature,
                currentValue = currentFeatureValue(feature.featureName, viewModel),
                currentWeek = currentWeek,
                totalWeeks = goal.durationWeeks,
                heightCm = viewModel.height.value
            )
        }

        PlannerSectionTitle(
            title = "Milestone Mingguan",
            subtitle = "Target minggu ini dihitung bertahap dari baseline menuju target akhir planner."
        )

        PlannerInfoCard(title = "Milestone") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDF5FF))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Minggu $currentWeek dari ${goal.durationWeeks}",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.primary)
                    )
                    Text(
                        text = "Fokuskan pencapaian minggu ini sebelum masuk ke target berikutnya.",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF4B5563)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF1269FE)
                )
            }
        }

        milestones.forEach { milestone ->
            PlannerInfoCard(title = milestone.label) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = milestone.label,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = milestone.statusText,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = milestone.statusColor
                    )
                }

                PlannerProgressBar(
                    progress = milestone.progressFraction,
                    accentColor = milestone.statusColor,
                    trackColor = milestone.statusColor.copy(alpha = 0.15f)
                )

                PlannerTripleValueRow(
                    firstLabel = "Saat ini",
                    firstValue = milestone.currentText,
                    secondLabel = "Minggu ini",
                    secondValue = milestone.expectedText,
                    thirdLabel = "Target",
                    thirdValue = milestone.finalTargetText
                )
            }
        }
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
            title = "Langkah Aksi",
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
                navController.navigate(Route.HealthProfileScreen.route)
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
                heightCm = viewModel.height.value
            )
        }
        val coachNote = buildWeeklyCoachNote(
            goal = goal,
            milestones = milestones,
            history = history,
            latestRisk = latestRisk.takeIf { it > 0.0 }
        )

        PlannerSectionTitle(
            title = "Catatan Coach",
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
            title = "Riwayat Check-in",
            subtitle = "Timeline ini mencatat update yang berkaitan langsung dengan goal aktif Anda."
        )

        PlannerInfoCard(title = "Timeline") {
            if (history.isEmpty()) {
                Text(
                    text = "Belum ada check-in untuk goal ini. Gunakan menu tambah aktivitas atau perbarui data kesehatan untuk mulai mencatat progres.",
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    history.take(12).forEachIndexed { index, entry ->
                        PlannerCheckInTimelineItem(entry = entry)
                        if (index != history.take(12).lastIndex) {
                            HorizontalDivider(color = Color(0xFFE5E7EB))
                        }
                    }
                }
            }
        }

        PrimaryButton(
            text = "Perbarui Data Kesehatan",
            onClick = { navController.navigate(Route.HealthProfileScreen.route) },
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
            title = "Chatbot Planner",
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
private fun PlannerCheckInTimelineItem(
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
                .background(Color(0xFFF2F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF5D6A85),
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimelineDate(entry.createdAtMillis),
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    color = Color(0xFF6B7280)
                )
            }

            Text(
                text = entry.valueText,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1269FE)
            )

            Text(
                text = entry.note,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}
