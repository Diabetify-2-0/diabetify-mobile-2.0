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
        val safePollingIntervalMs = pollingIntervalMs.coerceAtLeast(MIN_POLLING_INTERVAL_MS)
        val startedAtMs = System.currentTimeMillis()
        currentJobId = jobId
        isCancelled = false
        emit(PredictionJobStatus.Pending)

        while (System.currentTimeMillis() - startedAtMs < MAX_POLLING_DURATION_MS) {
            if (isCancelled) {
                emit(PredictionJobStatus.Failed("Prediction job was cancelled"))
                return@flow
            }

            val response = try {
                predictionApiService.getPredictionJobStatus(jobId)
            } catch (e: Exception) {
                emit(PredictionJobStatus.Failed(e.message ?: "Unknown error occurred"))
                return@flow
            }

            val data = response.data
            if (data == null) {
                emit(PredictionJobStatus.Failed(response.message ?: "Invalid prediction job status response"))
                return@flow
            }

            val progress = data.progress ?: 50

            when (data.status.orEmpty().lowercase()) {
                "pending", "submitted" -> emit(PredictionJobStatus.Pending)
                "processing" -> emit(PredictionJobStatus.InProgress(progress))
                "completed" -> {
                    emit(PredictionJobStatus.Completed)
                    return@flow
                }
                "failed" -> {
                    emit(PredictionJobStatus.Failed(data.error ?: data.message ?: "Prediction job failed"))
                    return@flow
                }
                "cancelled" -> {
                    emit(PredictionJobStatus.Failed("Prediction job was cancelled"))
                    return@flow
                }
                "" -> {
                    emit(PredictionJobStatus.Failed("Invalid prediction job status response"))
                    return@flow
                }
                else -> emit(PredictionJobStatus.InProgress(progress))
            }

            delay(safePollingIntervalMs)
        }

        emit(PredictionJobStatus.Failed("Prediction job timed out"))
    }

    override suspend fun cancelJob(jobId: String) {
        if (currentJobId == jobId) {
            isCancelled = true
        }
    }

    private companion object {
        private const val MAX_POLLING_DURATION_MS = 5 * 60 * 1000L
        private const val MIN_POLLING_INTERVAL_MS = 1_000L
    }
}
