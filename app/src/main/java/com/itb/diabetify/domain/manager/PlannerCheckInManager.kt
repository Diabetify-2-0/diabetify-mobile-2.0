package com.itb.diabetify.domain.manager

import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry
import kotlinx.coroutines.flow.Flow

interface PlannerCheckInManager {
    suspend fun markCheckedIn(type: String, timestampMillis: Long = System.currentTimeMillis())
    fun getLastCheckIns(): Flow<Map<String, Long>>
    suspend fun refreshCheckIns(goalId: String)
    suspend fun refreshCheckInHistory(goalId: String)
    suspend fun recordCheckIn(entry: PlannerCheckInEntry)
    fun getCheckInHistory(): Flow<List<PlannerCheckInEntry>>
}
