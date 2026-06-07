package com.itb.diabetify.presentation.home.planner

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalFeature
import com.itb.diabetify.presentation.home.HomeViewModel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

internal data class PlannerFeatureProgress(
    val featureName: String,
    val label: String,
    val baselineText: String,
    val currentText: String,
    val targetText: String,
    val actionText: String,
    val progressFraction: Float,
    val statusText: String,
    val isTargetReached: Boolean,
    val hasRelevantUpdate: Boolean = false
)

internal data class PlannerWeeklyMilestone(
    val featureName: String,
    val label: String,
    val baselineText: String,
    val currentText: String,
    val expectedText: String,
    val finalTargetText: String,
    val baselineValue: Double?,
    val currentValue: Double?,
    val expectedValue: Double?,
    val finalTargetValue: Double?,
    val progressFraction: Float,
    val statusText: String,
    val statusColor: Color,
    val status: MilestoneStatus
)

internal enum class MilestoneStatus {
    ACHIEVED,
    ON_TRACK,
    BEHIND,
    MONITOR
}

internal data class WeeklyCoachNote(
    val headline: String,
    val message: String,
    val suggestions: List<String>,
    val disclaimer: String = "Catatan ini bersifat pendamping perilaku dan bukan pengganti konsultasi tenaga kesehatan."
)

internal data class PlannerFeaturePalette(
    val containerColor: Color,
    val accentColor: Color
)

internal data class PlannerFeatureUiModel(
    val progress: PlannerFeatureProgress,
    @DrawableRes val iconResId: Int,
    val palette: PlannerFeaturePalette,
    val trailingText: String,
    val currentHeadline: String,
    val startLabel: String,
    val endLabel: String
)

internal data class PlannerShortcut(
    val label: String,
    @DrawableRes val iconResId: Int,
    val palette: PlannerFeaturePalette
)

internal enum class PlannerMilestoneCardType {
    NUMERIC,
    CATEGORICAL
}

internal enum class PlannerMilestoneHighlightTone {
    SUCCESS,
    WARNING
}

internal data class PlannerMilestoneHighlight(
    @DrawableRes val iconResId: Int,
    val message: String,
    val containerColor: Color,
    val textColor: Color,
    val iconColor: Color
)

internal data class PlannerMilestoneCardUiModel(
    val featureName: String,
    val title: String,
    val type: PlannerMilestoneCardType,
    @DrawableRes val iconResId: Int,
    val iconContainerColor: Color,
    val iconTint: Color,
    val statusText: String,
    val statusContainerColor: Color,
    val statusTextColor: Color,
    val progressFraction: Float,
    val progressColor: Color,
    val progressTrackColor: Color,
    val baselineCaption: String,
    val trailingCaption: String,
    val currentLabel: String,
    val currentValueText: String,
    val currentValueContainerColor: Color,
    val currentValueColor: Color,
    val weeklyLabel: String,
    val weeklyValueText: String,
    val targetLabel: String,
    val targetValueText: String,
    val transitionFromText: String? = null,
    val transitionToText: String? = null,
    val transitionFromColor: Color = Color(0xFF4B5563),
    val transitionToColor: Color = Color(0xFF111827),
    val highlight: PlannerMilestoneHighlight? = null
)

internal fun plannerShortcuts(): List<PlannerShortcut> {
    return listOf(
        PlannerShortcut(
            label = "Milestone",
            iconResId = R.drawable.ic_planner_flag,
            palette = PlannerFeaturePalette(
                containerColor = Color(0xFFDCFCDE),
                accentColor = Color(0xFF6EC522)
            )
        ),
        PlannerShortcut(
            label = "Coach",
            iconResId = R.drawable.ic_planner_school,
            palette = PlannerFeaturePalette(
                containerColor = Color(0xFFFFFAE5),
                accentColor = Color(0xFFFBBF24)
            )
        ),
        PlannerShortcut(
            label = "Chatbot",
            iconResId = R.drawable.ic_planner_message_chatbot,
            palette = PlannerFeaturePalette(
                containerColor = Color(0xFFD9FBFB),
                accentColor = Color(0xFF08B4BD)
            )
        ),
        PlannerShortcut(
            label = "Check-in",
            iconResId = R.drawable.ic_planner_calendar_check,
            palette = PlannerFeaturePalette(
                containerColor = Color(0xFFF2F5F9),
                accentColor = Color(0xFF5D6A85)
            )
        )
    )
}

