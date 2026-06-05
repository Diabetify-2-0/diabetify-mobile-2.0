package com.itb.diabetify.data.remote.planner.request

import com.google.gson.annotations.SerializedName
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalFeature
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus

data class PlannerGoalRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("current_risk_percentage")
    val currentRiskPercentage: Double?,
    @SerializedName("target_risk_percentage")
    val targetRiskPercentage: Int,
    @SerializedName("duration_weeks")
    val durationWeeks: Int? = null,
    @SerializedName("projected_risk_percentage")
    val projectedRiskPercentage: Double?,
    @SerializedName("source_job_id")
    val sourceJobId: String?,
    @SerializedName("created_at_millis")
    val createdAtMillis: Long,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("action_steps")
    val actionSteps: List<String>?,
    @SerializedName("features")
    val features: List<PlannerGoalFeatureRequest>?
) {
    fun toDomain(): PlannerGoal {
        return PlannerGoal(
            id = id,
            title = title,
            status = runCatching { PlannerGoalStatus.valueOf(status) }.getOrDefault(PlannerGoalStatus.ACTIVE),
            currentRiskPercentage = currentRiskPercentage,
            targetRiskPercentage = targetRiskPercentage,
            durationWeeks = durationWeeks ?: 12,
            projectedRiskPercentage = projectedRiskPercentage,
            sourceJobId = sourceJobId,
            createdAtMillis = createdAtMillis,
            summary = summary,
            actionSteps = actionSteps.orEmpty(),
            features = features.orEmpty().map { it.toDomain() }
        )
    }

    companion object {
        fun fromDomain(goal: PlannerGoal): PlannerGoalRequest {
            return PlannerGoalRequest(
                id = goal.id,
                title = goal.title,
                status = goal.status.name,
                currentRiskPercentage = goal.currentRiskPercentage,
                targetRiskPercentage = goal.targetRiskPercentage,
                durationWeeks = goal.durationWeeks,
                projectedRiskPercentage = goal.projectedRiskPercentage,
                sourceJobId = goal.sourceJobId,
                createdAtMillis = goal.createdAtMillis,
                summary = goal.summary,
                actionSteps = goal.actionSteps,
                features = goal.features.map(PlannerGoalFeatureRequest::fromDomain)
            )
        }
    }
}

data class PlannerGoalFeatureRequest(
    @SerializedName("feature_name")
    val featureName: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("baseline_value")
    val baselineValue: Double?,
    @SerializedName("target_value")
    val targetValue: Double?,
    @SerializedName("baseline_text")
    val baselineText: String,
    @SerializedName("target_text")
    val targetText: String,
    @SerializedName("action_label")
    val actionLabel: String
) {
    fun toDomain(): PlannerGoalFeature {
        return PlannerGoalFeature(
            featureName = featureName,
            label = label,
            baselineValue = baselineValue,
            targetValue = targetValue,
            baselineText = baselineText,
            targetText = targetText,
            actionLabel = actionLabel
        )
    }

    companion object {
        fun fromDomain(feature: PlannerGoalFeature): PlannerGoalFeatureRequest {
            return PlannerGoalFeatureRequest(
                featureName = feature.featureName,
                label = feature.label,
                baselineValue = feature.baselineValue,
                targetValue = feature.targetValue,
                baselineText = feature.baselineText,
                targetText = feature.targetText,
                actionLabel = feature.actionLabel
            )
        }
    }
}
