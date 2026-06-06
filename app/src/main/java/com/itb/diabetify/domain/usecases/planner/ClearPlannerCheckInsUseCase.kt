package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class ClearPlannerCheckInsUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    suspend operator fun invoke() = plannerCheckInManager.clearLastCheckIns()
}