internal fun overallGoalProgress(
    goal: PlannerGoal,
    latestRisk: Double?
): Float {
    val baselineRisk = goal.currentRiskPercentage ?: return 0f
    val projectedRisk = goal.projectedRiskPercentage ?: goal.targetRiskPercentage.toDouble()
    val currentRisk = latestRisk ?: baselineRisk
    val totalReduction = baselineRisk - projectedRisk
    if (totalReduction <= 0.0) {
        return if (currentRisk <= projectedRisk) 1f else 0f
    }

    val currentReduction = baselineRisk - currentRisk
    return (currentReduction / totalReduction).toFloat().coerceIn(0f, 1f)
}

internal fun buildPlannerFeatureUiModels(
    goal: PlannerGoal,
    viewModel: HomeViewModel,
    history: List<PlannerCheckInEntry> = emptyList()
): List<PlannerFeatureUiModel> {
    return goal.features.map { feature ->
        val progress = buildFeatureProgress(
            feature = feature,
            currentValue = currentFeatureValue(feature.featureName, viewModel),
            heightCm = viewModel.height.value,
            history = history
        )
        PlannerFeatureUiModel(
            progress = progress,
            iconResId = plannerFeatureIcon(feature.featureName),
            palette = plannerFeaturePalette(feature.featureName),
            trailingText = plannerFeatureTrailingText(progress),
            currentHeadline = plannerCurrentHeadline(progress),
            startLabel = progress.baselineText,
            endLabel = progress.targetText
        )
    }
}

internal fun buildMilestoneCardUiModels(
    milestones: List<PlannerWeeklyMilestone>
): List<PlannerMilestoneCardUiModel> {
    return milestones.map { milestone ->
        val palette = plannerFeaturePalette(milestone.featureName)
        val badgeColors = milestoneBadgeColors(
            featureName = milestone.featureName,
            status = milestone.status,
            statusText = milestone.statusText
        )
        val progressColor = Color(0xFFB6E8C7)
        val progressTrackColor = Color(0xFFF1F1F1)

        PlannerMilestoneCardUiModel(
            featureName = milestone.featureName,
            title = milestone.label,
            type = if (isNumericMilestoneFeature(milestone.featureName)) {
                PlannerMilestoneCardType.NUMERIC
            } else {
                PlannerMilestoneCardType.CATEGORICAL
            },
            iconResId = plannerFeatureIcon(milestone.featureName),
            iconContainerColor = palette.containerColor,
            iconTint = palette.accentColor,
            statusText = milestone.statusText,
            statusContainerColor = badgeColors.containerColor,
            statusTextColor = badgeColors.textColor,
            progressFraction = milestone.progressFraction,
            progressColor = progressColor,
            progressTrackColor = progressTrackColor,
            baselineCaption = "Baseline ${milestone.baselineText}",
            trailingCaption = milestone.finalTargetText,
            currentLabel = "Saat ini",
            currentValueText = milestone.currentText,
            currentValueContainerColor = numericCurrentMetricContainerColor(milestone),
            currentValueColor = numericCurrentMetricValueColor(milestone),
            weeklyLabel = "Target Minggu Ini",
            weeklyValueText = milestone.expectedText,
            targetLabel = "Target Akhir",
            targetValueText = milestone.finalTargetText,
            transitionFromText = if (isNumericMilestoneFeature(milestone.featureName)) {
                null
            } else {
                categoricalMilestoneLeftText(milestone)
            },
            transitionToText = if (isNumericMilestoneFeature(milestone.featureName)) {
                null
            } else {
                categoricalMilestoneRightText(milestone)
            },
            transitionFromColor = Color(0xFF888888),
            transitionToColor = categoricalMilestoneRightColor(milestone),
            highlight = buildMilestoneHighlight(milestone)
        )
    }
}

internal fun currentMilestoneWeek(createdAtMillis: Long, totalWeeks: Int): Int {
    val elapsedMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)
    val elapsedWeeks = ceil(elapsedMillis.toDouble() / WEEK_MILLIS).toInt().coerceAtLeast(1)
    return elapsedWeeks.coerceIn(1, totalWeeks.coerceAtLeast(1))
}

