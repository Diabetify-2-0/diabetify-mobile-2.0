package com.itb.diabetify.data.remote.planner.response

import com.google.gson.annotations.SerializedName
import com.itb.diabetify.data.remote.planner.request.PlannerCheckInRequest
import com.itb.diabetify.data.remote.planner.request.PlannerGoalRequest
import com.itb.diabetify.domain.model.planner.PlannerCoach
import com.itb.diabetify.domain.model.planner.PlannerCoachMilestoneItem
import com.itb.diabetify.domain.model.planner.PlannerCoachMilestoneProgress

data class PlannerGoalResponse(
    @SerializedName("data")
    val data: PlannerGoalRequest?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class PlannerCheckInHistoryResponse(
    @SerializedName("data")
    val data: List<PlannerCheckInRequest>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class PlannerCheckInStateResponse(
    @SerializedName("data")
    val data: Map<String, Long>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class PlannerCoachPayload(
    @SerializedName("goal_id")
    val goalId: String,
    @SerializedName("headline")
    val headline: String,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("focus_this_week")
    val focusThisWeek: List<String> = emptyList(),
    @SerializedName("action_steps")
    val actionSteps: List<String> = emptyList(),
    @SerializedName("monitoring_points")
    val monitoringPoints: List<String> = emptyList(),
    @SerializedName("warnings")
    val warnings: List<String> = emptyList(),
    @SerializedName("milestone_progress")
    val milestoneProgress: PlannerCoachMilestoneProgressPayload? = null,
    @SerializedName("generated_by")
    val generatedBy: String,
    @SerializedName("fallback_used")
    val fallbackUsed: Boolean,
    @SerializedName("generated_at")
    val generatedAt: String,
    @SerializedName("progress_week")
    val progressWeek: Int,
    @SerializedName("duration_weeks")
    val durationWeeks: Int,
    @SerializedName("check_in_count")
    val checkInCount: Int,
    @SerializedName("source_job_id")
    val sourceJobId: String?,
    @SerializedName("projected_risk_note")
    val projectedRiskNote: String?,
) {
    fun toDomain(): PlannerCoach {
        return PlannerCoach(
            goalId = goalId,
            headline = headline,
            summary = summary,
            focusThisWeek = focusThisWeek,
            actionSteps = actionSteps,
            monitoringPoints = monitoringPoints,
            warnings = warnings,
            milestoneProgress = milestoneProgress?.toDomain(),
            generatedBy = generatedBy,
            fallbackUsed = fallbackUsed,
            generatedAt = generatedAt,
            progressWeek = progressWeek,
            durationWeeks = durationWeeks,
            checkInCount = checkInCount,
            sourceJobId = sourceJobId,
            projectedRiskNote = projectedRiskNote,
        )
    }
}

data class PlannerCoachMilestoneProgressPayload(
    @SerializedName("progress_week")
    val progressWeek: Int,
    @SerializedName("duration_weeks")
    val durationWeeks: Int,
    @SerializedName("items")
    val items: List<PlannerCoachMilestoneItemPayload> = emptyList(),
) {
    fun toDomain(): PlannerCoachMilestoneProgress {
        return PlannerCoachMilestoneProgress(
            progressWeek = progressWeek,
            durationWeeks = durationWeeks,
            items = items.map { it.toDomain() },
        )
    }
}

data class PlannerCoachMilestoneItemPayload(
    @SerializedName("feature_name")
    val featureName: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("baseline_value")
    val baselineValue: Double? = null,
    @SerializedName("target_value")
    val targetValue: Double? = null,
    @SerializedName("current_value")
    val currentValue: Double? = null,
    @SerializedName("baseline_text")
    val baselineText: String,
    @SerializedName("target_text")
    val targetText: String,
    @SerializedName("current_text")
    val currentText: String,
    @SerializedName("expected_text")
    val expectedText: String,
    @SerializedName("progress_fraction")
    val progressFraction: Float,
    @SerializedName("progress_percentage")
    val progressPercentage: Int,
    @SerializedName("latest_check_in_label")
    val latestCheckInLabel: String? = null,
    @SerializedName("latest_check_in_value")
    val latestCheckInValue: String? = null,
) {
    fun toDomain(): PlannerCoachMilestoneItem {
        return PlannerCoachMilestoneItem(
            featureName = featureName,
            label = label,
            status = status,
            baselineValue = baselineValue,
            targetValue = targetValue,
            currentValue = currentValue,
            baselineText = baselineText,
            targetText = targetText,
            currentText = currentText,
            expectedText = expectedText,
            progressFraction = progressFraction,
            progressPercentage = progressPercentage,
            latestCheckInLabel = latestCheckInLabel,
            latestCheckInValue = latestCheckInValue,
        )
    }
}

data class PlannerCoachResponse(
    @SerializedName("data")
    val data: PlannerCoachPayload?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)
