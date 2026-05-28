package com.itb.diabetify.domain.usecases.planner

import com.itb.diabetify.domain.manager.PlannerCheckInManager
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry

class RecordPlannerCheckInUseCase(
    private val plannerCheckInManager: PlannerCheckInManager
) {
    suspend operator fun invoke(entry: PlannerCheckInEntry) = plannerCheckInManager.recordCheckIn(entry)
}
