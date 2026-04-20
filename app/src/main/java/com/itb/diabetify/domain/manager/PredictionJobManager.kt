package com.itb.diabetify.domain.manager

import kotlinx.coroutines.flow.Flow

interface PredictionJobManager {
    suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long = 2000L): Flow<PredictionJobStatus>
    suspend fun cancelJob(jobId: String)
}

sealed class PredictionJobStatus {
    object Pending : PredictionJobStatus()
    data class InProgress(val progress: Int) : PredictionJobStatus()
    object Completed : PredictionJobStatus()
    data class Failed(val error: String) : PredictionJobStatus()
}
