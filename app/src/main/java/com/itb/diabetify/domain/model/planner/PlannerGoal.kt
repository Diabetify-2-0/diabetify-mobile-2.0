package com.itb.diabetify.domain.model.planner

data class PlannerGoal(
    val id: String,
    val title: String,
    val status: PlannerGoalStatus = PlannerGoalStatus.ACTIVE,
    val currentRiskPercentage: Double?,
    val targetRiskPercentage: Int,
    val projectedRiskPercentage: Double?,
    val sourceJobId: String?,
    val createdAtMillis: Long,
    val summary: String?,
    val actionSteps: List<String> = emptyList(),
    val features: List<PlannerGoalFeature> = emptyList()
)

data class PlannerGoalFeature(
    val featureName: String,
    val label: String,
    val baselineValue: Double?,
    val targetValue: Double?,
    val baselineText: String,
    val targetText: String,
    val actionLabel: String
)

enum class PlannerGoalStatus {
    ACTIVE
}
