package com.itb.diabetify.domain.manager

import com.itb.diabetify.domain.model.planner.PlannerMilestoneProgress

interface PlannerMilestoneManager {
    suspend fun getActiveMilestones(): PlannerMilestoneProgress?
}