internal fun buildWeeklyMilestone(
    feature: PlannerGoalFeature,
    currentValue: Double?,
    currentWeek: Int,
    totalWeeks: Int,
    heightCm: Int,
    history: List<PlannerCheckInEntry> = emptyList()
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
        val latestCheckIn = latestRelevantPlannerCheckIn(feature.featureName, history)
        val hasUpdate = latestCheckIn != null
        return PlannerWeeklyMilestone(
            featureName = feature.featureName,
            label = displayFeatureLabel(feature),
            baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
            currentText = currentText,
            expectedText = categoricalMilestoneFocusText(
                featureName = feature.featureName,
                isTargetReached = reached
            ),
            finalTargetText = finalTargetText,
            baselineValue = baseline,
            currentValue = currentValue,
            expectedValue = null,
            finalTargetValue = target,
            progressFraction = when {
                reached -> 1f
                hasUpdate -> 0.5f
                else -> 0f
            },
            statusText = when {
                reached -> "Tercapai"
                hasUpdate -> "Pantau"
                else -> "Belum diupdate"
            },
            statusColor = when {
                reached -> Color(0xFF059669)
                hasUpdate -> Color(0xFF2563EB)
                else -> Color(0xFF9CA3AF)
            },
            status = if (reached) MilestoneStatus.ACHIEVED else MilestoneStatus.MONITOR
        )
    }

    val expectedFraction = currentWeek.toDouble() / totalWeeks.coerceAtLeast(1)
    val expectedValue = baseline + ((target - baseline) * expectedFraction)
    val expectedText = formatFeatureValue(feature.featureName, expectedValue, heightCm)
    val progressFraction = calculateProgressFraction(
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val latestCheckIn = latestRelevantPlannerCheckIn(feature.featureName, history)
    val hasUpdate = latestCheckIn != null
    val reached = hasReachedDisplayedNumericTarget(
        featureName = feature.featureName,
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = currentText,
        targetText = finalTargetText
    ) || isTargetReached(
        featureName = feature.featureName,
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val onTrack = reached || hasReachedDisplayedNumericTarget(
        featureName = feature.featureName,
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = currentText,
        targetText = expectedText
    ) || progressFraction + MILESTONE_TOLERANCE >= expectedFraction.toFloat()
    val resolvedProgressFraction = displayedNumericProgressFraction(
        featureName = feature.featureName,
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = currentText,
        targetText = finalTargetText
    ) ?: progressFraction
    val statusText = when {
        !hasUpdate -> "Belum diupdate"
        reached -> "Tercapai"
        onTrack -> "On track"
        else -> "Tertinggal"
    }
    val statusColor = when {
        !hasUpdate -> Color(0xFF888888)
        reached -> Color(0xFF059669)
        onTrack -> Color(0xFF2563EB)
        else -> Color(0xFFEA580C)
    }
    val status = when {
        !hasUpdate -> MilestoneStatus.MONITOR
        reached -> MilestoneStatus.ACHIEVED
        onTrack -> MilestoneStatus.ON_TRACK
        else -> MilestoneStatus.BEHIND
    }

    return PlannerWeeklyMilestone(
        featureName = feature.featureName,
        label = displayFeatureLabel(feature),
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = currentText,
        expectedText = expectedText,
        finalTargetText = finalTargetText,
        baselineValue = baseline,
        currentValue = currentValue,
        expectedValue = expectedValue,
        finalTargetValue = target,
        progressFraction = when {
            !hasUpdate -> 0f
            reached -> 1f
            else -> resolvedProgressFraction
        },
        statusText = statusText,
        statusColor = statusColor,
        status = status
    )
}

internal fun buildWeeklyCoachNote(
    goal: PlannerGoal,
    milestones: List<PlannerWeeklyMilestone>,
    history: List<PlannerCheckInEntry>,
    latestRisk: Double?
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

internal fun buildFeatureProgress(
    feature: PlannerGoalFeature,
    currentValue: Double?,
    heightCm: Int,
    history: List<PlannerCheckInEntry> = emptyList()
): PlannerFeatureProgress {
    val baseline = feature.baselineValue
    val target = feature.targetValue
    val hasRelevantUpdate = latestRelevantPlannerCheckIn(feature.featureName, history) != null
    val progressFraction = if (isCategoricalFeature(feature.featureName)) {
        0f
    } else {
        calculateProgressFraction(
            baseline = baseline,
            target = target,
            current = currentValue
        )
    }
    val isReached = isTargetReached(
        featureName = feature.featureName,
        baseline = baseline,
        target = target,
        current = currentValue
    )
    val progressPercentage = (progressFraction * 100).roundToInt()
    val resolvedProgressFraction = if (isCategoricalFeature(feature.featureName)) {
        when {
            isReached -> 1f
            hasRelevantUpdate -> 0.5f
            else -> 0f
        }
    } else if (isReached) {
        1f
    } else {
        progressFraction
    }

    return PlannerFeatureProgress(
        featureName = feature.featureName,
        label = displayFeatureLabel(feature),
        baselineText = formatFeatureValue(feature.featureName, baseline, heightCm),
        currentText = formatFeatureValue(feature.featureName, currentValue, heightCm),
        targetText = formatFeatureValue(feature.featureName, target, heightCm),
        actionText = displayFeatureActionText(feature, heightCm),
        progressFraction = resolvedProgressFraction,
        statusText = when {
            isReached -> "Target tercapai"
            isCategoricalFeature(feature.featureName) && hasRelevantUpdate -> "Perubahan sedang dipantau"
            isCategoricalFeature(feature.featureName) -> "Belum ada update"
            progressPercentage <= 0 -> "Belum ada perubahan menuju target"
            else -> "$progressPercentage% menuju target"
        },
        isTargetReached = isReached,
        hasRelevantUpdate = hasRelevantUpdate
    )
}

internal fun currentFeatureValue(
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

internal fun formatRisk(value: Double?): String {
    return value?.let { String.format("%.1f%%", it) } ?: "-"
}

internal fun formatTimelineDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID"))
    return formatter.format(java.util.Date(timestamp))
}

internal fun sanitizePlannerText(text: String): String {
    return text
        .replace(Regex("\\bBMI\\b", RegexOption.IGNORE_CASE), "berat badan")
        .replace(Regex("\\bIMT\\b", RegexOption.IGNORE_CASE), "berat badan")
}

private fun plannerFeatureTrailingText(progress: PlannerFeatureProgress): String {
    return when {
        progress.featureName in setOf("is_hypertension", "is_cholesterol", "smoking_status") && !progress.isTargetReached && progress.hasRelevantUpdate -> "Proses"
        progress.featureName in setOf("is_hypertension", "is_cholesterol", "smoking_status") && !progress.isTargetReached -> "Belum"
        progress.isTargetReached -> "100%"
        else -> "${(progress.progressFraction * 100).roundToInt()}%"
    }
}

private fun plannerCurrentHeadline(progress: PlannerFeatureProgress): String {
    return "Saat ini : ${progress.currentText}"
}

private fun plannerFeaturePalette(featureName: String): PlannerFeaturePalette {
    return when (featureName) {
        "BMI" -> PlannerFeaturePalette(Color(0xFFEDF5FF), Color(0xFF1269FE))
        "is_hypertension" -> PlannerFeaturePalette(Color(0xFFFFF1F3), Color(0xFFFA4D5E))
        "is_cholesterol" -> PlannerFeaturePalette(Color(0xFFDCFCDE), Color(0xFF6EC522))
        "smoking_status" -> PlannerFeaturePalette(Color(0xFFFFF1E3), Color(0xFFFB9D3E))
        "moderate_physical_activity_frequency" -> PlannerFeaturePalette(Color(0xFFF2F5F9), Color(0xFF5D6A85))
        else -> PlannerFeaturePalette(Color(0xFFF2F5F9), Color(0xFF5D6A85))
    }
}

private data class MilestoneBadgeColors(
    val containerColor: Color,
    val textColor: Color
)

private fun milestoneBadgeColors(
    featureName: String,
    status: MilestoneStatus,
    statusText: String
): MilestoneBadgeColors {
    val isNumeric = isNumericMilestoneFeature(featureName)
    return if (isNumeric) {
        when (statusText) {
            "Tercapai" -> MilestoneBadgeColors(
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF085041)
            )
            "On track" -> MilestoneBadgeColors(
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF0F6E56)
            )
            "Tertinggal" -> MilestoneBadgeColors(
                containerColor = Color(0xFFFAEEDA),
                textColor = Color(0xFF633806)
            )
            else -> MilestoneBadgeColors(
                containerColor = Color(0xFFF0F0F0),
                textColor = Color(0xFF888888)
            )
        }
    } else {
        when {
            status == MilestoneStatus.ACHIEVED -> MilestoneBadgeColors(
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF085041)
            )
            statusText == "Pantau" -> MilestoneBadgeColors(
                containerColor = Color(0xFFFAEEDA),
                textColor = Color(0xFF633806)
            )
            else -> MilestoneBadgeColors(
                containerColor = Color(0xFFF0F0F0),
                textColor = Color(0xFF888888)
            )
        }
    }
}

