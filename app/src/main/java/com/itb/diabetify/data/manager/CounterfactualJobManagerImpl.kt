package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.counterfactual.CounterfactualApiService
import com.itb.diabetify.domain.manager.CounterfactualJobManager
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterfactualJobManagerImpl @Inject constructor(
    private val counterfactualApiService: CounterfactualApiService
) : CounterfactualJobManager {
    override suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long): Flow<CounterfactualJobStatus> = flow {
        val safePollingIntervalMs = pollingIntervalMs.coerceAtLeast(MIN_POLLING_INTERVAL_MS)
        val startedAtMs = System.currentTimeMillis()
        emit(CounterfactualJobStatus.Pending)

        while (System.currentTimeMillis() - startedAtMs < MAX_POLLING_DURATION_MS) {
            val response = try {
                counterfactualApiService.getCounterfactualJobStatus(jobId)
            } catch (e: Exception) {
                emit(CounterfactualJobStatus.Failed(e.message ?: "Unknown error occurred"))
                return@flow
            }

            val data = response.data
            if (data == null) {
                emit(CounterfactualJobStatus.Failed(response.message ?: "Invalid counterfactual job status response"))
                return@flow
            }

            when (data.jobStatus.orEmpty().lowercase()) {
                "pending", "submitted" -> emit(CounterfactualJobStatus.Pending)
                "processing" -> emit(CounterfactualJobStatus.InProgress)
                "completed", "infeasible" -> {
                    emit(CounterfactualJobStatus.Completed)
                    return@flow
                }
                "failed", "cancelled" -> {
                    emit(
                        CounterfactualJobStatus.Failed(
                            data.error ?: data.message ?: "Counterfactual job failed"
                        )
                    )
                    return@flow
                }
                else -> emit(CounterfactualJobStatus.InProgress)
            }

            delay(safePollingIntervalMs)
        }

        emit(CounterfactualJobStatus.Failed("Counterfactual job timed out"))
    }

    private companion object {
        private const val MAX_POLLING_DURATION_MS = 5 * 60 * 1000L
        private const val MIN_POLLING_INTERVAL_MS = 1_000L
    }
}
