package com.itb.diabetify.data.manager

import android.content.Context
import com.google.gson.Gson
import com.itb.diabetify.data.remote.planner.PlannerApiService
import com.itb.diabetify.data.remote.planner.request.PlannerGoalRequest
import com.itb.diabetify.domain.manager.PlannerGoalManager
import com.itb.diabetify.domain.model.planner.PlannerGoal
import com.itb.diabetify.domain.model.planner.PlannerGoalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerGoalManagerImpl @Inject constructor(
    context: Context,
    private val gson: Gson,
    private val plannerApiService: PlannerApiService
) : PlannerGoalManager {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val activeGoalState = MutableStateFlow(readGoal())

    override suspend fun saveActiveGoal(goal: PlannerGoal) {
        runCatching {
            plannerApiService.saveGoal(PlannerGoalRequest.fromDomain(goal))
        }
        if (goal.status == PlannerGoalStatus.ACTIVE) {
            saveLocalGoal(goal)
        } else {
            clearLocalGoalIfMatches(goal.id)
        }
    }

    override fun getActiveGoal(): Flow<PlannerGoal?> {
        return activeGoalState.asStateFlow()
    }

    override suspend fun refreshActiveGoal() {
        withContext(Dispatchers.IO) {
            runCatching {
                plannerApiService.getLatestGoal().data?.toDomain()
            }.onSuccess { remoteGoal ->
                if (remoteGoal == null || remoteGoal.status != PlannerGoalStatus.ACTIVE) {
                    clearLocalGoal()
                } else {
                    saveLocalGoal(remoteGoal)
                }
            }
        }
    }

    override suspend fun deleteActiveGoal(goalId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                plannerApiService.deleteGoal(goalId)
            }

            clearLocalGoalIfMatches(goalId)
        }
    }

    private fun saveLocalGoal(goal: PlannerGoal) {
        sharedPreferences.edit()
            .putString(ACTIVE_GOAL_KEY, gson.toJson(goal))
            .apply()
        activeGoalState.value = goal
    }

    private fun clearLocalGoal() {
        sharedPreferences.edit()
            .remove(ACTIVE_GOAL_KEY)
            .apply()
        activeGoalState.value = null
    }

    private fun clearLocalGoalIfMatches(goalId: String) {
        if (activeGoalState.value?.id == goalId) {
            clearLocalGoal()
        }
    }

    private fun readGoal(): PlannerGoal? {
        val rawGoal = sharedPreferences.getString(ACTIVE_GOAL_KEY, null) ?: return null
        val goal = runCatching {
            gson.fromJson(rawGoal, PlannerGoal::class.java)
        }.getOrNull()?.let { parsedGoal ->
            if (parsedGoal.durationWeeks > 0) parsedGoal else parsedGoal.copy(durationWeeks = 12)
        }

        if (goal?.status != PlannerGoalStatus.ACTIVE) {
            sharedPreferences.edit()
                .remove(ACTIVE_GOAL_KEY)
                .apply()
            return null
        }

        return goal
    }

    private companion object {
        const val PREFS_NAME = "diabetify_planner_goal"
        const val ACTIVE_GOAL_KEY = "active_goal"
    }
}
