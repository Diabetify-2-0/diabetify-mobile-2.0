package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class RefreshPlannerCheckInsUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    suspend operator fun invoke(goalId: String) = plannerCheckInManager.refreshCheckIns(goalId)
}
