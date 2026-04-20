package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.prediction.PredictionApiService
import com.itb.diabetify.domain.manager.WhatIfJobManager
import com.itb.diabetify.domain.manager.WhatIfJobStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatIfJobManagerImpl @Inject constructor(
    private val predictionApiService: PredictionApiService
) : WhatIfJobManager {

    private val activeJobs = mutableSetOf<String>()
    private val cancelledJobs = mutableSetOf<String>()

    override suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long): Flow<WhatIfJobStatus> = flow {
        activeJobs.add(jobId)
        cancelledJobs.remove(jobId)
        emit(WhatIfJobStatus.Pending)

        try {
            while (activeJobs.contains(jobId)) {
                if (cancelledJobs.contains(jobId)) {
                    emit(WhatIfJobStatus.Failed("Job cancelled"))
                    break
                }

                val response = predictionApiService.getWhatIfJobStatus(jobId)
                val data = response.data

                when (data.status.orEmpty().lowercase()) {
                    "pending", "submitted" -> emit(WhatIfJobStatus.Pending)
                    "processing" -> emit(WhatIfJobStatus.InProgress(50))
                    "completed" -> {
                        emit(WhatIfJobStatus.Completed)
                        break
                    }
                    "failed" -> {
                        emit(WhatIfJobStatus.Failed(data.error ?: data.message ?: "What-if prediction job failed"))
                        break
                    }
                    "cancelled" -> {
                        emit(WhatIfJobStatus.Failed("What-if prediction job was cancelled"))
                        break
                    }
                    "" -> {
                        emit(WhatIfJobStatus.Failed("Invalid what-if job status response"))
                        break
                    }
                    else -> emit(WhatIfJobStatus.InProgress(50))
                }

                delay(pollingIntervalMs)
            }
        } catch (e: Exception) {
            emit(WhatIfJobStatus.Failed(e.message ?: "Unknown error occurred"))
        } finally {
            activeJobs.remove(jobId)
            cancelledJobs.remove(jobId)
        }
    }

    override suspend fun cancelJob(jobId: String) {
        if (activeJobs.contains(jobId)) {
            cancelledJobs.add(jobId)
        }
    }
}
