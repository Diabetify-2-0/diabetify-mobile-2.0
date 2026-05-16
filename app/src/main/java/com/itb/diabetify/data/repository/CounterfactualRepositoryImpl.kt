package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.counterfactual.CounterfactualApiService
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.domain.manager.CounterfactualJobManager
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.domain.repository.CounterfactualRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import okio.IOException
import retrofit2.HttpException

class CounterfactualRepositoryImpl(
    private val counterfactualApiService: CounterfactualApiService,
    private val counterfactualJobManager: CounterfactualJobManager
) : CounterfactualRepository {
    override suspend fun startCounterfactualJob(request: CounterfactualRequest): Resource<CounterfactualJobResponse> {
        return try {
            Resource.Success(counterfactualApiService.submitCounterfactual(request))
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Network error")
        } catch (e: HttpException) {
            Resource.Error(e.message ?: "Server error")
        }
    }

    override suspend fun pollCounterfactualJob(
        jobId: String,
        pollingIntervalMs: Long
    ): Flow<CounterfactualJobStatus> {
        return counterfactualJobManager.pollJobStatus(jobId, pollingIntervalMs)
    }

    override suspend fun getCounterfactualJobResult(jobId: String): Resource<CounterfactualJobResultResponse> {
        return try {
            Resource.Success(counterfactualApiService.getCounterfactualJobResult(jobId))
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Network error")
        } catch (e: HttpException) {
            Resource.Error(e.message ?: "Server error")
        }
    }
}