@DrawableRes
private fun plannerFeatureIcon(featureName: String): Int {
    return when (featureName) {
        "BMI" -> R.drawable.ic_weight
        "is_hypertension" -> R.drawable.ic_hypertension
        "is_cholesterol" -> R.drawable.ic_cholesterol
        "smoking_status" -> R.drawable.ic_smoking
        "moderate_physical_activity_frequency" -> R.drawable.ic_walk
        else -> R.drawable.ic_goal
    }
}

private fun isNumericMilestoneFeature(featureName: String): Boolean {
    return featureName in setOf("BMI", "moderate_physical_activity_frequency")
}

private fun numericCurrentMetricContainerColor(
    milestone: PlannerWeeklyMilestone
): Color {
    if (!isNumericMilestoneFeature(milestone.featureName)) {
        return Color(0xFFFFEFF1)
    }

    return when {
        milestone.statusText == "Belum diupdate" -> Color(0xFFF6F6F6)
        milestone.statusText == "Tertinggal" -> Color(0xFFFAEEDA)
        meetsNumericWeeklyTarget(milestone) -> Color(0xFFE1F5EE)
        else -> Color(0xFFFFEFF1)
    }
}

private fun numericCurrentMetricValueColor(
    milestone: PlannerWeeklyMilestone
): Color {
    if (!isNumericMilestoneFeature(milestone.featureName)) {
        return Color(0xFFF24E5A)
    }

    return when {
        milestone.statusText == "Belum diupdate" -> Color(0xFF111827)
        milestone.statusText == "Tertinggal" -> Color(0xFF633806)
        meetsNumericWeeklyTarget(milestone) -> Color(0xFF0F6E56)
        else -> Color(0xFFF24E5A)
    }
}

