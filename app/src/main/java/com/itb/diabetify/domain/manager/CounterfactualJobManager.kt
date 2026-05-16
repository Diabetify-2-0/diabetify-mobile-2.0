package com.itb.diabetify.domain.manager

import kotlinx.coroutines.flow.Flow

interface CounterfactualJobManager {
    suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long = 2000L): Flow<CounterfactualJobStatus>
}

sealed class CounterfactualJobStatus {
    object Pending : CounterfactualJobStatus()
    object InProgress : CounterfactualJobStatus()
    object Completed : CounterfactualJobStatus()
    data class Failed(val error: String) : CounterfactualJobStatus()
}
