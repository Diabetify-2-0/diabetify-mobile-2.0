package com.itb.diabetify.data.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val goalHistoryState = MutableStateFlow(readHistory())

    override suspend fun saveActiveGoal(goal: PlannerGoal) {
        runCatching {
            plannerApiService.saveGoal(PlannerGoalRequest.fromDomain(goal))
        }
        saveLocalGoal(goal)
        mergeLocalHistory(listOf(goal))
    }

    override fun getActiveGoal(): Flow<PlannerGoal?> {
        return activeGoalState.asStateFlow()
    }

    override fun getGoalHistory(): Flow<List<PlannerGoal>> {
        return goalHistoryState.asStateFlow()
    }

    override suspend fun refreshActiveGoal() {
        withContext(Dispatchers.IO) {
            runCatching {
                plannerApiService.getLatestGoal().data?.toDomain()
            }.onSuccess { remoteGoal ->
                if (remoteGoal == null) {
                    clearLocalGoal()
                } else {
                    saveLocalGoal(remoteGoal)
                    mergeLocalHistory(listOf(remoteGoal))
                }
            }
        }
    }

    override suspend fun refreshGoalHistory() {
        withContext(Dispatchers.IO) {
            runCatching {
                plannerApiService.getGoalHistory().data.orEmpty().map { it.toDomain() }
            }.onSuccess { remoteHistory ->
                saveHistory(remoteHistory)
            }
        }
    }

    override suspend fun clearActiveGoal() {
        val archivedGoal = activeGoalState.value?.copy(status = PlannerGoalStatus.ARCHIVED)
        activeGoalState.value?.let { goal ->
            runCatching {
                plannerApiService.archiveGoal(goal.id)
            }.onSuccess { response ->
                response.data?.toDomain()?.let { mergeLocalHistory(listOf(it)) }
            }.onFailure {
                archivedGoal?.let { mergeLocalHistory(listOf(it)) }
            }
        }
        clearLocalGoal()
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

    private fun mergeLocalHistory(goals: List<PlannerGoal>) {
        saveHistory((goals + goalHistoryState.value).distinctBy { it.id })
    }

    private fun saveHistory(goals: List<PlannerGoal>) {
        val sortedGoals = goals
            .distinctBy { it.id }
            .sortedByDescending { it.createdAtMillis }
            .take(MAX_HISTORY_SIZE)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, gson.toJson(sortedGoals))
            .apply()
        goalHistoryState.value = sortedGoals
    }

    private fun readGoal(): PlannerGoal? {
        val rawGoal = sharedPreferences.getString(ACTIVE_GOAL_KEY, null) ?: return null
        return runCatching {
            gson.fromJson(rawGoal, PlannerGoal::class.java)
        }.getOrNull()
    }

    private fun readHistory(): List<PlannerGoal> {
        val rawHistory = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PlannerGoal>>() {}.type
            gson.fromJson<List<PlannerGoal>>(rawHistory, type)
        }.getOrNull().orEmpty()
    }

    private companion object {
        const val PREFS_NAME = "diabetify_planner_goal"
        const val ACTIVE_GOAL_KEY = "active_goal"
        const val HISTORY_KEY = "goal_history"
        const val MAX_HISTORY_SIZE = 40
    }
}