private fun meetsNumericWeeklyTarget(
    milestone: PlannerWeeklyMilestone
): Boolean {
    return hasReachedDisplayedNumericTarget(
        featureName = milestone.featureName,
        baselineText = milestone.baselineText,
        currentText = milestone.currentText,
        targetText = milestone.expectedText
    )
}

private fun categoricalMilestoneLeftText(
    milestone: PlannerWeeklyMilestone
): String {
    return when (milestone.featureName) {
        "smoking_status" -> if (milestone.baselineText.equals("Masih aktif", ignoreCase = true)) {
            "Masih aktif merokok"
        } else {
            milestone.baselineText
        }
        else -> milestone.baselineText
    }
}

private fun categoricalMilestoneRightText(
    milestone: PlannerWeeklyMilestone
): String? {
    return when (milestone.statusText) {
        "Belum diupdate" -> null
        "Pantau" -> "Dalam penanganan"
        "Tercapai" -> when (milestone.featureName) {
            "smoking_status" -> "Berhenti merokok"
            else -> "Terkontrol"
        }
        else -> null
    }
}

private fun categoricalMilestoneRightColor(
    milestone: PlannerWeeklyMilestone
): Color {
    return when (milestone.statusText) {
        "Pantau" -> Color(0xFF633806)
        "Tercapai" -> Color(0xFF085041)
        else -> Color(0xFF888888)
    }
}

