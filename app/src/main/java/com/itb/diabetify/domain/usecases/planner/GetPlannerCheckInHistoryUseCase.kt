package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager

class GetPlannerCheckInHistoryUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    operator fun invoke() = plannerCheckInManager.getCheckInHistory()
}
