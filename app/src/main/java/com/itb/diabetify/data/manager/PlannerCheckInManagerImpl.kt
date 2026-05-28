package com.itb.diabetify.data.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itb.diabetify.data.remote.planner.PlannerApiService
import com.itb.diabetify.data.remote.planner.request.PlannerCheckInRequest
import com.itb.diabetify.domain.manager.PlannerCheckInManager
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerCheckInManagerImpl @Inject constructor(
    context: Context,
    private val gson: Gson,
    private val plannerApiService: PlannerApiService
) : PlannerCheckInManager {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lastCheckIns = MutableStateFlow(readLastCheckIns())
    private val checkInHistory = MutableStateFlow(readHistory())

    override suspend fun markCheckedIn(type: String, timestampMillis: Long) {
        sharedPreferences.edit()
            .putLong(keyFor(type), timestampMillis)
            .apply()
        lastCheckIns.value = readLastCheckIns()
    }

    override fun getLastCheckIns(): Flow<Map<String, Long>> {
        return lastCheckIns.asStateFlow()
    }

    override suspend fun refreshCheckIns(goalId: String) {
        runCatching {
            plannerApiService.getLastCheckIns(goalId).data.orEmpty()
        }.onSuccess { remoteLastCheckIns ->
            saveLastCheckIns(remoteLastCheckIns)
        }

        runCatching {
            plannerApiService.getCheckInHistory(goalId).data.orEmpty().map { it.toDomain() }
        }.onSuccess { remoteHistory ->
            saveHistoryForGoal(goalId, remoteHistory)
        }
    }

    override suspend fun refreshCheckInHistory(goalId: String) {
        runCatching {
            plannerApiService.getCheckInHistory(goalId).data.orEmpty().map { it.toDomain() }
        }.onSuccess { remoteHistory ->
            saveHistoryForGoal(goalId, remoteHistory)
        }
    }

    override suspend fun recordCheckIn(entry: PlannerCheckInEntry) {
        runCatching {
            plannerApiService.recordCheckIn(
                goalId = entry.goalId,
                request = PlannerCheckInRequest.fromDomain(entry)
            )
        }
        val updatedHistory = (listOf(entry) + checkInHistory.value)
            .distinctBy { it.id }
            .take(MAX_HISTORY_SIZE)
        saveHistory(updatedHistory)
    }

    override fun getCheckInHistory(): Flow<List<PlannerCheckInEntry>> {
        return checkInHistory.asStateFlow()
    }

    private fun readLastCheckIns(): Map<String, Long> {
        return CHECK_IN_TYPES.mapNotNull { type ->
            val value = sharedPreferences.getLong(keyFor(type), 0L)
            if (value > 0L) type to value else null
        }.toMap()
    }

    private fun keyFor(type: String): String = "last_check_in_$type"

    private fun saveLastCheckIns(values: Map<String, Long>) {
        val editor = sharedPreferences.edit()
        CHECK_IN_TYPES.forEach { type ->
            editor.remove(keyFor(type))
        }
        values.forEach { (type, timestamp) ->
            if (timestamp > 0L) {
                editor.putLong(keyFor(type), timestamp)
            }
        }
        editor.apply()
        lastCheckIns.value = readLastCheckIns()
    }

    private fun saveHistory(history: List<PlannerCheckInEntry>) {
        val updatedHistory = history
            .distinctBy { it.id }
            .sortedByDescending { it.createdAtMillis }
            .take(MAX_HISTORY_SIZE)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, gson.toJson(updatedHistory))
            .apply()
        checkInHistory.value = updatedHistory
    }

    private fun saveHistoryForGoal(goalId: String, history: List<PlannerCheckInEntry>) {
        val otherGoalHistory = checkInHistory.value.filterNot { it.goalId == goalId }
        saveHistory(history + otherGoalHistory)
    }

    private fun readHistory(): List<PlannerCheckInEntry> {
        val rawHistory = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PlannerCheckInEntry>>() {}.type
            gson.fromJson<List<PlannerCheckInEntry>>(rawHistory, type)
        }.getOrNull().orEmpty()
    }

    private companion object {
        const val PREFS_NAME = "diabetify_planner_check_in"
        const val HISTORY_KEY = "check_in_history"
        const val MAX_HISTORY_SIZE = 80
        val CHECK_IN_TYPES = listOf(
            "weight",
            "activity",
            "smoking",
            "hypertension",
            "cholesterol"
        )
    }
}
