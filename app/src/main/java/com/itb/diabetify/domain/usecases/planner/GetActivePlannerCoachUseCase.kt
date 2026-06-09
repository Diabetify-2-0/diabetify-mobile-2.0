package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCoachManager

class GetActivePlannerCoachUseCase(
    private val plannerCoachManager: PlannerCoachManager
) {
    suspend operator fun invoke() = plannerCoachManager.getActiveCoach()
}
