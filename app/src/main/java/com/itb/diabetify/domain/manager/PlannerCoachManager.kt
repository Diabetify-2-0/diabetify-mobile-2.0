package com.itb.diabetify.domain.manager

import com.itb.diabetify.domain.model.planner.PlannerCoach

interface PlannerCoachManager {
    suspend fun getActiveCoach(): PlannerCoach?
}
