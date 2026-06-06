package com.itb.diabetify.domain.model.planner

data class PlannerCheckInEntry(
    val id: String,
    val goalId: String,
    val type: String,
    val label: String,
    val valueText: String,
    val note: String,
    val createdAtMillis: Long
)
