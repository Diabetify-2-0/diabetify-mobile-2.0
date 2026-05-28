package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerGoalManager
import com.itb.diabetify.domain.model.planner.PlannerGoal

class SavePlannerGoalUseCase(
    private val plannerGoalManager: PlannerGoalManager
) {
    suspend operator fun invoke(goal: PlannerGoal) = plannerGoalManager.saveActiveGoal(goal)
}
