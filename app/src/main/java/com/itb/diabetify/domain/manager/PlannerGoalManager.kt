package com.itb.diabetify.domain.manager

import com.itb.diabetify.domain.model.planner.PlannerGoal
import kotlinx.coroutines.flow.Flow

interface PlannerGoalManager {
    suspend fun saveActiveGoal(goal: PlannerGoal)
    fun getActiveGoal(): Flow<PlannerGoal?>
    suspend fun refreshActiveGoal()
    suspend fun deleteActiveGoal(goalId: String)
}
