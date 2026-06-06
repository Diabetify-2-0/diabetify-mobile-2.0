package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class MarkPlannerCheckInUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    suspend operator fun invoke(goalId: String, type: String) = plannerCheckInManager.markCheckedIn(goalId, type)
}
