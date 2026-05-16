package com.itb.diabetify.domain.usecases.counterfactual

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.domain.model.counterfactual.AsyncCounterfactualResult
import com.itb.diabetify.domain.repository.CounterfactualRepository
import com.itb.diabetify.util.Resource

class RunCounterfactualAsyncUseCase(
    private val repository: CounterfactualRepository
) {
    suspend operator fun invoke(
        request: CounterfactualRequest,
        pollingIntervalMs: Long = 2000L
    ): AsyncCounterfactualResult {
        val jobResult = repository.startCounterfactualJob(request)

        return when (jobResult) {
            is Resource.Success -> {
                val jobId = jobResult.data?.data?.jobId
                if (jobId != null) {
                    AsyncCounterfactualResult(
                        jobId = jobId,
                        jobStatusFlow = repository.pollCounterfactualJob(jobId, pollingIntervalMs)
                    )
                } else {
                    AsyncCounterfactualResult(error = "Gagal mendapatkan job ID dari respons counterfactual")
                }
            }

            is Resource.Error -> {
                AsyncCounterfactualResult(
                    error = jobResult.message ?: "Gagal memulai counterfactual"
                )
            }

            else -> {
                AsyncCounterfactualResult(
                    error = "Terjadi kesalahan saat memulai counterfactual"
                )
            }
        }
    }
}
