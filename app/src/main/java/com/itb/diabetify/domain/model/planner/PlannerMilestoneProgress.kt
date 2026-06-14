package com.itb.diabetify.domain.model.planner

data class PlannerMilestoneProgress(
    val progressWeek: Int,
    val durationWeeks: Int,
    val items: List<PlannerMilestoneItem> = emptyList(),
)

data class PlannerMilestoneItem(
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
