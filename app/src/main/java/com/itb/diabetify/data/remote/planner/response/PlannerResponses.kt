package com.itb.diabetify.data.remote.planner.response

import com.google.gson.annotations.SerializedName
import com.itb.diabetify.data.remote.planner.request.PlannerCheckInRequest
import com.itb.diabetify.data.remote.planner.request.PlannerGoalRequest
import com.itb.diabetify.domain.model.planner.PlannerMilestoneItem
import com.itb.diabetify.domain.model.planner.PlannerMilestoneProgress

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

data class PlannerMilestoneProgressPayload(
    @SerializedName("progress_week")
    val progressWeek: Int,
    @SerializedName("duration_weeks")
    val durationWeeks: Int,
    @SerializedName("items")
    val items: List<PlannerMilestoneItemPayload> = emptyList(),
) {
    fun toDomain(): PlannerMilestoneProgress {
        return PlannerMilestoneProgress(
            progressWeek = progressWeek,
            durationWeeks = durationWeeks,
            items = items.map { it.toDomain() },
        )
    }
}

data class PlannerMilestoneItemPayload(
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
    fun toDomain(): PlannerMilestoneItem {
        return PlannerMilestoneItem(
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

data class PlannerMilestoneProgressResponse(
    @SerializedName("data")
    val data: PlannerMilestoneProgressPayload?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)
