package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.planner.PlannerApiService
import com.itb.diabetify.domain.manager.PlannerMilestoneManager
import com.itb.diabetify.domain.model.planner.PlannerMilestoneProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerMilestoneManagerImpl @Inject constructor(
    private val plannerApiService: PlannerApiService,
) : PlannerMilestoneManager {
    override suspend fun getActiveMilestones(): PlannerMilestoneProgress? {
        return plannerApiService.getActiveMilestones().data?.toDomain()
    }
}