private fun buildMilestoneHighlight(
    milestone: PlannerWeeklyMilestone
): PlannerMilestoneHighlight? {
    return when (milestone.featureName) {
        "BMI" -> buildNumericMilestoneHighlight(
            milestone = milestone,
            exactSuccessMessage = "Kamu sudah mencapai target minggu ini!",
            overSuccessMessage = "Kamu sudah melampaui target minggu ini!",
            finalSuccessMessage = "Selamat target Berat Badan tercapai! Pertahankan!",
            warningMessage = { difference ->
                "Turunkan sekitar ${String.format("%.0f", difference)} kg untuk mengejar target minggu ini!"
            }
        )
        "moderate_physical_activity_frequency" -> buildNumericMilestoneHighlight(
            milestone = milestone,
            exactSuccessMessage = "Aktivitasmu sudah mencapai target minggu ini!",
            overSuccessMessage = "Aktivitasmu sudah melampaui target minggu ini!",
            finalSuccessMessage = "Selamat target Aktivitas Fisik tercapai! Pertahankan!",
            warningMessage = { difference ->
                "Tambahkan ${difference.toInt()} hari aktivitas untuk memenuhi target!"
            }
        )
        "is_hypertension" -> if (milestone.status == MilestoneStatus.ACHIEVED) {
            PlannerMilestoneHighlight(
                iconResId = R.drawable.ic_confetti,
                message = "Selamat target Hipertensi tercapai! Pertahankan!",
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF085041),
                iconColor = Color(0xFF0F6E56)
            )
        } else {
            null
        }
        "is_cholesterol" -> if (milestone.status == MilestoneStatus.ACHIEVED) {
            PlannerMilestoneHighlight(
                iconResId = R.drawable.ic_confetti,
                message = "Selamat target Kolesterol tercapai! Pertahankan!",
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF085041),
                iconColor = Color(0xFF0F6E56)
            )
        } else {
            null
        }
        "smoking_status" -> if (milestone.status == MilestoneStatus.ACHIEVED) {
            PlannerMilestoneHighlight(
                iconResId = R.drawable.ic_confetti,
                message = "Selamat, kamu sudah mencapai target berhenti merokok!",
                containerColor = Color(0xFFE1F5EE),
                textColor = Color(0xFF085041),
                iconColor = Color(0xFF0F6E56)
            )
        } else {
            null
        }
        else -> null
    }
}

