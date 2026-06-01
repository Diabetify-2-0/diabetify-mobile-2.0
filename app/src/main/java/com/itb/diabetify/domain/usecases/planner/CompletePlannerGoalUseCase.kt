package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerGoalManager

class CompletePlannerGoalUseCase(
    private val plannerGoalManager: PlannerGoalManager
) {
    suspend operator fun invoke(goalId: String) = plannerGoalManager.deleteActiveGoal(goalId)
}
