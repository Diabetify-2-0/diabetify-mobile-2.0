package com.itb.diabetify.domain.model.planner

data class PlannerCoach(
    val goalId: String,
    val headline: String,
    val summary: String,
    val focusThisWeek: List<String> = emptyList(),
    val actionSteps: List<String> = emptyList(),
    val monitoringPoints: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val milestoneProgress: PlannerCoachMilestoneProgress? = null,
    val generatedBy: String,
    val fallbackUsed: Boolean,
    val generatedAt: String,
    val progressWeek: Int,
    val durationWeeks: Int,
    val checkInCount: Int,
    val sourceJobId: String?,
    val projectedRiskNote: String?,
)

data class PlannerCoachMilestoneProgress(
    val progressWeek: Int,
    val durationWeeks: Int,
    val items: List<PlannerCoachMilestoneItem> = emptyList(),
)

data class PlannerCoachMilestoneItem(
    val featureName: String,
    val label: String,
    val status: String,
    val baselineValue: Double? = null,
    val targetValue: Double? = null,
    val currentValue: Double? = null,
    val baselineText: String,
    val targetText: String,
    val currentText: String,
    val expectedText: String,
    val progressFraction: Float,
    val progressPercentage: Int,
    val latestCheckInLabel: String? = null,
    val latestCheckInValue: String? = null,
)
