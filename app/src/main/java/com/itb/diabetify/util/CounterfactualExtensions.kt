package com.itb.diabetify.util

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.domain.usecases.counterfactual.CounterfactualUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

suspend fun CounterfactualUseCases.handleAsyncCounterfactual(
    @Suppress("UNUSED_PARAMETER")
    scope: CoroutineScope,
    request: CounterfactualRequest,
    pollingIntervalMs: Long = 2000L,
    onPending: () -> Unit = {},
    onProgress: () -> Unit = {},
    onCompleted: suspend (jobId: String) -> Unit = {},
    onFailed: (error: String) -> Unit = {}
) {
    val asyncResult = runCounterfactualAsync(request, pollingIntervalMs)

    if (asyncResult.error != null) {
        onFailed(asyncResult.error)
        return
    }

    val statusFlow = asyncResult.jobStatusFlow
    if (statusFlow == null) {
        onFailed("Gagal memulai polling counterfactual")
        return
    }

    val terminalStatus = statusFlow.first { status ->
        when (status) {
            is CounterfactualJobStatus.Pending -> {
                onPending()
                false
            }

            is CounterfactualJobStatus.InProgress -> {
                onProgress()
                false
            }

            is CounterfactualJobStatus.Completed,
            is CounterfactualJobStatus.Failed -> true
        }
    }

    when (terminalStatus) {
        is CounterfactualJobStatus.Completed -> {
            asyncResult.jobId?.let { jobId ->
                onCompleted(jobId)
            } ?: onFailed("Job selesai tetapi job ID tidak ditemukan")
        }

        is CounterfactualJobStatus.Failed -> onFailed(terminalStatus.error)
        else -> Unit
    }
}