private fun buildNumericMilestoneHighlight(
    milestone: PlannerWeeklyMilestone,
    exactSuccessMessage: String,
    overSuccessMessage: String,
    finalSuccessMessage: String,
    warningMessage: (Double) -> String
): PlannerMilestoneHighlight? {
    val weeklyTargetStatus = displayedNumericTargetStatus(
        featureName = milestone.featureName,
        baselineText = milestone.baselineText,
        currentText = milestone.currentText,
        targetText = milestone.expectedText
    )
    val finalTargetStatus = displayedNumericTargetStatus(
        featureName = milestone.featureName,
        baselineText = milestone.baselineText,
        currentText = milestone.currentText,
        targetText = milestone.finalTargetText
    )

    return when {
        finalTargetStatus == NumericTargetStatus.EXACT ||
            finalTargetStatus == NumericTargetStatus.OVER -> PlannerMilestoneHighlight(
            iconResId = R.drawable.ic_confetti,
            message = finalSuccessMessage,
            containerColor = Color(0xFFE1F5EE),
            textColor = Color(0xFF085041),
            iconColor = Color(0xFF0F6E56)
        )
        weeklyTargetStatus == NumericTargetStatus.EXACT -> PlannerMilestoneHighlight(
            iconResId = R.drawable.ic_confetti,
            message = exactSuccessMessage,
            containerColor = Color(0xFFE1F5EE),
            textColor = Color(0xFF085041),
            iconColor = Color(0xFF0F6E56)
        )
        weeklyTargetStatus == NumericTargetStatus.OVER -> PlannerMilestoneHighlight(
            iconResId = R.drawable.ic_confetti,
            message = overSuccessMessage,
            containerColor = Color(0xFFE1F5EE),
            textColor = Color(0xFF085041),
            iconColor = Color(0xFF0F6E56)
        )
        milestone.status == MilestoneStatus.BEHIND -> {
            val rawDifference = displayedNumericShortfall(
                featureName = milestone.featureName,
                baselineText = milestone.baselineText,
                currentText = milestone.currentText,
                targetText = milestone.expectedText
            )
            if (rawDifference <= 0.0) {
                null
            } else {
                PlannerMilestoneHighlight(
                    iconResId = R.drawable.ic_exclamation_circle,
                    message = warningMessage(rawDifference),
                    containerColor = Color(0xFFFAEEDA),
                    textColor = Color(0xFF633806),
                    iconColor = Color(0xFF633806)
                )
            }
        }
        else -> null
    }
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

private fun displayFeatureLabel(feature: PlannerGoalFeature): String {
    return if (feature.featureName == "BMI") {
        "Berat badan"
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
        "Turunkan berat sekitar ${String.format("%.1f", abs(delta))} kg dari baseline."
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
            0 -> "Tidak pernah"
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
        "is_hypertension" -> if (value.toInt() == 1) "Belum terkontrol" else "Terkontrol"
        "is_cholesterol" -> if (value.toInt() == 1) "Belum terkontrol" else "Terkontrol"
        "is_bloodline" -> if (value.toInt() == 1) "Ya" else "Tidak"
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
    return "${String.format("%.0f", weight)} kg"
}

private fun bmiToWeight(bmi: Double, heightCm: Int): Double? {
    val heightMeters = heightCm / 100.0
    if (heightMeters <= 0.0) {
        return null
    }
    return bmi * heightMeters * heightMeters
}

private fun hasReachedDisplayedNumericTarget(
    featureName: String,
    baselineText: String,
    currentText: String,
    targetText: String
): Boolean {
    return displayedNumericTargetStatus(
        featureName = featureName,
        baselineText = baselineText,
        currentText = currentText,
        targetText = targetText
    ) != NumericTargetStatus.BELOW
}

private enum class NumericTargetStatus {
    BELOW,
    EXACT,
    OVER
}

private fun displayedNumericTargetStatus(
    featureName: String,
    baselineText: String,
    currentText: String,
    targetText: String
): NumericTargetStatus {
    val baseline = displayedNumericComparableValue(featureName, baselineText) ?: return NumericTargetStatus.BELOW
    val current = displayedNumericComparableValue(featureName, currentText) ?: return NumericTargetStatus.BELOW
    val target = displayedNumericComparableValue(featureName, targetText) ?: return NumericTargetStatus.BELOW

    return when {
        target < baseline && current < target -> NumericTargetStatus.OVER
        target < baseline && current == target -> NumericTargetStatus.EXACT
        target < baseline -> NumericTargetStatus.BELOW
        target > baseline && current > target -> NumericTargetStatus.OVER
        target > baseline && current == target -> NumericTargetStatus.EXACT
        target > baseline -> NumericTargetStatus.BELOW
        current == target -> NumericTargetStatus.EXACT
        else -> NumericTargetStatus.BELOW
    }
}

private fun displayedNumericShortfall(
    featureName: String,
    baselineText: String,
    currentText: String,
    targetText: String
): Double {
    val baseline = displayedNumericComparableValue(featureName, baselineText) ?: return 0.0
    val current = displayedNumericComparableValue(featureName, currentText) ?: return 0.0
    val target = displayedNumericComparableValue(featureName, targetText) ?: return 0.0

    return when {
        target < baseline -> (current - target).coerceAtLeast(0.0)
        target > baseline -> (target - current).coerceAtLeast(0.0)
        else -> 0.0
    }
}

private fun displayedNumericProgressFraction(
    featureName: String,
    baselineText: String,
    currentText: String,
    targetText: String
): Float? {
    val baseline = displayedNumericComparableValue(featureName, baselineText) ?: return null
    val current = displayedNumericComparableValue(featureName, currentText) ?: return null
    val target = displayedNumericComparableValue(featureName, targetText) ?: return null
    return calculateProgressFraction(baseline, target, current)
}

private fun displayedNumericComparableValue(
    featureName: String,
    text: String
): Double? {
    return when (featureName) {
        "BMI",
        "moderate_physical_activity_frequency" -> {
            Regex("-?\\d+").find(text)?.value?.toDoubleOrNull()
        }
        else -> null
    }
}

private fun categoricalMilestoneFocusText(
    featureName: String,
    isTargetReached: Boolean
): String {
    return when (featureName) {
        "is_hypertension" -> {
            if (isTargetReached) "Pertahankan tetap terkontrol" else "Pantau tekanan darah"
        }
        "is_cholesterol" -> {
            if (isTargetReached) "Pertahankan tetap terkontrol" else "Pantau status kolesterol"
        }
        "smoking_status" -> {
            if (isTargetReached) "Pertahankan berhenti merokok" else "Usahakan berhenti merokok"
        }
        else -> if (isTargetReached) "Pertahankan status target" else "Pantau perubahan status"
    }
}

private fun latestRelevantPlannerCheckIn(
    featureName: String,
    history: List<PlannerCheckInEntry>
): PlannerCheckInEntry? {
    val checkInType = when (featureName) {
        "BMI" -> "weight"
        "moderate_physical_activity_frequency" -> "activity"
        "is_hypertension" -> "hypertension"
        "is_cholesterol" -> "cholesterol"
        "smoking_status" -> "smoking"
        else -> null
    } ?: return null

    return history
        .asSequence()
        .filter { it.type == checkInType }
        .maxByOrNull { it.createdAtMillis }
}

private const val MILESTONE_TOLERANCE = 0.05f
private const val WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L
