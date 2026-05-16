package com.itb.diabetify.util

import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

suspend fun PredictionUseCases.handleAsyncPrediction(
    @Suppress("UNUSED_PARAMETER")
    scope: CoroutineScope,
    pollingIntervalMs: Long = 2000L,
    onPending: () -> Unit = {},
    onProgress: (progress: Int) -> Unit = {},
    onCompleted: suspend () -> Unit = {},
    onFailed: (error: String) -> Unit = {}
) {
    val asyncResult = predictAsync(pollingIntervalMs)

    if (asyncResult.error != null) {
        onFailed(asyncResult.error)
        return
    }

    val statusFlow = asyncResult.jobStatusFlow
    if (statusFlow == null) {
        onFailed("Gagal memulai polling prediksi")
        return
    }

    val terminalStatus = statusFlow.first { status ->
        when (status) {
            is PredictionJobStatus.Pending -> {
                onPending()
                false
            }
            is PredictionJobStatus.InProgress -> {
                onProgress(status.progress)
                false
            }
            is PredictionJobStatus.Completed,
            is PredictionJobStatus.Failed -> true
        }
    }

    when (terminalStatus) {
        is PredictionJobStatus.Completed -> {
            getLatestPrediction()
            onCompleted()
        }
        is PredictionJobStatus.Failed -> onFailed(terminalStatus.error)
        else -> Unit
    }
}
