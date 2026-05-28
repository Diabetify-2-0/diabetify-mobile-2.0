package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerGoalManager

class RefreshPlannerGoalUseCase(
    private val plannerGoalManager: PlannerGoalManager
) {
    suspend operator fun invoke() = plannerGoalManager.refreshActiveGoal()
}
