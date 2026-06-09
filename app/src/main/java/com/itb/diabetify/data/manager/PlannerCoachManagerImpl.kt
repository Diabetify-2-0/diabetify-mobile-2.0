package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.planner.PlannerApiService
import com.itb.diabetify.domain.manager.PlannerCoachManager
import com.itb.diabetify.domain.model.planner.PlannerCoach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerCoachManagerImpl @Inject constructor(
    private val plannerApiService: PlannerApiService,
) : PlannerCoachManager {
    override suspend fun getActiveCoach(): PlannerCoach? {
        return plannerApiService.getActiveCoach().data?.toDomain()
    }
}
