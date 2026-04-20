package com.itb.diabetify.util

import com.itb.diabetify.data.remote.prediction.request.WhatIfPredictionRequest
import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.manager.WhatIfJobStatus
import com.itb.diabetify.domain.usecases.prediction.PredictionUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

private var isWhatIfPredictionInProgress = false

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

suspend fun PredictionUseCases.handleAsyncWhatIfPrediction(
    @Suppress("UNUSED_PARAMETER")
    scope: CoroutineScope,
    whatIfRequest: WhatIfPredictionRequest,
    pollingIntervalMs: Long = 2000L,
    onPending: () -> Unit = {},
    onProgress: (progress: Int) -> Unit = {},
    onCompleted: suspend (jobId: String) -> Unit = {},
    onFailed: (error: String) -> Unit = {}
) {
    if (isWhatIfPredictionInProgress) {
        onFailed("Sudah ada prediksi what-if yang sedang berjalan")
        return
    }

    isWhatIfPredictionInProgress = true

    val asyncResult = whatIfPredictionAsync(whatIfRequest, pollingIntervalMs)

    if (asyncResult.error != null) {
        isWhatIfPredictionInProgress = false
        onFailed(asyncResult.error)
        return
    }

    val statusFlow = asyncResult.jobStatusFlow
    if (statusFlow == null) {
        isWhatIfPredictionInProgress = false
        asyncResult.onComplete?.invoke()
        onFailed("Gagal memulai polling what-if")
        return
    }

    val terminalStatus = statusFlow.first { status ->
        when (status) {
            is WhatIfJobStatus.Pending -> {
                onPending()
                false
            }
            is WhatIfJobStatus.InProgress -> {
                onProgress(status.progress)
                false
            }
            is WhatIfJobStatus.Completed,
            is WhatIfJobStatus.Failed -> true
        }
    }

    when (terminalStatus) {
        is WhatIfJobStatus.Completed -> {
            asyncResult.jobId?.let { jobId ->
                onCompleted(jobId)
            }
            asyncResult.onComplete?.invoke()
            isWhatIfPredictionInProgress = false
        }
        is WhatIfJobStatus.Failed -> {
            onFailed(terminalStatus.error)
            asyncResult.onComplete?.invoke()
            isWhatIfPredictionInProgress = false
        }
        else -> Unit
    }
}
