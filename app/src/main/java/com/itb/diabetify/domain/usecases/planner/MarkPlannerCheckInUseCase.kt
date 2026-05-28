package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class MarkPlannerCheckInUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    suspend operator fun invoke(type: String) = plannerCheckInManager.markCheckedIn(type)
}
