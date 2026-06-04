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
    val isTargetReached: Boolean
)

internal data class PlannerWeeklyMilestone(
    val label: String,
    val currentText: String,
    val expectedText: String,
    val finalTargetText: String,
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

internal data class GoalCompletionState(
    val isCompleted: Boolean,
    val isEligible: Boolean,
    val title: String,
    val message: String,
    val highlights: List<String>
) {
    val shouldShow: Boolean = isCompleted || isEligible
}

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
            label = "Aksi",
            iconResId = R.drawable.ic_planner_barbell,
            palette = PlannerFeaturePalette(
                containerColor = Color(0xFFF6F2FF),
                accentColor = Color(0xFF8A3FFC)
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
    viewModel: HomeViewModel
): List<PlannerFeatureUiModel> {
    return goal.features.map { feature ->
        val progress = buildFeatureProgress(
            feature = feature,
            currentValue = currentFeatureValue(feature.featureName, viewModel),
            heightCm = viewModel.height.value
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

internal fun buildGoalCompletionState(
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

internal fun currentMilestoneWeek(createdAtMillis: Long): Int {
    val elapsedMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)
    val elapsedWeeks = ceil(elapsedMillis.toDouble() / WEEK_MILLIS).toInt().coerceAtLeast(1)
    return elapsedWeeks.coerceIn(1, MILESTONE_TOTAL_WEEKS)
}

internal fun buildWeeklyMilestone(
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
        featureName = feature.featureName,
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
        progress.featureName in setOf("is_hypertension", "is_cholesterol", "smoking_status") && !progress.isTargetReached -> "Proses"
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
        "moderate_physical_activity_frequency" -> "${value.toInt()} Aktivitas"
        "smoking_status" -> when (value.toInt()) {
            0 -> "Tidak merokok"
            1 -> "Berhenti merokok"
            2 -> "Aktif merokok"
            else -> value.toInt().toString()
        }
        "brinkman_index" -> when (value.toInt()) {
            0 -> "Sangat rendah"
            1 -> "Ringan"
            2 -> "Sedang"
            3 -> "Tinggi"
            else -> value.toInt().toString()
        }
        "is_hypertension" -> if (value.toInt() == 1) "Terkontrol" else "Belum terkontrol"
        "is_cholesterol" -> if (value.toInt() == 1) "Terkontrol" else "Belum terkontrol"
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

private const val MILESTONE_TOTAL_WEEKS = 12
private const val MILESTONE_TOLERANCE = 0.05f
private const val COMPLETION_RISK_BUFFER_PERCENTAGE = 2.0
private const val WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L
