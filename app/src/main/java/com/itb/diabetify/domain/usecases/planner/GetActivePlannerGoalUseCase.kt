package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerGoalManager

class GetActivePlannerGoalUseCase(
    private val plannerGoalManager: PlannerGoalManager
) {
    operator fun invoke() = plannerGoalManager.getActiveGoal()
}
