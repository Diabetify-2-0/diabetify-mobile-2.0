package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerGoalManager

class GetPlannerGoalHistoryUseCase(
    private val plannerGoalManager: PlannerGoalManager
) {
    operator fun invoke() = plannerGoalManager.getGoalHistory()
}
