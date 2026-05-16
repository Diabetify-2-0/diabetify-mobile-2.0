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
        emit(CounterfactualJobStatus.Pending)

        while (true) {
            val response = try {
                counterfactualApiService.getCounterfactualJobStatus(jobId)
            } catch (e: Exception) {
                emit(CounterfactualJobStatus.Failed(e.message ?: "Unknown error occurred"))
                break
            }

            val data = response.data

            when (data.jobStatus.orEmpty().lowercase()) {
                "pending", "submitted" -> emit(CounterfactualJobStatus.Pending)
                "processing" -> emit(CounterfactualJobStatus.InProgress)
                "completed", "infeasible" -> {
                    emit(CounterfactualJobStatus.Completed)
                    break
                }
                "failed", "cancelled" -> {
                    emit(
                        CounterfactualJobStatus.Failed(
                            data.error ?: data.message ?: "Counterfactual job failed"
                        )
                    )
                    break
                }
                else -> emit(CounterfactualJobStatus.InProgress)
            }

            delay(pollingIntervalMs)
        }
    }
}
