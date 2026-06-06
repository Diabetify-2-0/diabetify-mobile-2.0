package com.itb.diabetify.domain.usecases.prediction

import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.util.PredictionUpdateNotifier
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PredictBackgroundUseCase(
    private val repository: PredictionRepository,
    private val applicationScope: CoroutineScope
) {
    operator fun invoke(pollingIntervalMs: Long = 5000L) {
        applicationScope.launch {
            PredictionUpdateNotifier.notifyPredictionUpdateStarted()
            try {
                val jobResult = repository.startPredictionJob()

                if (jobResult is Resource.Success) {
                    val jobId = jobResult.data?.data?.jobId
                    if (jobId != null) {
                        val jobStatusFlow = repository.pollPredictionJob(jobId, pollingIntervalMs)
                        when (jobStatusFlow.first { it is PredictionJobStatus.Completed || it is PredictionJobStatus.Failed }) {
                            is PredictionJobStatus.Completed -> {
                                repository.fetchLatestPrediction()
                                PredictionUpdateNotifier.notifyPredictionUpdated()
                            }
                            is PredictionJobStatus.Failed -> Unit
                            else -> Unit
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent failure
            } finally {
                PredictionUpdateNotifier.notifyPredictionUpdateFinished()
            }
        }
    }
}
