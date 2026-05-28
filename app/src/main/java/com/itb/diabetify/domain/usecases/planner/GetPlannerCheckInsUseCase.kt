package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class GetPlannerCheckInsUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    operator fun invoke() = plannerCheckInManager.getLastCheckIns()
}
