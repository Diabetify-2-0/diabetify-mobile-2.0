package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.prediction.PredictionApiService
import com.itb.diabetify.domain.manager.PredictionJobManager
import com.itb.diabetify.domain.manager.PredictionJobStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionJobManagerImpl @Inject constructor(
    private val predictionApiService: PredictionApiService
) : PredictionJobManager {

    private var currentJobId: String? = null
    private var isCancelled = false

    override suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long): Flow<PredictionJobStatus> = flow {
        currentJobId = jobId
        isCancelled = false
        emit(PredictionJobStatus.Pending)

        try {
            while (true) {
                if (isCancelled) {
                    emit(PredictionJobStatus.Failed("Prediction job was cancelled"))
                    break
                }

                val response = predictionApiService.getPredictionJobStatus(jobId)
                val data = response.data
                val progress = data.progress ?: 50

                when (data.status.orEmpty().lowercase()) {
                    "pending", "submitted" -> emit(PredictionJobStatus.Pending)
                    "processing" -> emit(PredictionJobStatus.InProgress(progress))
                    "completed" -> {
                        emit(PredictionJobStatus.Completed)
                        break
                    }
                    "failed" -> {
                        emit(PredictionJobStatus.Failed(data.error ?: data.message ?: "Prediction job failed"))
                        break
                    }
                    "cancelled" -> {
                        emit(PredictionJobStatus.Failed("Prediction job was cancelled"))
                        break
                    }
                    "" -> {
                        emit(PredictionJobStatus.Failed("Invalid prediction job status response"))
                        break
                    }
                    else -> emit(PredictionJobStatus.InProgress(progress))
                }

                delay(pollingIntervalMs)
            }
        } catch (e: Exception) {
            if (!isCancelled) {
                emit(PredictionJobStatus.Failed(e.message ?: "Unknown error occurred"))
            }
        }
    }

    override suspend fun cancelJob(jobId: String) {
        if (currentJobId == jobId) {
            isCancelled = true
        }
    }
}
