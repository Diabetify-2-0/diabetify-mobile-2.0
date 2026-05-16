package com.itb.diabetify.domain.repository

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow

interface CounterfactualRepository {
    suspend fun startCounterfactualJob(request: CounterfactualRequest): Resource<CounterfactualJobResponse>
    suspend fun pollCounterfactualJob(
        jobId: String,
        pollingIntervalMs: Long = 2000L
    ): Flow<CounterfactualJobStatus>

    suspend fun getCounterfactualJobResult(jobId: String): Resource<CounterfactualJobResultResponse>
}
