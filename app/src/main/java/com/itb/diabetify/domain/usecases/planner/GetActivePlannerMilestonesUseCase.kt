package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerMilestoneManager

class GetActivePlannerMilestonesUseCase(
    private val plannerMilestoneManager: PlannerMilestoneManager
) {
    suspend operator fun invoke() = plannerMilestoneManager.getActiveMilestones()
}
